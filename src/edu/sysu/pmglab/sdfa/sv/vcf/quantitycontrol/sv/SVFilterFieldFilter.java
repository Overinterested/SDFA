package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.ByteCode;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 21:44
 * @description
 */
public abstract class SVFilterFieldFilter extends AbstractSVFieldFilter{

    abstract public boolean filter(ByteCode filterField);

}
