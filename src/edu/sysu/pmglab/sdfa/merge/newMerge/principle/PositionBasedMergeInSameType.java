package edu.sysu.pmglab.sdfa.merge.newMerge.principle;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 00:44
 * @description
 */
public class PositionBasedMergeInSameType implements SVMergePrinciple {
    final int positionBias;
    private static PositionBasedMergeInSameType instance;

    private PositionBasedMergeInSameType(int positionBias) {
        this.positionBias = positionBias;
    }

    @Override
    public boolean merge(UnifiedSV var1, UnifiedSV var2) {
        if (Math.abs(var1.getPos() - var2.getPos()) > positionBias) {
            return false;
        }
        return Math.abs(var1.getEnd() - var2.getEnd()) <= positionBias;
    }

    @Override
    public boolean merge(ComplexSV var1, ComplexSV var2) {
        int number = var1.numOfSubSV();
        Array<UnifiedSV> var1s = var1.getSVs();
        Array<UnifiedSV> var2s = var2.getSVs();
        for (int i = 0; i < number; i++) {
            if (!merge(var1s.get(i),var2s.get(i))){
                return false;
            }
        }
        return true;
    }

    /**
     * if the merged isn't initial, return a merger using 1000bp by default
     *
     * @return a position-based merger
     */
    public static synchronized PositionBasedMergeInSameType getInstance() {
        if (instance == null) {
            return instance = new PositionBasedMergeInSameType(1000);
        }
        return instance;
    }

    public static synchronized void init(int mergingBias) {
        if (instance == null) {
            instance = new PositionBasedMergeInSameType(mergingBias);
        }
    }
}
