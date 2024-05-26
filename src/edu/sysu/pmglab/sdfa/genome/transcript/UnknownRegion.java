package edu.sysu.pmglab.sdfa.genome.transcript;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.genome.RNAAnnot;
import edu.sysu.pmglab.sdfa.genome.RefGenome;

/**
 * @author wenjie peng
 * @create 2022-06-26-5:38 下午
 */
public class UnknownRegion extends RNAElement {
    Chromosome chrIndex;
    int geneIndex;
    int RNAIndex;
    private static final byte model = 12;
    public static final ByteCode region = new ByteCode(
            new byte[]{ByteCode.U, ByteCode.n, ByteCode.k, ByteCode.n,
                    ByteCode.o, ByteCode.w, ByteCode.n}
    );

    public UnknownRegion() {
    }

    public UnknownRegion(int geneIndex, int RNAIndex) {
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
    }

    @Override
    public Chromosome getChr() {
        return chrIndex;
    }

    @Override
    public short getGeneIndex() {
        return 0b111111111111;
    }

    @Override
    public short getRNAIndex() {
        return 0b1111111;
    }

    /**
     * Converts the compressed comment result into a specific RNA area
     * geneIndex<<16|RNAIndex
     *
     * @return
     */
    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model);
    }

    @Override
    public int getRelativeDis() {
        return 0x7fffffff;
    }

    @Override
    public int getOffset() {
        return 0x7fffffff;
    }

    @Override
    public ByteCode getRNARegionName() {
        return region;
    }

    public UnknownRegion setChrIndex(Chromosome chrIndex) {
        return this;
    }

    public UnknownRegion setRelativeDis(int distance) {
        return this;
    }

    @Override
    public RNAElement setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    @Override
    public UnknownRegion setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    public UnknownRegion setOffset(short offset) {
        return this;
    }

    public String toString(RefGenome refGenome) {
        return null;
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return region.toBytes();
    }

    @Override
    public UnknownRegion setOffset(int offset) {
        return this;
    }

    @Override
    public UnknownRegion setElementIndex(short inElementIndex) {
        return this;
    }

    public byte getRNARegion() {
        return model;
    }
}
