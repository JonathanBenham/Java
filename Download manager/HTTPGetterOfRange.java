import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;


/**
 * Class implementing Runnable (useful for threads)
 * downloads a given url.
 * Reads CHUNK_SIZE buffers at a time and writes it into a BlockingQueue.
 * Supporting the download of a range of data
 */
public class HTTPGetterOfRange implements Runnable {
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;
    static final int CHUNK_SIZE = 4096;
    private int ThreadNumber;
    private final String url;
    private final Range range;
    private final BlockingQueue<Chunk> outQueue;
    private TokenBucket tokenBucket;


    HTTPGetterOfRange(String url, Range range, BlockingQueue<Chunk> outQueueIn, TokenBucket tokenBucket, int i) {
            this.url = url;
            this.range = range;
            this.outQueue = outQueueIn;
            this.tokenBucket = tokenBucket;
            this.ThreadNumber = i;
        }

    private void downloadRange() throws IOException, InterruptedException {
        try {
            URL url = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // connection properties
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            //Range request
            conn.setRequestProperty("Range", "bytes=" + this.range.getStart() + "-" + this.range.getEnd());


            if  (conn.getResponseCode() / 100 == 2){
                InputStream inputStream = conn.getInputStream();
                long readBytes = 0;
                long bytesToRead = range.getLength();
                while (readBytes < bytesToRead) {
                    if(tokenBucket.take(CHUNK_SIZE) == CHUNK_SIZE){
                        byte[] data = new byte[CHUNK_SIZE];
                        long offset = range.getStart() + readBytes;
                        int size_in_bytes = inputStream.read(data);
                        // End of the file
                        if(size_in_bytes == -1){
                            break;
                        }

                        readBytes += size_in_bytes;
                        Chunk outChunk = new Chunk(data, offset, size_in_bytes, this.range);
                        outQueue.put(outChunk);
                    }
                }
                //If download of a thread finishes
               if(ThreadNumber !=0){
                   System.out.println("[" + ThreadNumber + "] Finished downloading");
                }
                inputStream.close();
                conn.disconnect();
            }

        } catch (IOException e) {
            String err = "Couldn't fetch range that starts at :" + this.range.getStart() + " and ends at: " + this.range.getEnd() + ". Download failed. " +
                    "Try again";
            throw new IOException(err);
        } catch (InterruptedException e ){
            String err = "Runtime exception. Download failed.";
            throw new InterruptedException(err);
        }
    }


    public void run() {
        try {
            this.downloadRange();
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}
