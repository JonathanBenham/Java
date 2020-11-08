/**
 * A chunk of data file
 *
 * Contains an offset, bytes of data, and size
 */
class Chunk {
    private byte[] data;
    private long offset;
    private int size_in_bytes;
    private Range range;

    Chunk(byte[] data, long offset, int size_in_bytes, Range range) {
        this.data = data != null ? data.clone() : null;
        this.offset = offset;
        this.size_in_bytes = size_in_bytes;
        this.range = range;
    }

    byte[] getData() {
        return data;
    }

    long getOffset() {
        return offset;
    }

    int getSize_in_bytes() {
        return size_in_bytes;
    }

    Range getRange(){return range;}
}
