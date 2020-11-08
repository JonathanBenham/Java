/**
 *  Implementing a Token Bucket
 *  Used in the tockenBucketManager class
 *  Inspired from the slide
 *
 */
class TokenBucket {
    private long size;
    TokenBucket(long tokens) {
        this.size = tokens;
    }

    //setting the tockenBucket
    synchronized void set(long tokens) {
        this.size = tokens;
    }
    synchronized long take(long tokens) {
       if(tokens < this.size){
            this.size -= tokens;
         return tokens;
        }
        return 0;
    }


}
