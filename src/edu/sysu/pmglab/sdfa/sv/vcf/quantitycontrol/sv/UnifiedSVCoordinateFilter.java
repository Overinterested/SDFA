package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.sdfa.sv.SVCoordinate;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 00:38
 * @description
 */
public abstract class UnifiedSVCoordinateFilter extends AbstractUnifiedSVFilter {

    public UnifiedSVCoordinateFilter() {
        super();
    }

    @Override
    public boolean filter(UnifiedSV sv) {
        return filter(sv.getCoordinate());
    }

    public abstract boolean filter(SVCoordinate coordinate);

}
