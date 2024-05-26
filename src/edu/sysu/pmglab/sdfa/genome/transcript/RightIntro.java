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
public class RightIntro extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    short inIntrosPos = -1;
    int offset = -1;
    private static final byte model = 6;
    int distanceFromNearestExonToCodingEnd = -1;
    private static final VolumeByteStream cache = new VolumeByteStream();
    private static final ByteCode region = new ByteCode(
            new byte[]{ByteCode.r, ByteCode.i, ByteCode.g, ByteCode.h, ByteCode.t, ByteCode.I,
                    ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o}
    );

    public RightIntro(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromNearestExonToCodingEnd, short inIntrosPos, short offset) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromNearestExonToCodingEnd = distanceFromNearestExonToCodingEnd;
        this.inIntrosPos = inIntrosPos;
        this.offset = offset;
    }

    public RightIntro() {
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName() + ":"
                + briefRNA.getRNAName() + ":"
                + "c.*" + distanceFromNearestExonToCodingEnd + (offset > 0 ? "+" + offset : offset) + "N>N:"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "Intro" + inIntrosPos;
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
        cache.writeSafety(LeftIntro.TAGBYTES);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromNearestExonToCodingEnd));
        if (offset > 0) {
            cache.writeSafety(ByteCode.ADD);
            cache.writeSafety(ValueUtils.Value2Text.int2bytes(offset));
        } else {
            cache.writeSafety(ValueUtils.Value2Text.int2bytes(offset));
        }
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.I, ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(inIntrosPos));
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return region.toBytes();
    }

    public RightIntro setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public RightIntro setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public RightIntro setOffset(short offset) {
        this.offset = offset;
        return this;
    }

    public RightIntro setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    public RightIntro setElementIndex(short inElementIndex) {
        this.inIntrosPos = inElementIndex;
        return this;
    }

    public RightIntro setRelativeDis(int distance) {
        this.distanceFromNearestExonToCodingEnd = distance;
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
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inIntrosPos, distanceFromNearestExonToCodingEnd, offset);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int inIntrosPos, int distanceFromNearestExonToCodingEnd, int offset) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inIntrosPos, distanceFromNearestExonToCodingEnd, offset);
    }

    @Override
    public int getRelativeDis() {
        return this.distanceFromNearestExonToCodingEnd;
    }

    @Override
    public int getOffset() {
        return offset;
    }


    public ByteCode getRNARegionName() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.I, ByteCode.n, ByteCode.t, ByteCode.r, ByteCode.o});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inIntrosPos));
        return cache.toByteCode();
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    public RightIntro setInIntrosPos(short inIntrosPos) {
        this.inIntrosPos = inIntrosPos;
        return this;
    }

    @Override
    public RightIntro setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public RightIntro setDistanceFromNearestExonToCodingEnd(int distanceFromNearestExonToCodingEnd) {
        this.distanceFromNearestExonToCodingEnd = distanceFromNearestExonToCodingEnd;
        return this;
    }
}
