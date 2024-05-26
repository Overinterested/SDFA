package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 12:42
 * @description
 */
public abstract class SVFormatFilter extends AbstractSVFilter {
    abstract public boolean filter(Array<VCFFormatField> svFormat);

}
