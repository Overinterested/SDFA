package edu.sysu.pmglab.sdfa.annotation.interval;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 03:54
 * @description
 */
public class IntervalAnnotationFeature {
    final int start;
    final int end;
    final int line;

    public IntervalAnnotationFeature(int start, int end, int line) {
        this.start = start;
        this.end = end;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

}
