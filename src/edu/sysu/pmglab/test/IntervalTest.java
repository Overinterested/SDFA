package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 00:33
 * @description
 */
public class IntervalTest {
    public static void main(String[] args) {

        IntervalTree<Interval<Long>, Integer> tree = new IntervalTree<>();
        tree.addInterval(1,10,null);
        tree.addInterval(3,120,null);
        tree.addInterval(-14,95,null);
        tree.addInterval(-9,84,null);
        BaseArray<IntervalObject<Interval<Long>, Integer>> overlapsIntervals = tree.getOverlapsIntervals(-100, 1);
        int a = 1;
    }
}
