package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.sdfa.genome.brief.RefRNAName;
import edu.sysu.pmglab.sdfa.genome.transcript.RNAElement;

/**
 * @author Wenjie Peng
 * @create 2023-07-17 15:02
 * @description here we combine
 */
public class RNAAnnot {
    /**
     * geneIndex for 2 pointers: start and end
     * 1-16 bit: gene index in Chromosome
     * 17-32 bit: RNA index in gene
     */
    int geneRNAIndex;
    /**
     * 2 short combine: start and end:
     * 1-10 bit: end position's exon or intro index (10 bit)
     * 11-14 bit: end position's RNA region index (4 bit)
     * 15-16 bit: 0 (TODO)
     * 17-26 bit: start position's exon or intro index (10 bit)
     * 27-30 bit: start position's RNA region index (4 bit)
     * 31-32 bit: 0 (TODO)
     */
    int RNARegion;
    /**
     * more details
     */
    int relativeDisForStart;
    int offsetForStart;
    int relativeDisForEnd;
    int offsetForEnd;
//    RNAQuantificationAnnotation quantificationAnnotation;
    static byte[] GeneIndex = "GeneIndex:".getBytes();
    static byte[] RNAIndex = "RNAIndex:".getBytes();
    private static final int mark = (1 << 10) - 1;
    private static final VolumeByteStream cache = new VolumeByteStream();

    public RNAAnnot() {

    }

