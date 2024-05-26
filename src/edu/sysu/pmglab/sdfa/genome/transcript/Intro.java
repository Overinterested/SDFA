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
 * @create 2022-06-19-10:26 下午
 */
public class Intro extends RNAElement {
    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    int distanceFromNearestExonToCodingStart = -1;
    short inIntrosPos = -1;
    int offset = -1;
    boolean offStrand = false;
    private static final byte model = 5;
    private static final VolumeByteStream cache = new VolumeByteStream();
    static final byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD};

    public Intro(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromNearestExonToCodingStart, short inIntrosPos, short offset, boolean offStrand) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromNearestExonToCodingStart = distanceFromNearestExonToCodingStart;
        this.inIntrosPos = inIntrosPos;
        this.offset = offset;
        this.offStrand = offStrand;
    }

    public Intro() {
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName().toString() + ":"
                + briefRNA.getRNAName().toString() + ":"
                + "c." + distanceFromNearestExonToCodingStart + (offset > 0 ? "+" + offset : offset) + "N>N:"
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
        cache.writeSafety(Intro.TAGBYTES);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromNearestExonToCodingStart));
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
        cache.writeSafety(new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n,ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.I,ByteCode.n,ByteCode.t,ByteCode.r,ByteCode.o});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(inIntrosPos));
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.I,ByteCode.n,ByteCode.t,ByteCode.r,ByteCode.o};
    }


    public Intro setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public Intro setOffset(short offset) {
        this.offset = offset;
        return this;
    }

    public Intro setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public Intro setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
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

    @Override
    public RNAAnnot transfer() {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inIntrosPos, distanceFromNearestExonToCodingStart, offset);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int inIntrosPos, int distanceFromNearestExonToCodingStart, int offset) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inIntrosPos, distanceFromNearestExonToCodingStart, offset);
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    @Override
    public int getRelativeDis() {
        return distanceFromNearestExonToCodingStart;
    }

    public Intro setRelativeDis(int relativeDis) {
        this.distanceFromNearestExonToCodingStart = relativeDis;
        return this;
    }

    public Intro setElementIndex(short inElementIndex) {
        this.inIntrosPos = inElementIndex;
        return this;
    }

    public short getInIntrosPos() {
        return inIntrosPos;
    }

    public Intro setInIntrosPos(short inIntrosPos) {
        this.inIntrosPos = inIntrosPos;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public ByteCode getRNARegionName() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.I,ByteCode.n,ByteCode.t,ByteCode.r,ByteCode.o});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inIntrosPos));
        return cache.toByteCode();
    }


    public boolean isOffStrand() {
        return offStrand;
    }

    public Intro setOffStrand(boolean offStrand) {
        this.offStrand = offStrand;
        return this;
    }


    public byte[] getBytesModel() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.I,ByteCode.n,ByteCode.t,ByteCode.r,ByteCode.o});
        cache.writeSafety(this.inIntrosPos);
        return cache.getCache();
    }


    @Override
    public Intro setOffset(int offset) {
        this.offset = offset;
        return this;
    }
}
