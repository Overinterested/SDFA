package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.sdfa.sv.SVGenotype;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 00:39
 * @description
 */
public abstract class SVGenotypeFilter extends AbstractSVFieldFilter {

    public SVGenotypeFilter() {
    }

    public abstract boolean filter(SVGenotype[] genotypes);

}
