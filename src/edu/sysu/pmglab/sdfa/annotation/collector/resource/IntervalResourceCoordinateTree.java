package edu.sysu.pmglab.sdfa.annotation.collector.resource;

import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 20:07
 * @description
 */
public class IntervalResourceCoordinateTree {
    int size = 0;
    IntervalTree<Integer, Integer> coordinateIntervalTree;

    public IntervalResourceCoordinateTree() {
        this.coordinateIntervalTree = new IntervalTree<>();
    }

    public void update(int start, int end, int line) {
        size++;
        coordinateIntervalTree.addInterval(
                start, end, line
        );
    }

    public int[] getOverlap(BriefSVAnnotationFeature sv) {
        if (coordinateIntervalTree == null) {
            return new int[0];
        }
        BaseArray<IntervalObject<Integer, Integer>> overlapsIntervals = coordinateIntervalTree.getOverlapsIntervals(sv.getStart(), sv.getEnd());
        if (overlapsIntervals.isEmpty()) {
            return new int[0];
        }
        int[] relatedLines = new int[overlapsIntervals.size()];
        int count = 0;
        for (IntervalObject<Integer, Integer> overlapsInterval : overlapsIntervals) {
            relatedLines[count++] = overlapsInterval.getData();
        }
        Arrays.sort(relatedLines);
        return relatedLines;
    }

    public IntervalTree<Integer, Integer> getCoordinateIntervalTree() {
        return coordinateIntervalTree;
    }
    public boolean isEmpty(){
        return size == 0;
    }
}
