package edu.sysu.pmglab.sdfa.merge.base;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-16 01:59
 * @description
 */
public class CSVMergeQueue {
    int lastCanBeMergedIndex = -1;
    Array<ComplexSV> toBeMerged;

    public CSVMergeQueue() {
    }

    public boolean isEmpty() {
        return toBeMerged.isEmpty();
    }

    public boolean isFullMerged() {
        return !(lastCanBeMergedIndex == -1 || ((toBeMerged.size() - 1) != lastCanBeMergedIndex));
    }


    public CSVMergeQueue setToBeMerged(Array<ComplexSV> toBeMerged) {
        this.toBeMerged = toBeMerged;
        return this;
    }
}
