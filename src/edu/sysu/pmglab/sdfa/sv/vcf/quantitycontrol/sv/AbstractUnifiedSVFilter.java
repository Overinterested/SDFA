package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 00:15
 * @description
 */
public abstract class AbstractUnifiedSVFilter extends AbstractSVFilter{

    public AbstractUnifiedSVFilter() {
    }

    abstract public boolean filter(UnifiedSV sv);

}
