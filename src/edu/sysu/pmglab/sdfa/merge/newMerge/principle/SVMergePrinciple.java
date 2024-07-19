package edu.sysu.pmglab.sdfa.merge.newMerge.principle;

import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 00:43
 * @description
 */
public interface SVMergePrinciple {
    boolean merge(UnifiedSV var1, UnifiedSV var2);

    boolean merge(ComplexSV var1, ComplexSV var2);
}
