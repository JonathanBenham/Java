import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.concurrent.*;
import java.io.File;
import java.io.FileNotFoundException;

public class IdcDm {

    /**
     * Receiving input arguments,
     * If it input is not correct sending error message
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int numberOfWorkers = 1;

        if (args.length < 1 || args.length > 2) {
            System.err.printf("usage:\n\tjava IdcDm URL|URL-LIST-FILE [MAX-CONCURRENT-CONNECTIONS]\n");
            System.exit(1);
        }

        else if (args.length == 2) {
            numberOfWorkers = Integer.parseInt(args[1]);
        }

        String url = args[0];
        // multiple connection or one connection downloading from one server
        DownloadURL(url, numberOfWorkers);
    }

    /**
     * Implementing Multi-server part
     * Receive name of a list file, which is a list of line
     * Each line contains a Url.
     * Useful to pick randomly a url in a list of urls
     *
     * @param f c file containing list or url's
     * @return string containing a url
     */
    public static String pickRandomUrl(File f)
    {
        String result = null;
        Random rand = new Random();
        int n = 0;
        try {
            for(Scanner sc = new Scanner(f); sc.hasNext(); )
            {
                ++n;
                String line = sc.nextLine();
                if(rand.nextInt(n) == 0)
                    result = line;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.print("Error. Opening the file for Multi-Server failed. " +
                    "Verify name of the file or try again. Download Failed");
        }

        return result;
    }



    /**
     * Initiate the file's metadata, and iterate over missing ranges(printing errors if it the case).
     * For each:
     * Check if multi-server or not
     * Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter, TockenBucketManager, and different
     * thread of HTTPRangeGetters, according to the input number  of connection requested.
     * HTTPRangeGetters, send finish marker to the Queue and terminate the TokenBucket
     * FileWriter and TockenBucketManager setup
     * Send each request to it's corresponding thread (proportionnaly distributed)
     *
     * Print "Download succeeded" or "Download failed" and delete the metadata as needed.
     * If the Download failed, then after trying again, it should work.
     *
     * @param url Url to download or list of Urls if Multi-Server
     * @param numberOfWorkers number of concurrent connections
     */
    private static void DownloadURL(String url, int numberOfWorkers) {
        boolean multiServer = false;
        String urlMetadata = url;

        //Checking if multi server or not requested
        if (!url.startsWith("http")) {
            multiServer=true;
            File UrlList = new File(url);
            urlMetadata = pickRandomUrl(UrlList);
        }

        BlockingQueue<Chunk> outQueue = new LinkedBlockingQueue<>();
        TokenBucket tokenBucket = new TokenBucket(Long.MAX_VALUE);
        Metadata metadata = new Metadata(urlMetadata);
        FileWriter fileWriter = new FileWriter(metadata, outQueue);
        TockenBucketManager tockenBucketManager = new TockenBucketManager(tokenBucket, metadata);
        Thread tockenBucketManagerThread = new Thread(tockenBucketManager);
        Thread fileWriterThread = new Thread(fileWriter);
        int size = metadata.getRangeList().size();

        if(numberOfWorkers>size){
            numberOfWorkers =  size;
            System.err.printf("Too much multiple connections. ");
        }

        ExecutorService[] executor = new ExecutorService[numberOfWorkers];
        ArrayList<ArrayList<Range>> ArrOfSublist = new ArrayList<>();
        //List<Range>[] ArrOfSublist =  new List[numberOfWorkers];
        String[] UrlOfWorker= new String[numberOfWorkers];
        Range[] EndOfSublist = new Range[numberOfWorkers];

        System.err.print("Downloading");
        if (numberOfWorkers > 1)
            System.err.printf(" using %d connections... \n", numberOfWorkers);


        for(int i = 0; i<numberOfWorkers; i++) {
            executor[i]= Executors.newSingleThreadExecutor();
            //implementing multi-server part
            if(multiServer) {
                UrlOfWorker[i] = pickRandomUrl(new File(url));
            }
            else UrlOfWorker[i] = url;
            ArrOfSublist.add(i, new ArrayList<Range>());

            //Splitting the ranges to the different threads in a proportional way, and copying them to ArrOfSublist
            // to make numberOfWorker different Arraylist
            ArrayList<Range> sublist;
            if(i==0){
                sublist = new ArrayList<Range>(metadata.getRangeList().subList(0, ((size)/numberOfWorkers)));
            }

            else {
                sublist = new ArrayList<Range>(metadata.getRangeList().subList(((i * size) / numberOfWorkers),
                        (((i + 1) * size) / numberOfWorkers)));
            }
            for(Range range : sublist)
                ArrOfSublist.get(i).add(range);
            //Printing ranges of each thread
            System.out.println("["+ (i+1) +"] Start downloading range (" + ArrOfSublist.get(i).get(0).getStart()+ "-" +
                    ArrOfSublist.get(i).get(ArrOfSublist.get(i).size()-1).getEnd()  + ") from:\n" +  UrlOfWorker[i] );
        }
        try {
            tockenBucketManagerThread.start();
            fileWriterThread.start();

            //Send each range to numberOfWorkers Threads and make them work concurrently
            for(int i = 0; i< numberOfWorkers; i++){
                EndOfSublist[i] = ArrOfSublist.get(i).get(ArrOfSublist.get(i).size() -1);
                for (Range range: ArrOfSublist.get(i)) {
                    Runnable worker;
                    //Useful to print when the thread finishes downloading
                    if(range == EndOfSublist[i]){
                        worker = new HTTPGetterOfRange(UrlOfWorker[i],range, outQueue, tokenBucket, i+1);
                    }
                    else {
                        worker = new HTTPGetterOfRange(UrlOfWorker[i], range, outQueue, tokenBucket, 0);
                    }
                    executor[i].execute(worker);
                }
            }

            //shutting down the numberOfWorkers's threads
            for( int i = 0; i<numberOfWorkers;i++ ) {
                executor[i].shutdown();
            }

        } catch (Exception e){
            System.err.println("Error in main function. Download failed. Try again.");
            System.exit(-1);
        }
    }
}
