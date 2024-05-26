package edu.sysu.pmglab.sdfa.genome.transcript;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.genome.RNAAnnot;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.RefGenome;
import edu.sysu.pmglab.sdfa.genome.brief.RefRNAName;

/**
 * @author wenjie peng
 * @create 2022-06-19-10:17 下午
 */
public class LeftIntergenic extends RNAElement {
    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    public static final byte model = 0;
    public static final ByteCode region = new ByteCode(
            new byte[]{ByteCode.L, ByteCode.e, ByteCode.f, ByteCode.t, ByteCode.I, ByteCode.n, ByteCode.t,
                    ByteCode.e, ByteCode.r, ByteCode.g, ByteCode.e, ByteCode.n, ByteCode.i, ByteCode.c}
    );

    public LeftIntergenic(Chromosome chrIndex, short geneIndex, short RNAIndex) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
    }

    public LeftIntergenic() {
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName().toString() + ":"
                + briefRNA.getRNAName().toString() + ":"
                + region;
    }


    @Override
    public byte[] getRNARegionNameBytes() {
        return region.toBytes();
    }

    @Override
    public RNAElement setOffset(int offset) {
        return this;
    }

    @Override
    public RNAElement setElementIndex(short inElementIndex) {
        return this;
    }

    public LeftIntergenic setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public LeftIntergenic setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public LeftIntergenic setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    public LeftIntergenic setRelativeDis(int distance) {
        return this;
    }

    public LeftIntergenic setOffset(short offset) {
        return this;
    }

    @Override
    public Chromosome getChr() {
        return chrIndex;
    }

    @Override
    public short getGeneIndex() {
        return geneIndex;
    }

    @Override
    public short getRNAIndex() {
        return RNAIndex;
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model);
    }

    /**
     * Converts the compressed comment result into a specific RNA area
     *
     * @return
     */
    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model);
    }


    @Override
    public int getRelativeDis() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getOffset() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ByteCode getRNARegionName() {
        return region;
    }


    @Override
    public byte getRNARegion() {
        return model;
    }
}
