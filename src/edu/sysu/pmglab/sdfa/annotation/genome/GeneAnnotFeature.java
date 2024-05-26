package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Interval;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.*;
import edu.sysu.pmglab.sdfa.annotation.base.AnnotFeature;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.transcript.LeftIntergenic;
import edu.sysu.pmglab.sdfa.genome.transcript.RNAElement;
import edu.sysu.pmglab.sdfa.genome.transcript.RightIntergenic;
import edu.sysu.pmglab.sdfa.genome.transcript.UnknownRegion;

/**
 * @author Wenjie Peng
 * @create 2023-07-19 16:12
 * @description
 */
public class GeneAnnotFeature extends AnnotFeature {
    Array<RNAAnnot> pointAnnot;
    private static final VolumeByteStream cache = new VolumeByteStream();
    private final VolumeByteStream quantificationRNA = new VolumeByteStream();

    public GeneAnnotFeature() {
        pointAnnot = new Array<>();
    }

    public void add(RNAAnnot annot) {
        pointAnnot.add(annot);
    }

    public GeneAnnotFeature(Array<RNAAnnot> pointAnnot) {
        this.pointAnnot = pointAnnot;
    }

    public short getGeneIndex(int index) {
        return pointAnnot.get(index).getGeneIndex();
    }

    public short getRNAIndex(int index) {
        return pointAnnot.get(index).getRNAIndex();
    }

    public int getGeneRNAIndex(int index) {
        return pointAnnot.get(index).getGeneRNAIndex();
    }

    public ByteCode encode() {
        if (pointAnnot.isEmpty()) {
            return ByteCode.EMPTY;
        } else {
            int annotSize = pointAnnot.size();
            RNAAnnot first = pointAnnot.get(0);
            RNAAnnot end = pointAnnot.get(annotSize - 1);
            BaseArrayEncoder encoder = ByteCodeArray.getEncoder(1 + annotSize);
            BaseArrayEncoder preInfo = IntArray.getEncoder(4);
            // start and end gene index:
            preInfo.add((int) first.getGeneIndex());
            preInfo.add((int) end.getGeneIndex());
            // start and end RNA index:
            preInfo.add(first.getGeneRNAIndex());
            preInfo.add(end.getGeneRNAIndex());
            encoder.add(preInfo.flush().toByteCode());
            for (RNAAnnot item : pointAnnot) {
                encoder.add(item.encode());
            }
            ByteCode res = encoder.flush().toByteCode().asUnmodifiable();
            pointAnnot.clear();
            return res;
        }
    }

