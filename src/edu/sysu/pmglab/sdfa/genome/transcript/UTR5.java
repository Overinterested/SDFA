package edu.sysu.pmglab.sdfa.genome.transcript;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.genome.RNAAnnot;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.RefGenome;
import edu.sysu.pmglab.sdfa.genome.brief.RefRNAName;

/**
 * @author wenjie peng
 * @create 2022-06-19-10:25 下午
 */
public class UTR5 extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    short inExonsPos = -1;
    int offset = -1;
    int distanceFromCodingStart = -1;
    private static final byte model = 2;
    private static final VolumeByteStream cache = new VolumeByteStream();
    public final static byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD, ByteCode.MINUS};

    public UTR5(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromCodingStart) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromCodingStart = distanceFromCodingStart;
    }

    public UTR5() {

    }

    public UTR5 setOffset(short offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName() + ":"
                + briefRNA.getRNAName() + ":"
                + "c.-" + distanceFromCodingStart
                + "N>N"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "UTR5";
    }

    @Override
    public ByteCode toHGVSFrame(RefGenome refGenome) {
        return toHGVSFrame(refGenome.getBriefRefGenomeName().get(chrIndex));
    }

    @Override
    public ByteCode toHGVSFrame(Array<RefGeneName> refGeneNames) {
        cache.reset();
        RefGeneName gene = refGeneNames.get(geneIndex);
        RefRNAName RNA = gene.getRNAName(RNAIndex);
        cache.writeSafety(gene.getGeneName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(RNA.getRNAName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(TAGBYTES);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromCodingStart));
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.FIVE});
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.FIVE};
    }

    public UTR5 setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public UTR5 setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public UTR5 setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }


    public Chromosome getChrIndex() {
        return this.chrIndex;
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

    /**
     * Converts the compressed comment result into a specific RNA area
     * geneIndex<<16|RNAIndex
     *
     * @return
     */
    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsPos, distanceFromCodingStart, offset);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int inExonsPos, int distanceFromCodingStart, int offset) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsPos, distanceFromCodingStart, offset);
    }

    @Override
    public int getRelativeDis() {
        return distanceFromCodingStart;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public ByteCode getRNARegionName() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.FIVE});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inExonsPos));
        cache.writeSafety(new byte[]{41});
        return cache.toByteCode();
    }

    public UTR5 setRelativeDis(int distance) {
        this.distanceFromCodingStart = distance;
        return this;
    }

    public UTR5 setElementIndex(short inElementIndex) {
        this.inExonsPos = inElementIndex;
        return this;
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    public UTR5 setInExonsPos(short inExonsPos) {
        this.inExonsPos = inExonsPos;
        return this;
    }

    @Override
    public UTR5 setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public UTR5 setDistanceFromCodingStart(int distanceFromCodingStart) {
        this.distanceFromCodingStart = distanceFromCodingStart;
        return this;
    }
}
