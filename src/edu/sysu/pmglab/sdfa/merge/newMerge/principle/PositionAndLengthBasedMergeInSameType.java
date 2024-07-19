package edu.sysu.pmglab.sdfa.merge.newMerge.principle;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 01:09
 * @description
 */
public class PositionAndLengthBasedMergeInSameType implements SVMergePrinciple {
    final int lengthBias;
    final int positionBias;
    private static PositionAndLengthBasedMergeInSameType instance;

    private PositionAndLengthBasedMergeInSameType(int positionBias, int lengthBias) {
        this.lengthBias = lengthBias;
        this.positionBias = positionBias;
    }

    @Override
    public boolean merge(UnifiedSV var1, UnifiedSV var2) {
        if (Math.abs(var1.getPos() - var2.getPos()) > positionBias) {
            return false;
        } else if (Math.abs(var1.getEnd() - var2.getEnd()) > positionBias) {
            return false;
        } else return Math.abs(var1.getLength() - var2.getLength()) <= lengthBias;
    }

    @Override
    public boolean merge(ComplexSV var1, ComplexSV var2) {
        int number = var1.numOfSubSV();
        Array<UnifiedSV> var1s = var1.getSVs();
        Array<UnifiedSV> var2s = var2.getSVs();
        for (int i = 0; i < number; i++) {
            if (!merge(var1s.get(i), var2s.get(i))) {
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
    public static synchronized PositionAndLengthBasedMergeInSameType getInstance() {
        if (instance == null) {
            return instance = new PositionAndLengthBasedMergeInSameType(1000, 100);
        }
        return instance;
    }

    public static synchronized void init(int positionBias, int lengthBias) {
        if (instance == null) {
            instance = new PositionAndLengthBasedMergeInSameType(positionBias, lengthBias);
        }
    }
}
