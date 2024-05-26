package edu.sysu.pmglab.sdfa.annotation.collector.resource;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 20:12
 * @description
 */
public class RefSVResourceCoordinateTree {
    int size;
    IntervalTree<BriefSVFeature, Integer> coordinateSVTree;

    public RefSVResourceCoordinateTree() {
        this.coordinateSVTree = new IntervalTree<>();
    }

    public void update(int start, int end, IRecord record, int line) {
        size++;
        coordinateSVTree.addInterval(
                start, end, new BriefSVFeature(record.get(3), record.get(4), line)
        );
    }

    public int[] getOverlap(BriefSVAnnotationFeature sv) {
        if (coordinateSVTree == null) {
            return new int[0];
        }
        BaseArray<IntervalObject<BriefSVFeature, Integer>> overlapsIntervals = coordinateSVTree.getOverlapsIntervals(sv.getStart(), sv.getEnd());
        if (overlapsIntervals.isEmpty()) {
            return new int[0];
        }
        int[] relatedLines = new int[overlapsIntervals.size()];
        int count = 0;
        for (IntervalObject<BriefSVFeature, Integer> overlapsInterval : overlapsIntervals) {
            relatedLines[count++] = overlapsInterval.getData().line;
        }
        Arrays.sort(relatedLines);
        return relatedLines;
    }

    static class BriefSVFeature {
        int len;
        int line;
        SVTypeSign type;

        public BriefSVFeature(int len, int typeIndex, int line) {
            this.len = len;
            this.line = line;
            this.type = SVTypeSign.getByIndex(typeIndex);
        }
    }

    public IntervalTree<BriefSVFeature, Integer> getCoordinateSVTree() {
        return coordinateSVTree;
    }

    public boolean isEmpty(){
        return size == 0;
    }
}