    public static GeneAnnotFeature decode(ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return null;
        } else {
            ByteCodeArray decoder = (ByteCodeArray) ArrayType.decode(src);
            Array<RNAAnnot> pointerAnnot = new Array<>();
            for (int i = 1; i < decoder.size(); i++) {
                pointerAnnot.add(RNAAnnot.decode(decoder.get(i)));
            }
            return new GeneAnnotFeature(pointerAnnot);
        }
    }

    public static Interval<Integer> getGeneRange(ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return null;
        } else {
            ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(src);
            IntArray preInfo = (IntArray) ArrayType.decode(decode.get(0));
            return new Interval<>(preInfo.get(0), preInfo.get(1));
        }
    }

    public static Interval<Integer> getGeneRNARange(ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return null;
        } else {
            ByteCodeArray decode = (ByteCodeArray) ArrayType.decode(src);
            IntArray preInfo = (IntArray) ArrayType.decode(decode.get(0));
            return new Interval<>(preInfo.get(2), preInfo.get(3));
        }
    }

    public ByteCode getStartOutput(Array<RefGeneName> refGeneNames) {
        if (pointAnnot == null || pointAnnot.isEmpty()) {
            return new ByteCode(new byte[]{ByteCode.PERIOD});
        }
        cache.reset();
        int count = 0;
        int featureSize = pointAnnot.size();
        for (RNAAnnot rnaAnnot : pointAnnot) {
            RNAElement startRegion = rnaAnnot.parse(0);
            if (startRegion instanceof LeftIntergenic || startRegion instanceof RightIntergenic) {
                continue;
            }
            cache.writeSafety(startRegion.toHGVSFrame(refGeneNames));
            if (count != featureSize - 1) {
                count++;
                cache.writeSafety(ByteCode.SEMICOLON);
            }
        }
        if (cache.size() == 0) {
            cache.writeSafety(ByteCode.PERIOD);
        }
        return cache.toByteCode();
    }

    public ByteCode getEndPosOutput(Array<RefGeneName> refGeneNames) {
        if (pointAnnot == null || pointAnnot.isEmpty()) {
            return new ByteCode(new byte[]{ByteCode.PERIOD});
        }
        cache.reset();
        int count = 0;
        int featureSize = pointAnnot.size();
        for (RNAAnnot rnaAnnot : pointAnnot) {
            RNAElement endRegion = rnaAnnot.parse(1);
            if (endRegion instanceof LeftIntergenic || endRegion instanceof RightIntergenic) {
                continue;
            }
            cache.writeSafety(endRegion.toHGVSFrame(refGeneNames));
            if (count != featureSize - 1) {
                count++;
                cache.writeSafety(ByteCode.SEMICOLON);
            }
        }
        if (cache.size() == 0) {
            cache.writeSafety(ByteCode.PERIOD);
        }
        return cache.toByteCode();
    }

    public ByteCode getCoveredOutput(Array<RefGeneName> refGeneNames) {
        if (pointAnnot == null || pointAnnot.isEmpty()) {
            return new ByteCode(new byte[]{ByteCode.N, ByteCode.o, ByteCode.n, ByteCode.e});
        }
        cache.reset();
        RefGeneName geneName;
        int firstSize = pointAnnot.size();
        for (int i = 0; i < firstSize; i++) {
            RNAAnnot rnaAnnot = pointAnnot.get(i);
            RNAElement first = rnaAnnot.parse(0);
            RNAElement second = rnaAnnot.parse(1);
            geneName = refGeneNames.get(rnaAnnot.getGeneIndex());
            cache.writeSafety(geneName.getGeneName());
            cache.writeSafety(ByteCode.COLON);
            cache.writeSafety(geneName.getRNAName(rnaAnnot.getRNAIndex()).getRNAName());
            cache.writeSafety(ByteCode.COLON);
            cache.writeSafety(first.getRNARegionName());
            if (!(second instanceof UnknownRegion)) {
                cache.writeSafety(ByteCode.MINUS);
                cache.writeSafety(second.getRNARegionName());
            }
            if (i != firstSize - 1) {
                cache.writeSafety(ByteCode.SEMICOLON);
            }
        }
        if (cache.size() == 0) {
            cache.writeSafety(ByteCode.PERIOD);
        }
        return cache.toByteCode();
    }

    public ByteCode getCoveredOutputRNAQuantification(Array<RefGeneName> refGeneNames) {
        if (pointAnnot == null || pointAnnot.isEmpty()) {
            return new ByteCode(new byte[]{ByteCode.PERIOD});
        }
        RefGeneName geneName;
        quantificationRNA.reset();
        for (RNAAnnot rnaAnnot : pointAnnot) {
            RNAElement startRegion = rnaAnnot.parse(0);
            geneName = refGeneNames.get(startRegion.getGeneIndex());
            quantificationRNA.writeSafety(geneName.getGeneName());
            quantificationRNA.writeSafety(ByteCode.COLON);
            quantificationRNA.writeSafety(geneName.getRNAName(rnaAnnot.getRNAIndex()).getRNAName());
            quantificationRNA.writeSafety(ByteCode.COLON);
//            quantificationRNA.writeSafety(rnaAnnot.getRNAQuantificationValue());
            quantificationRNA.writeSafety(ByteCode.SEMICOLON);
        }
        return quantificationRNA.toByteCode();
    }

    public Array<RNAAnnot> getRNAArray() {
        return pointAnnot;
    }

    public boolean isEmpty() {
        return pointAnnot == null || pointAnnot.isEmpty();
    }

    public int getSize() {
        if (pointAnnot == null) {
            return 0;
        }
        return pointAnnot.size();
    }

    public void reset(){
        pointAnnot.clear();
    }
}
