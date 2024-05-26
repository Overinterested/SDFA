package edu.sysu.pmglab.sdfa.merge.base;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-12 09:59
 * @description
 */
public class SimpleSVMergeQueue {
    boolean isSorted = false;
    int lastCanBeMergedIndex = -1;
    Array<UnifiedSV> toBeMerged = new Array<>();

    public SimpleSVMergeQueue() {

    }

    public boolean isEmpty(){
        return toBeMerged.isEmpty();
    }
    public boolean isFullMerged() {
        return !(lastCanBeMergedIndex == -1 || ((toBeMerged.size() - 1) != lastCanBeMergedIndex));
    }


    public void unsafeAddSV(UnifiedSV sv) {
        toBeMerged.add(sv);
        isSorted = false;
    }

    public synchronized void safeAddSV(UnifiedSV sv) {
        toBeMerged.add(sv);
        isSorted = false;
    }

    public void addToBeMergedSVArray(BaseArray<UnifiedSV> toBeMerged){
        this.toBeMerged.addAll(toBeMerged);
    }

    public BaseArray<UnifiedSV> popAll(){
        if (toBeMerged.isEmpty()){
            return toBeMerged;
        }
        return toBeMerged.popFirst(toBeMerged.size());
    }

    public boolean isSorted() {
        return isSorted;
    }

    public void sort(){
        if (!isSorted){
            toBeMerged.sort(UnifiedSV::compareTo);
            isSorted = true;
        }
    }
    public void unsorted(){
        isSorted = false;
    }
    public Array<UnifiedSV> getToBeMerged(){
        return toBeMerged;
    }
}
