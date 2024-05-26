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
public class Exon extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    short inExonsIndex = -1;
    int offset = -1;
    int distanceFromCodingStart = -1;
    public static final byte model = 4;
    private static final VolumeByteStream cache = new VolumeByteStream();
    public static final String distanceTag = "distance from coding start";
    public static final byte[] TAGBYTES = new byte[]{ByteCode.c, ByteCode.PERIOD};

    public Exon() {
    }

    public Exon(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromCodingStart, short inExonsPos, int offset) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromCodingStart = distanceFromCodingStart;
        this.inExonsIndex = inExonsPos;
        this.offset = offset;
    }

    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName().toString() + ":"
                + briefRNA.getRNAName() + ":"
                + "c." + distanceFromCodingStart + "N>N:"
                + "(" + briefRNA.numOfExons() + "Exons):"
                + "Exon" + inExonsIndex;
    }

    @Override
    public ByteCode toHGVSFrame(RefGenome refGenome) {
        return toHGVSFrame(refGenome.getBriefRefGenomeName().get(chrIndex));
    }

    @Override
    public ByteCode toHGVSFrame(Array<RefGeneName> refGeneNames) {
        cache.reset();
        RefGeneName gene =refGeneNames.get(geneIndex);
        RefRNAName RNA = gene.getRNAName(RNAIndex);
        cache.writeSafety(gene.getGeneName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(RNA.getRNAName());
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(Exon.TAGBYTES);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(distanceFromCodingStart));
        cache.writeSafety(new byte[]{ByteCode.N, 62, ByteCode.N});
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n,ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n,ByteCode.s});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(inExonsIndex));
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n,ByteCode.s};
    }

    @Override
    public Exon setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Exon setElementIndex(short inElementIndex) {
        inExonsIndex = inElementIndex;
        return this;
    }

    @Override
    public Exon setRelativeDis(int relativeDis) {
        distanceFromCodingStart = relativeDis;
        return this;
    }

    @Override
    public Exon setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    @Override
    public Exon setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }

    @Override
    public Chromosome getChr() {
        return this.chrIndex;
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
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsIndex, distanceFromCodingStart, offset);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int inExonsIndex, int distanceFromCodingStart, int offset) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, inExonsIndex, distanceFromCodingStart, offset);
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    @Override
    public int getRelativeDis() {
        return distanceFromCodingStart;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public short getInExonsIndex() {
        return inExonsIndex;
    }

    public Exon setInExonsIndex(short inExonsIndex) {
        this.inExonsIndex = inExonsIndex;
        return this;
    }

    public byte[] getBytesModel() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n,ByteCode.s});
        cache.writeSafety(this.inExonsIndex);
        return cache.getCache();
    }

    @Override
    public ByteCode getRNARegionName() {
        cache.reset();
        cache.writeSafety(new byte[]{ByteCode.E,ByteCode.x,ByteCode.o,ByteCode.n});
        cache.writeSafety(ValueUtils.Value2Text.short2bytes(inExonsIndex));
        return cache.toByteCode();
    }
}
