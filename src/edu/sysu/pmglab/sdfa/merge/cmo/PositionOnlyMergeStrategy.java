package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:37
 * @description
 */
public class PositionOnlyMergeStrategy extends PosSVMergeStrategy {

    static ByteCode mergeMethodName = new ByteCode("SDFA_POS");


    @Override
    public boolean mergeTwoSimpleSVs(UnifiedSV sv1, UnifiedSV sv2) {
        return Math.abs(sv1.getPos() - sv2.getPos()) < mergePosRange &&
                (Math.abs(sv1.getEnd() - sv2.getEnd()) < mergePosRange);
    }

    @Override
    public boolean mergeTwoCSVs(ComplexSV csv1, ComplexSV csv2) {
        Array<UnifiedSV> svs1 = csv1.getSVs();
        Array<UnifiedSV> svs2 = csv2.getSVs();
        if (svs1.size() != svs2.size()) {
            return false;
        }
        // loop inner sub-SV
        for (int i = 0; i < svs1.size(); i++) {
            UnifiedSV sv1 = svs1.get(i);
            UnifiedSV sv2 = svs2.get(i);
            // check chr
            if (!sv1.getChr().equals(sv2.getChr())) {
                return false;
            }
            // check type
            if (sv1.getTypeIndex() != sv2.getTypeIndex()) {
                return false;
            }
            // check position
            if (Math.abs(sv1.getPos() - sv2.getPos()) >= mergePosRange ||
                    (Math.abs(sv1.getEnd() - sv2.getEnd()) >= mergePosRange)) {
                return false;
            }
        }
        return true;
    }

    public PositionOnlyMergeStrategy setMergePosRange(int mergePosRange) {
        this.mergePosRange = mergePosRange;
        return this;
    }

    @Override
    public ByteCode getMergeMethodName() {
        return mergeMethodName;
    }

}
