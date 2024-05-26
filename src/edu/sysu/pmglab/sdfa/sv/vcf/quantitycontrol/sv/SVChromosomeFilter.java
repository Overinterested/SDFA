package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.gbc.genome.Chromosome;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 12:31
 * @description
 */
public abstract class SVChromosomeFilter extends AbstractSVFilter{

    public abstract boolean filter(Chromosome chromosome);
}
