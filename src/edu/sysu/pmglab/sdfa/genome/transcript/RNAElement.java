package edu.sysu.pmglab.sdfa.genome.transcript;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.genome.RNAAnnot;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.RefGenome;

/**
 * @author Wenjie Peng
 * @create 2023-07-20 14:55
 * @description
 */
public abstract class RNAElement {
    private static final ByteCode NONE = new ByteCode(new byte[]{ByteCode.PERIOD});

    public static RNAElement of(byte region) {
        switch (region) {
            case 0:
                return new LeftIntergenic();
            case 1:
                return new Upstream();
            case 2:
                return new UTR5();
            case 3:
                return new LeftIntro();
            case 4:
                return new Exon();
            case 5:
                return new Intro();
            case 6:
                return new RightIntro();
            case 7:
                return new UTR3();
            case 8:
                return new Downstream();
            case 9:
                return new RightIntergenic();
            case 10:
                return new NCRNA();
            case 12:
                return new UnknownRegion();
            default:
                throw new RuntimeException("No such RNA element");
        }
    }

    public abstract Chromosome getChr();

    public abstract short getGeneIndex();

    public abstract short getRNAIndex();

    public int getGeneRNAIndex() {
        return getGeneIndex() << 16 | getRNAIndex();
    }

    /**
     * Converts the compressed comment result into a specific RNA area
     * geneIndex<<16|RNAIndex
     *
     * @return
     */
    public abstract RNAAnnot transfer();

    public abstract byte getRNARegion();

    public abstract int getRelativeDis();

    public abstract int getOffset();

    public abstract ByteCode getRNARegionName();

    public abstract String toString(RefGenome refGenome);

    public ByteCode toHGVSFrame(RefGenome refGenome){
        return NONE;
    }

    public ByteCode toHGVSFrame(Array<RefGeneName> refGeneNames) {
        return NONE;
    }

    public abstract byte[] getRNARegionNameBytes();

    public abstract RNAElement setOffset(int offset);

    public abstract RNAElement setElementIndex(short inElementIndex);

    public abstract RNAElement setRelativeDis(int relativeDis);

    public abstract RNAElement setGeneIndex(short geneIndex);

    public abstract RNAElement setRNAIndex(short RNAIndex);

}
