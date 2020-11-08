Dean Meyer: 000802794
Jonathan Benhamou: 206822991
IdcDm.class - Main class. Initiates all objects above, starts all threads, and run them consequently for data downloading.
TockenBucketManager.class - Runnable class : Initiate TockenBucket (see  above).
TokenBucket.class - Synchronized token bucket 
Chunk.java - Used to transmit "chunks" of data (ranges of data) from downloading threads to writing thread (FileWriter).
Range.class - Range of bytes. range downloading uses range  and metadata.
Metadata.java - Download status with the metadata  in the download process. 
FileWriter.java - Runnable class : writes downloaded data to destination local file and updates metadata.
HttpGetterOfRange.java - Runnable class : download a specific of bytes from a url, then transfers the data to a Blocking queue to write to disk.