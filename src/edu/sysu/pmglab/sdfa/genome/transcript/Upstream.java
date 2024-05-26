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
public class Upstream extends RNAElement {
    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    int distanceFromRNAStart = -1;
    private static final byte model = 1;
    private static final VolumeByteStream cache = new VolumeByteStream();
    public static final byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD, ByteCode.MINUS};

    public Upstream(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromRNAStart) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromRNAStart = distanceFromRNAStart;
    }

    public Upstream() {
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName() + ":"
                + briefRNA.getRNAName() + ":"
                + "c.-" + distanceFromRNAStart + "N>N:"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "upstream";
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
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromRNAStart));
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.u, ByteCode.p, ByteCode.s, ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m});
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.u, ByteCode.p, ByteCode.s, ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m};
    }

    public Upstream setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public Upstream setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public Upstream setRNAIndex(short RNAIndex) {
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
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromRNAStart);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int distanceFromRNAStart) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromRNAStart);
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    @Override
    public int getRelativeDis() {
        return distanceFromRNAStart;
    }

    @Override
    public int getOffset() {
        return 0x7fffffff;
    }

    public Upstream setRelativeDis(int distance) {
        this.distanceFromRNAStart = distance;
        return this;
    }

    @Override
    public ByteCode getRNARegionName() {
        return new ByteCode(new byte[]{ByteCode.u, ByteCode.p, ByteCode.s, ByteCode.t, ByteCode.r, ByteCode.e, ByteCode.a, ByteCode.m});
    }

    public Upstream setDistanceFromRNAStart(int distanceFromRNAStart) {
        this.distanceFromRNAStart = distanceFromRNAStart;
        return this;
    }

    @Override
    public Upstream setElementIndex(short inElementIndex) {
        return this;
    }

    @Override
    public Upstream setOffset(int offset) {
        return this;
    }

}
