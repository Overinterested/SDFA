package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.ByteCode;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 23:00
 * @description
 */
public abstract class SVQualFieldFilter extends AbstractSVFieldFilter{

    abstract public boolean filter(ByteCode qualField);

}