    public RNAAnnot(int geneRNAIndex, int RNARegion) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = (RNARegion << 10);
    }

    public RNAAnnot(int geneRNAIndex, int RNARegion, int relativeDisForStart) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = (RNARegion << 10);
        this.relativeDisForStart = relativeDisForStart;
    }

    public RNAAnnot(int geneRNAIndex, int RNARegion, int relativeDisForStart, int offsetForStart) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = (RNARegion << 10);
        this.relativeDisForStart = relativeDisForStart;
        this.offsetForStart = offsetForStart;
    }

    public RNAAnnot(int geneRNAIndex, int RNARegion, int inElementIndex, int relativeDisForStart, int offsetForStart) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = (RNARegion << 10 | inElementIndex);
        this.relativeDisForStart = relativeDisForStart;
        this.offsetForStart = offsetForStart;
    }

    public RNAAnnot(int geneRNAIndex, int RNARegion, int relativeDisForStart, int offsetForStart, int relativeDisForEnd, int offsetForEnd) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = RNARegion;
        this.relativeDisForStart = relativeDisForStart;
        this.offsetForStart = offsetForStart;
        this.relativeDisForEnd = relativeDisForEnd;
        this.offsetForEnd = offsetForEnd;
    }

    public RNAAnnot(int geneRNAIndex, int RNARegion, int inElementIndex, int relativeDisForStart, int offsetForStart, int relativeDisForEnd, int offsetForEnd) {
        this.geneRNAIndex = geneRNAIndex;
        this.RNARegion = (RNARegion << 10 | inElementIndex);
        this.relativeDisForStart = relativeDisForStart;
        this.offsetForStart = offsetForStart;
        this.relativeDisForEnd = relativeDisForEnd;
        this.offsetForEnd = offsetForEnd;
    }

    public ByteCode encode() {
        return IntArray.getEncoder(6)
                .add(geneRNAIndex)
                .add(RNARegion)
                .add(relativeDisForStart)
                .add(offsetForStart)
                .add(relativeDisForEnd)
                .add(offsetForEnd)
                .flush()
                .toByteCode();
    }

    public static RNAAnnot decode(ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return null;
        }
        IntArray decode = (IntArray) ArrayType.decode(src);
        return new RNAAnnot(decode.get(0), decode.get(1), decode.get(2), decode.get(3), decode.get(4), decode.get(5));
    }

    @Override
    public String toString() {
        // GeneIndex:
        // RNAIndex:
        // start Pos: RNA Region
        // end Pos: RNA Region
        VolumeByteStream string = new VolumeByteStream();
        string.writeSafety(GeneIndex);
        string.writeSafety(ValueUtils.Value2Text.int2bytes(geneRNAIndex >>> 16));
        string.writeSafety(ByteCode.NEWLINE);
        string.writeSafety(RNAIndex);
        string.writeSafety(ValueUtils.Value2Text.int2bytes(geneRNAIndex << 16 >>> 16));
        string.writeSafety(ByteCode.NEWLINE);
        string.writeSafety(new byte[]{ByteCode.S,ByteCode.t,ByteCode.a,ByteCode.r,ByteCode.t,ByteCode.P,ByteCode.o,ByteCode.s});
        string.writeSafety(ByteCode.COLON);
        string.writeSafety(RefRNAName.getRegion((RNARegion >>> 16) & 0b00001111));
        string.writeSafety(ByteCode.NEWLINE);
        string.writeSafety(new byte[]{ByteCode.E,ByteCode.n,ByteCode.d,ByteCode.P,ByteCode.o,ByteCode.s});
        string.writeSafety(ByteCode.COLON);
        string.writeSafety(RefRNAName.getRegion((RNARegion >>> 10) & 0b00001111));
        string.writeSafety(ByteCode.NEWLINE);
        return string.toByteCode().toString();
    }

    public short getGeneIndex() {
        return (short) (geneRNAIndex >>> 16);
    }

    public short getRNAIndex() {
        return (short) (geneRNAIndex & 0xFFFF);
    }

    public int getGeneRNAIndex() {
        return geneRNAIndex;
    }

    public RNAAnnot append(RNAAnnot secondPointer) {
        relativeDisForEnd = secondPointer.relativeDisForStart;
        offsetForEnd = secondPointer.offsetForStart;
        RNARegion = RNARegion << 16 | secondPointer.RNARegion;
        return this;
    }

    public byte getRNARegionByte(int index) {
        if (index == 0) {
            return (byte) ((RNARegion >>> 26) & 0b00001111);
        } else if (index == 1) {
            return (byte) ((RNARegion >>> 10) & 0b00001111);
        } else {
            throw new UnsupportedOperationException("Index is out of two pointer");
        }
    }

    public RNAElement parse(int index) {
        switch (index) {
            case 0:
                return RNAElement.of(getRNARegionByte(0))
                        .setGeneIndex((short) (geneRNAIndex >>> 16))
                        .setRNAIndex((short) (geneRNAIndex & 0xFFFF))
                        .setElementIndex((short) ((RNARegion >> 16) & mark))
                        .setRelativeDis(relativeDisForStart)
                        .setOffset(offsetForStart);

            case 1:
                return RNAElement.of(getRNARegionByte(1))
                        .setGeneIndex((short) (geneRNAIndex >>> 16))
                        .setRNAIndex((short) (geneRNAIndex & 0xFFFF))
                        .setElementIndex((short) (RNARegion & mark))
                        .setRelativeDis(relativeDisForEnd)
                        .setOffset(offsetForEnd);
            default:
                throw new UnsupportedOperationException("Index is out of boundary!");
        }
    }

    public short getElementIndex(int index) {
        if (index == 0) {
            return (short) ((RNARegion >> 16) & mark);
        } else if (index == 1) {
            return (short) (RNARegion & mark);
        } else {
            throw new UnsupportedOperationException("Index is out of boundary!");
        }
    }

//    public RNAAnnot setQuantificationAnnotation(RNAQuantificationAnnotation quantificationAnnotation) {
//        this.quantificationAnnotation = quantificationAnnotation;
//        return this;
//    }

//    public ByteCode getRNAQuantificationValue() {
//        cache.reset();
//        cache.writeSafety(ValueUtils.Value2Text.byte2bytes(quantificationAnnotation.getRnaFeature()));
//        cache.writeSafety(ByteCode.ADD);
//        cache.writeSafety(ValueUtils.Value2Text.double2bytes(quantificationAnnotation.calculate()));
//        cache.writeSafety(ByteCode.ADD);
//        int count = 0;
//        for (byte coverageItem : quantificationAnnotation.getCoverage()) {
//            cache.writeSafety(ValueUtils.Value2Text.byte2bytes(coverageItem));
//            count++;
//            if (count != 5) {
//                cache.writeSafety(ByteCode.COMMA);
//            }
//        }
//        cache.writeSafety(ByteCode.SEMICOLON);
//        return cache.toByteCode();
//    }

}
