package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.merge.base.SimpleSVMergeQueue;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 22:48
 * @description
 */
public class PositionOverlapMergeStrategy extends AbstractSVMergeStrategy{
    @Override
    public Array<UnifiedSV> popFirstMergedSimpleSV(SimpleSVMergeQueue specificTypeSimpleSVMergeQueue) {
        return null;
    }

    @Override
    public Array<ComplexSV> popFirstCanBeMergedCSVArray(Array<ComplexSV> specificTypeCSVArray) {
        return null;
    }

    @Override
    public void mergeSimpleSVArray(Array<UnifiedSV> simpleSVArray) {
    }

    @Override
    public void mergeCSVArray(Array<ComplexSV> csvArray) {
    }

    @Override
    public ByteCode getMergeMethodName() {
        return null;
    }
}
