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
 * @create 2022-06-19-11:08 下午
 */
public class NCRNA extends RNAElement {

    Chromosome chrIndex;
    short geneIndex = -1;
    short RNAIndex = -1;
    int distanceFromStart;
    private static final byte model = 10;
    private static final VolumeByteStream cache = new VolumeByteStream();

    public NCRNA(Chromosome chrIndex, short geneIndex, short RNAIndex, int distanceFromStart) {
        this.chrIndex = chrIndex;
        this.geneIndex = geneIndex;
        this.RNAIndex = RNAIndex;
        this.distanceFromStart = distanceFromStart;
    }

    public NCRNA() {
    }


    @Override
    public String toString(RefGenome refGenome) {
        RefGeneName briefGene = refGenome.getBriefRefGenomeName().get(chrIndex).get(geneIndex);
        RefRNAName briefRNA = briefGene.getRNAName(RNAIndex);
        return briefGene.getGeneName() + ":"
                + briefRNA.getRNAName() + ":("
                + briefRNA.numOfExons()
                + "Exons):ncRNA";
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
        cache.writeSafety(new byte[]{40});
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(RNA.numOfExons()));
        cache.writeSafety(new byte[]{ByteCode.E, ByteCode.x, ByteCode.o, ByteCode.n, ByteCode.s});
        cache.writeSafety(new byte[]{41});
        cache.writeSafety(ByteCode.COLON);
        cache.writeSafety(new byte[]{ByteCode.n, ByteCode.c, ByteCode.R, ByteCode.N, ByteCode.A});
        return cache.toByteCode();
    }

    @Override
    public byte[] getRNARegionNameBytes() {
        return new byte[]{ByteCode.n, ByteCode.c, ByteCode.R, ByteCode.N, ByteCode.A};
    }

    @Override
    public RNAElement setOffset(int offset) {
        return this;
    }

    @Override
    public RNAElement setElementIndex(short inElementIndex) {
        return this;
    }

    public NCRNA setChrIndex(Chromosome chrIndex) {
        this.chrIndex = chrIndex;
        return this;
    }

    public NCRNA setGeneIndex(short geneIndex) {
        this.geneIndex = geneIndex;
        return this;
    }

    public NCRNA setRNAIndex(short RNAIndex) {
        this.RNAIndex = RNAIndex;
        return this;
    }


    public NCRNA setRelativeDis(int distance) {
        this.distanceFromStart = distance;
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
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromStart);
    }

    public static RNAAnnot convert(int geneIndex, int RNAIndex, int distanceFromStart) {
        return new RNAAnnot(geneIndex << 16 | RNAIndex, model, distanceFromStart);
    }

    @Override
    public int getRelativeDis() {
        return distanceFromStart;
    }

    @Override
    public int getOffset() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ByteCode getRNARegionName() {
        return new ByteCode(new byte[]{ByteCode.n, ByteCode.c, ByteCode.R, ByteCode.N, ByteCode.A});
    }

    public NCRNA setOffset(short offset) {
        return this;
    }

    @Override
    public byte getRNARegion() {
        return model;
    }

    public NCRNA setDistanceFromStart(int distanceFromStart) {
        this.distanceFromStart = distanceFromStart;
        return this;
    }

}
