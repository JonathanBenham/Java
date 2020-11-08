/**
 * File is divided into ranges,
 * which consist of a starting
 * and ending bit.
 */

class Range {
    private Long end;
    private Long start;


    Range(Long start, Long end) {
        this.start = start;
        this.end = end;
    }
    Long getLength() {
        return end - start + 1;
    }

    Long getEnd() {
        return end;
    }

    Long getStart() {
        return start;
    }




}
