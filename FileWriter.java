import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;
import java.io.File;
import java.io.IOException;


/**
 * Implements runnable so to run using a thread
 * Taking chunks from the queue,
 * writes them to disk and invokes
 * remove the range from file's metadata.
 */
public class FileWriter implements Runnable {

    private Metadata metadata;
    private final BlockingQueue<Chunk> queueOfChunk;


    FileWriter(Metadata metadata, BlockingQueue<Chunk> chunkQueue) {
        this.queueOfChunk = chunkQueue;
        this.metadata = metadata;
    }

    private void writingOfChunks() throws InterruptedException, IOException {
        File file = new File(metadata.getFilename());
        RandomAccessFile writer = new RandomAccessFile(file, "rw");
        while (!this.metadata.isComplete()) {
            try{
                Chunk chunk = queueOfChunk.take();
                writer.seek(chunk.getOffset());
                writer.write(chunk.getData(),0,chunk.getSize_in_bytes());
                this.metadata.addDataDynamicallyToMetadata(chunk);

            } catch (IOException e) {
                throw new IOException("Error. Write of chunk to file failed. Download Failed. Try again.");
            } catch (InterruptedException e) {
                String err = "Runtime interruption. Download failed.";
                throw new InterruptedException(err);
            }
        }
        writer.close();
    }

    @Override
    public void run() {
        try {
            this.writingOfChunks();
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
            System.exit(-1);
        }
    }
}
