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
 * @create 2022-06-19-10:30 下午
 */
public class Downstream extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    int distanceFromRNAEnd = -1;
    public static final byte model = 8;
    public static final String distanceTag = "distance from RNA end";
    private static final VolumeByteStream cache = new VolumeByteStream();
    public static final byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD, ByteCode.STAR};

    public Downstream() {
    }

    public Downstream(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromRNAEnd) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromRNAEnd = distanceFromRNAEnd;
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName().toString() + ":"
                + briefRNA.getRNAName().toString() + ":"
                + "c.*" + distanceFromRNAEnd + "N>N:"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "downstream";
    }

    @Override
    public ByteCode toHGVSFrame(RefGenome refGenome) {
        return toHGVSFrame(refGenome.getBriefRefGenomeName().get(chrIndex));
    }

    @Override
    public ByteCode toHGVSFrame(Array<RefGeneName> refGeneNames) {
        cache.reset();
        RefGeneName briefGene = refGeneNames.get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        cache.writeSafety(briefGene.getGeneName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(briefRNA.getRNAName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(Downstream.TAGBYTES);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromRNAEnd));
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(briefRNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.d, ByteCode.o, ByteCode.w, ByteCode.n, ByteCode.s, ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m});
        return cache.toByteCode();
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
    public byte getRNARegion() {
        return 8;
    }

    public byte[] getRNARegionNameBytes() {
        return "Downstream".getBytes();
    }

    @Override
    public Downstream setOffset(int offset) {
        return this;
    }

    @Override
    public Downstream setElementIndex(short inElementIndex) {
        return this;
    }

    @Override
    public Downstream setRelativeDis(int relativeDis) {
        distanceFromRNAEnd = relativeDis;
        return this;
    }

    @Override
    public Downstream setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    @Override
    public Downstream setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    @Override
    public short getRNAIndex() {
        return RNAIndex;
    }

    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromRNAEnd);
    }

    public static RNAAnnot convert(int geneIndex, short RNAIndex, int distanceFromRNAEnd) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromRNAEnd);
    }

    @Override
    public int getRelativeDis() {
        return distanceFromRNAEnd;
    }

    @Override
    public int getOffset() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ByteCode getRNARegionName() {
        return new ByteCode(new byte[]{ByteCode.d, ByteCode.o, ByteCode.w, ByteCode.n, ByteCode.s, ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m});
    }

    public int getDistanceFromRNAEnd() {
        return distanceFromRNAEnd;
    }
}
