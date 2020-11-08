/**
 * Implements Runnable
 * To run with a thread
 * A manager for the token bucket .
 *
 */
public class TockenBucketManager implements Runnable {
    private final TokenBucket tokenBucket;
    private Metadata metadata;

    TockenBucketManager(TokenBucket tokenBucket, Metadata metadata) {
        this.tokenBucket = tokenBucket;
        this.metadata = metadata;
    }

    @Override
    public void run() {
        while (!this.metadata.isComplete()) {
            try{
               tokenBucket.set(Long.MAX_VALUE);
               Thread.sleep(1000); //taken from slide

            } catch (InterruptedException e) {
                System.err.println("TockenBucketManager interrupted. Download failed.");
                System.exit(-1);
            }
        }
    }
}
