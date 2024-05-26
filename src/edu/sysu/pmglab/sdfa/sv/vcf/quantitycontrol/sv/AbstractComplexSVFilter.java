package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.sdfa.sv.ComplexSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 00:35
 * @description
 */
public abstract class AbstractComplexSVFilter extends AbstractSVFilter{
    public AbstractComplexSVFilter() {
    }

    public abstract boolean filter(ComplexSV sv);

}
