package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.ReusableMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 00:41
 * @description
 */
public abstract class SVInfoFieldFilter extends AbstractSVFieldFilter {
    public SVInfoFieldFilter() {

    }

    abstract public boolean filter(ReusableMap<ByteCode, ByteCode> infoFieldMap);

}
