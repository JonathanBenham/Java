import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

/**
 * Help to describe what is considered to be the file metadata
 * compose of URL, file name and size
 * and which part is already on the disk (useful for resuming)
 * Which part is already stored safely in disk is useful for resume.
 * When constructing a new metadata object, we check the disk to load existing metadata if they exists.
 */
class Metadata {
    private String url;
    private long size;
    private final String fileNameOfMetadata;
    private String fileName;
    private File metadataFile;
    private ArrayList<Range> rangeList;
    private ArrayList<Range> rangeListToConserve;
    private int downloaded;

    Metadata(String url) {
        this.url = url;
        this.fileName = getName(url);
        this.fileNameOfMetadata = getNameOfMetadata(fileName);
        this.size = contentSize();
        this.metadataFile = getMetadataFile();
        this.rangeList = makeRangeList();
        this.rangeListToConserve = new ArrayList<Range>(this.rangeList);
        this.downloaded = 100 - this.rangeList.size();
    }

    private File getMetadataFile() {
        File metadataFile = new File(this.fileNameOfMetadata);
        try {
            File tempMetadataFile = new File(this.fileNameOfMetadata + ".tmp");

            // if the program has crashed before renaming it then we delete the .tmp file
            Files.deleteIfExists(tempMetadataFile.toPath());

            if (!metadataFile.exists()) {
                if(metadataFile.createNewFile()) {
                    initMetadataFile(metadataFile);
                } else {
                    System.err.println("Error creating metadata file. Download failed.");
                    System.exit(-1);
                }
            }
            return metadataFile;
        } catch (IOException e) {
            System.err.println("Error getting metadata file. Download failed.");
            System.exit(-1);
            return null;
        }
    }

    private void initMetadataFile(File metadataFile){
        try {

            // initializing the metadata file
            RandomAccessFile randomFile = new RandomAccessFile(metadataFile, "rw");
            StringBuilder stringBuilder = new StringBuilder() ;
            Long start, end, percent;
            percent = this.size/100;

            // writing to metadata file ranges, with some percert of the file size
            for (long i = 0; i< 100; i++){
                start = i*percent;
                end = start + percent - 1;
                if(i == 99 && end != this.size){
                    end = this.size;
                }
                String sRange = Long.toString(start) + ',' + Long.toString(end) + "\n";
                stringBuilder.append(sRange);
            }
            randomFile.writeBytes(stringBuilder.toString());
            randomFile.close();
        } catch (IOException e) {
            System.err.println("Error initiating metadata file. Download failed.");
            System.exit(-1);
        }
    }

    private long contentSize(){
        try {
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            int res = conn.getResponseCode();
            return  (res / 100 == 2) ? conn.getContentLengthLong() : 0;

        } catch (IOException e) {
            return 0;
        }
    }

    private ArrayList<Range> makeRangeList(){
        List<Range> rangeList = new ArrayList<>();
        try {
            RandomAccessFile randomFile = new RandomAccessFile(this.metadataFile, "rw");
            Range range;
            String line;
            String[] separated;
            while ((line = randomFile.readLine()) != null) {
                    separated = line.split(",");
                    range = new Range(Long.parseLong(separated[0]), Long.parseLong(separated[1]));
                    rangeList.add(range);
            }
            randomFile.close();
        } catch (IOException e) {
            System.err.println("Error has occurred when getting ranges. Download failed. Try again.");
            System.exit(-1);
        }
        return (ArrayList<Range>) rangeList;
    }

    public void addDataDynamicallyToMetadata(Chunk chunk){
        int i = this.rangeList.indexOf(chunk.getRange());
        Range oldRange = this.rangeListToConserve.get(i);
        long newStart = oldRange.getStart() + chunk.getSize_in_bytes();
        long newEnd = oldRange.getEnd();
        long dist = newStart - newEnd;
        Range newRange = new Range(newStart, newEnd);

        //printing percent of download
        if(dist == 1 || dist == 0 ){
            this.downloaded++;
            System.err.println("Downloaded " + this.downloaded + "%");
        }

        //  delete metadata file when download succeed
        if (this.isComplete()){
            System.err.println("Download succeeded");
            this.deleteMetadata();
        } else {
            this.rangeListToConserve.set(i, newRange);
            writeNewRangeToTemp();
        }
    }

    private void writeNewRangeToTemp(){
        try {
            File MetadataFileTemp = new File(this.fileNameOfMetadata + ".tmp");

            if (!MetadataFileTemp.exists()) {
                if(!MetadataFileTemp.createNewFile()){
                    System.err.println("Error while creating temporary metadata file. Download failed. Try again.");
                    System.exit(-1);
                }
            }

            //  actual ranges written to temp file
            RandomAccessFile randomTemp = new RandomAccessFile(MetadataFileTemp, "rw");
            StringBuilder stringBuilder = new StringBuilder() ;
            for (Range range: this.rangeListToConserve) {
                long start = range.getStart();
                long end = range.getEnd();
                long dist = start - end;
                if(!(dist == 1 || dist == 0)){
                    String sRange = Long.toString(start) + ',' + Long.toString(end) + "\n";
                    stringBuilder.append(sRange);
                }
            }
            randomTemp.writeBytes(stringBuilder.toString());
            randomTemp.close();

            //attempt to rename .tmp file
            try {
                Files.move(MetadataFileTemp.toPath(), this.metadataFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }catch (IOException e) {
                System.err.println("Error. Renaming .tmp file failed. Download failed.");
                System.exit(-1);
            }
        } catch (IOException e) {
            System.err.println("Error. Writing to .tmp file failed. Download failed.");
            System.exit(-1);
        }
    }

    String getFilename() {
        return fileName;
    }

    boolean isComplete() {
        return (this.downloaded == 100);
    }

    public ArrayList<Range> getRangeList(){
        return this.rangeList;
    }

    private static String getNameOfMetadata(String filename) {
        return filename + ".metadata";
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }

    private void deleteMetadata() {
        if(this.metadataFile.exists()) {

            try {
                Files.delete(this.metadataFile.toPath());
            } catch (IOException e) {
                System.err.println("Metadata deletion failed");
            }
        }
    }
}
