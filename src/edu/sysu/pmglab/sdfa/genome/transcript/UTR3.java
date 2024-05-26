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
 * @create 2022-06-19-10:31 下午
 */
public class UTR3 extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    short inExonsPos = -1;
    int offset = -1;
    int distanceFromCodingEnd = -1;
    private static final byte model = 7;
    private static final VolumeByteStream cache = new VolumeByteStream();
    public final static byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD, ByteCode.STAR};

    public UTR3(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromCodingEnd) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromCodingEnd = distanceFromCodingEnd;
    }

    public UTR3() {
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName() + ":"
                + briefRNA.getRNAName() + ":"
                + "c.*" + distanceFromCodingEnd
                + "N>N"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "UTR3";
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
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromCodingEnd));
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.THREE});
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.THREE};
    }

    public UTR3 setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public UTR3 setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public UTR3 setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }


    public UTR3 setOffset(short offset) {
        this.offset = offset;
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

    /**
     * Converts the compressed comment result into a specific RNA area
     * geneIndex<<16|RNAIndex
     *
     * @return
     */
    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsPos, distanceFromCodingEnd, offset);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int inExonsPos, int distanceFromCodingEnd, int offset) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsPos, distanceFromCodingEnd, offset);
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    @Override
    public int getRelativeDis() {
        return this.distanceFromCodingEnd;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public ByteCode getRNARegionName() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.U, ByteCode.T, ByteCode.R, ByteCode.THREE});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inExonsPos));
        cache.writeSafety(new byte[]{41});
        return cache.toByteCode();
    }

    public UTR3 setRelativeDis(int distance) {
        this.distanceFromCodingEnd = distance;
        return this;
    }

    public UTR3 setElementIndex(short inElementIndex) {
        this.inExonsPos = inElementIndex;
        return this;
    }


    public byte[] getBytesModel() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inExonsPos));
        return cache.getCache();
    }

    public UTR3 setInExonsPos(short inExonsPos) {
        this.inExonsPos = inExonsPos;
        return this;
    }

    @Override
    public UTR3 setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public UTR3 setDistanceFromCodingEnd(int distanceFromCodingEnd) {
        this.distanceFromCodingEnd = distanceFromCodingEnd;
        return this;
    }
}
