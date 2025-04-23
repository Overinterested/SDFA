package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.sdfa.merge.SDFAMergeManager;
import edu.sysu.pmglab.sdfa.merge.base.SimpleSVMergeQueue;
import edu.sysu.pmglab.sdfa.sv.*;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 20:46
 * @description
 */
public abstract class PosSVMergeStrategy extends AbstractSVMergeStrategy {
    int decimalPrecision = 4;
    boolean containAF = true;
    boolean containCI = true;
    boolean containAVG = true;
    boolean containIDList = true;
    boolean containMergeName = false;
    boolean containStandardDev = true;
    boolean containSupportVector = true;
    public static int mergePosRange = 1000;
    public static boolean outputMeanPosFunc = false;
    static StringBuilder builder = new StringBuilder();
    static ByteCode AF_BYTECODE = new ByteCode("AF");
    static ByteCode CHR_BYTECODE = new ByteCode("CHR");
    static ByteCode POS_BYTECODE = new ByteCode("POS");
    static ByteCode END_BYTECODE = new ByteCode("END");
    static ByteCode LENTH_BYTECODE = new ByteCode("SVLEN");
    static ByteCode CIPOS_BYTECODE = new ByteCode("CIPOS");
    static ByteCode CIEND_BYTECODE = new ByteCode("CIEND");
    static ByteCode CILEN_BYTECODE = new ByteCode("CILEN");
    static ByteCode TYPE_BYTECODE = new ByteCode("SVTYPE");
    static ByteCode METHOD_BYTECODE = new ByteCode("METHOD");
    static ByteCode IDLIST_BYTECODE = new ByteCode("IDLIST");
    static ByteCode SUPPVEC_BYTECODE = new ByteCode("SUPP_VEC");
    static ByteCode AVERAGEEND_BYTECODE = new ByteCode("AVG_END");
    static ByteCode AVERAGELENGTH_BYTECODE = new ByteCode("AVG_LEN");
    static ByteCode AVERAGESTART_BYTECODE = new ByteCode("AVG_POS");
    static ByteCode STDEV_POS = new ByteCode("STDEV_POS");
    static ByteCode STDEV_END = new ByteCode("STDEV_END");

    public Array<UnifiedSV> popFirstMergedSimpleSV(SimpleSVMergeQueue specificTypeSimpleSVMergeQueue) {
        Array<UnifiedSV> res = new Array<>();
        specificTypeSimpleSVMergeQueue.sort();
        Array<UnifiedSV> toBeMerged = specificTypeSimpleSVMergeQueue.getToBeMerged();
        int mergedSize = 1;
        UnifiedSV firstNode = toBeMerged.get(0);
        for (int i = 1; i < toBeMerged.size(); i++) {
            if (!mergeTwoSimpleSVs(toBeMerged.get(i), firstNode)) {
                break;
            }
            mergedSize++;
        }
        res.addAll(toBeMerged.popFirst(mergedSize));
        return res;
    }

    public Array<ComplexSV> popFirstCanBeMergedCSVArray(Array<ComplexSV> specificTypeCSVArray) {
        Array<ComplexSV> res = new Array<>();
        if (specificTypeCSVArray.isEmpty()) {
            return res;
        }
        specificTypeCSVArray.sort(ComplexSV::compareTo);
        int mergedSize = 1;
        for (int i = 1; i < specificTypeCSVArray.size(); i++) {
            if (!mergeTwoCSVs(specificTypeCSVArray.get(i), specificTypeCSVArray.get(0))) {
                mergedSize++;
                break;
            }
        }
        res.addAll(specificTypeCSVArray.popFirst(mergedSize));
        return res;
    }

    abstract public boolean mergeTwoCSVs(ComplexSV csv1, ComplexSV csv2);

    abstract public boolean mergeTwoSimpleSVs(UnifiedSV sv1, UnifiedSV sv2);


    @Override
    public void mergeSimpleSVArray(Array<UnifiedSV> simpleSVArray) {
        MergedSV mergedSV = mergeSimpleSVs(simpleSVArray, containSupportVector, containIDList, getTmpMergeSV());
        ByteCodeArray info = mergedSV.sv.getSpecificInfoField();
        if (info == null) {
            info = new ByteCodeArray();
            mergedSV.sv.setRawInfoField(info);
        }
        Array<Object> mergedInfoFeatureArray = mergedSV.infoFeatureArray;
        if (mergedSV.cache == null) {
            mergedSV.cache = new VolumeByteStream();
        }
        VolumeByteStream cache = mergedSV.cache;
        // modify the info in mergedSV
        boolean infoChanged = getMergedInfo(mergedInfoFeatureArray, cache);
        if (infoChanged) {
            if (containMergeName) {
                addWrappedTagValue(cache, METHOD_BYTECODE, getMergeMethodName());
            }
            ByteCode mergedInfo = cache.toByteCode().asUnmodifiable();
            for (int i = 0; i < 5; i++) {
                mergedInfoFeatureArray.set(i, null);
            }
            cache.reset();
            //region write SV info: TYPE, LENGTH, END
            addWrappedTagValue(cache, CHR_BYTECODE, mergedSV.sv.getChr().getName());
            addWrappedTagValue(cache, TYPE_BYTECODE, mergedSV.sv.getSVTypeName());
            addWrappedTagValue(cache, LENTH_BYTECODE, ValueUtils.Value2Text.int2bytes(mergedSV.sv.getLength()));
            addWrappedTagValue(cache, END_BYTECODE, ValueUtils.Value2Text.int2bytes(mergedSV.sv.getEnd()));
            //endregion
            // extra merged info
            cache.writeSafety(mergedInfo);
            info.add(cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
    }

    @Override
    public void mergeCSVArray(Array<ComplexSV> csvArray) {
        int mergedSVSize = csvArray.size();
        ComplexSV middleComplexSV = csvArray.get(mergedSVSize / 2);
        UnifiedSV firstSimpleSVInMiddleSV = middleComplexSV.getSVs().get(0);
        int innerSVSizeInSingleComplexSV = csvArray.get(0).getSVs().size();
        Array<MergedSV> csvInnerSimpleSV = new Array<>();
        csvInnerSimpleSV.clear();
        //region get each sub-SV merged info
        for (int i = 0; i < innerSVSizeInSingleComplexSV; i++) {
            Array<UnifiedSV> tmpUnifiedSVArray = new Array<>(mergedSVSize);
            for (int j = 0; j < mergedSVSize; j++) {
                tmpUnifiedSVArray.add(csvArray.get(j).getSVs().get(i));
            }
            if (i == 0) {
                csvInnerSimpleSV.add(mergeSimpleSVs(tmpUnifiedSVArray, containSupportVector, containIDList, new MergedSV()));
            } else {
                csvInnerSimpleSV.add(mergeSimpleSVs(tmpUnifiedSVArray, false, false, new MergedSV()));
            }
        }
        VolumeByteStream cache = csvInnerSimpleSV.get(0).cache;
        //endregion
        //region write represented merged SV information: CHR, TYPE, START, LEN, END
        Array<UnifiedSV> innerSVs = middleComplexSV.getSVs();
        for (int i = 0; i < innerSVSizeInSingleComplexSV; i++) {
            UnifiedSV tmpInnerSimpleSV = innerSVs.get(i);
            if (i == 0) {
                addWrappedTagValue(cache, CHR_BYTECODE, tmpInnerSimpleSV.getChr().getName());
                addWrappedTagValue(cache, TYPE_BYTECODE, tmpInnerSimpleSV.getSVTypeName());
                addWrappedTagValue(cache, POS_BYTECODE, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getPos()));
                addWrappedTagValue(cache, LENTH_BYTECODE, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getLength()));
                addWrappedTagValue(cache, END_BYTECODE, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getEnd()));
            } else {
                addWrappedIndexTagValue(cache, CHR_BYTECODE, i + 1, tmpInnerSimpleSV.getChr().getName());
                addWrappedIndexTagValue(cache, TYPE_BYTECODE, i + 1, tmpInnerSimpleSV.getSVTypeName());
                addWrappedIndexTagValue(cache, POS_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getPos()));
                addWrappedIndexTagValue(cache, LENTH_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getLength()));
                addWrappedIndexTagValue(cache, END_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(tmpInnerSimpleSV.getEnd()));
            }
        }
        //endregion
        //region write the CI, AVG of each simple SV
        for (int i = 0; i < innerSVSizeInSingleComplexSV; i++) {
            MergedSV tmpInnerMergedSV = csvInnerSimpleSV.get(i);
            if (i == 0) {
                if (containCI) {
                    Object tmpCI = tmpInnerMergedSV.infoFeatureArray.get(0);
                    if (tmpCI instanceof int[]) {
                        int[] innerSVCI = (int[]) tmpCI;
                        addWrappedTagValues(cache, CIPOS_BYTECODE, ValueUtils.Value2Text.int2bytes(innerSVCI[0]), ValueUtils.Value2Text.int2bytes(innerSVCI[1]));
                        addWrappedTagValues(cache, CILEN_BYTECODE, ValueUtils.Value2Text.int2bytes(innerSVCI[2]), ValueUtils.Value2Text.int2bytes(innerSVCI[3]));
                        addWrappedTagValues(cache, CIEND_BYTECODE, ValueUtils.Value2Text.int2bytes(innerSVCI[4]), ValueUtils.Value2Text.int2bytes(innerSVCI[5]));
                    }
                }

                if (containAVG) {
                    Object tmpAVG = tmpInnerMergedSV.infoFeatureArray.get(1);
                    if (tmpAVG instanceof float[]) {
                        float[] tmpAVGValue = (float[]) tmpAVG;
                        addWrappedTagValue(cache, AVERAGESTART_BYTECODE, ValueUtils.Value2Text.float2bytes(tmpAVGValue[0], decimalPrecision));
                        addWrappedTagValue(cache, AVERAGELENGTH_BYTECODE, ValueUtils.Value2Text.float2bytes(tmpAVGValue[1], decimalPrecision));
                        addWrappedTagValue(cache, AVERAGEEND_BYTECODE, ValueUtils.Value2Text.float2bytes(tmpAVGValue[2], decimalPrecision));
                    }
                }
            } else {
                if (containCI) {
                    Object tmpCI = tmpInnerMergedSV.infoFeatureArray.get(0);
                    if (tmpCI instanceof int[]) {
                        int[] innerSVCI = (int[]) tmpCI;
                        addWrappedIndexTagValues(cache, CIPOS_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(innerSVCI[0]), ValueUtils.Value2Text.int2bytes(innerSVCI[1]));
                        addWrappedIndexTagValues(cache, CILEN_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(innerSVCI[2]), ValueUtils.Value2Text.int2bytes(innerSVCI[3]));
                        addWrappedIndexTagValues(cache, CIEND_BYTECODE, i + 1, ValueUtils.Value2Text.int2bytes(innerSVCI[4]), ValueUtils.Value2Text.int2bytes(innerSVCI[5]));
                    }
                }
                if (containAVG) {
                    Object tmpAVG = tmpInnerMergedSV.infoFeatureArray.get(1);
                    if (tmpAVG instanceof float[]) {
                        float[] tmpAVGValue = (float[]) tmpAVG;
                        addWrappedIndexTagValue(cache, AVERAGESTART_BYTECODE, i + 1, ValueUtils.Value2Text.float2bytes(tmpAVGValue[0], decimalPrecision));
                        addWrappedIndexTagValue(cache, AVERAGELENGTH_BYTECODE, i + 1, ValueUtils.Value2Text.float2bytes(tmpAVGValue[1], decimalPrecision));
                        addWrappedIndexTagValue(cache, AVERAGEEND_BYTECODE, i + 1, ValueUtils.Value2Text.float2bytes(tmpAVGValue[2], decimalPrecision));
                    }
                }
            }
        }
        //endregion
        // region write the AF, SupportVector, IDList, MergeMethod
        if (containAF) {
            Object AF = csvInnerSimpleSV.get(0).infoFeatureArray.get(2);
            if (AF instanceof Float) {
                float AFNumber = (float) AF;
                addWrappedTagValue(cache, AF_BYTECODE, ValueUtils.Value2Text.float2bytes(AFNumber, decimalPrecision));
            }
        }
        if (containSupportVector) {
            Object supportVector = csvInnerSimpleSV.get(0).infoFeatureArray.get(3);
            if (supportVector instanceof ByteCode) {
                ByteCode supportVectorValue = (ByteCode) supportVector;
                addWrappedTagValue(cache, SUPPVEC_BYTECODE, supportVectorValue);
            }
        }
        if (containIDList) {
            Object containIDList = csvInnerSimpleSV.get(0).infoFeatureArray.get(4);
            if (containIDList instanceof ByteCodeArray) {
                ByteCodeArray containIDListValue = (ByteCodeArray) containIDList;
                addWrappedTagValue(cache, IDLIST_BYTECODE, containIDListValue);
            }
        }
        if (containMergeName) {
            addWrappedTagValue(cache, METHOD_BYTECODE, getMergeMethodName());
        }
        //endregion
        ByteCodeArray info = firstSimpleSVInMiddleSV.getSpecificInfoField();
        if (info != null) {
            info.setAutoExpansion(true).clear();
        } else {
            info = new ByteCodeArray();
            firstSimpleSVInMiddleSV.setSpecificInfoField(info);
        }
        info.add(cache.toByteCode().asUnmodifiable());
        cache.reset();
        MergedSV res = getTmpMergeSV();
        res.sv = firstSimpleSVInMiddleSV;
    }

    public MergedSV mergeSimpleSVs(Array<UnifiedSV> simpleSVs, boolean containSupportVector, boolean containIDList, MergedSV res) {
        //region init variables
        int size = simpleSVs.size();
        UnifiedSV startSV = simpleSVs.get(0);
        UnifiedSV middleSV = simpleSVs.get(size / 2);
//        middleSV.getSpecificInfoField().clear();
        UnifiedSV endSV = simpleSVs.get(size - 1);
        int middlePos = middleSV.getPos();
        int middleEnd = middleSV.getEnd();
        int middleLen = middleSV.getLength();

        // loop for other features
        HashSet<Integer> relatedSampleSize = new HashSet<>();
        ByteCodeArray IDList = new ByteCodeArray();
        int[] posCI = new int[2];
        int[] lengthCI = new int[2];
        int[] endCI = new int[2];
        short[] supportReads = new short[SDFAMergeManager.fileSize];
        SVGenotype[] svGenotypes = new SVGenotype[SDFAMergeManager.fileSize];
        long startSum = 0;
        long endSum = 0;
        long lengthSum = 0;
        Arrays.fill(svGenotypes, SVGenotype.noneGenotye);
        //endregion
        for (UnifiedSV sv : simpleSVs) {
            if (containIDList) {
                IDList.add(sv.getID());
            }
            // here SVIndexInFile mapped to SVFileIndexMap
            int fileID = sv.getFileID();
            relatedSampleSize.add(fileID);
            supportReads[fileID]++;
            // get merged genotypes
            svGenotypes[fileID] = SVGenotype.of(Math.max(
                    svGenotypes[fileID].getByteCode(),
                    sv.getGenotypes().getGenotype(0).getByteCode())
            );
            int tmpStart = sv.getPos();
            int tmpEnd = sv.getEnd();
            int tmpLength = sv.getLength();
            // update AVG_POS, CIPOS
            startSum += tmpStart;
            if (tmpStart > middlePos) {
                posCI[1] = Math.max(posCI[1], tmpStart - middlePos);
            } else {
                posCI[0] = Math.min(posCI[0], tmpStart - middlePos);
            }
            // update AVG_END, CIEND
            endSum += tmpEnd;
            if (tmpEnd > middleEnd) {
                endCI[1] = Math.max(endCI[1], tmpEnd - middleEnd);
            } else {
                endCI[0] = Math.min(endCI[0], tmpEnd - middleEnd);
            }
            // update AVG_LEN, CILEN
            lengthSum += tmpLength;
            if (tmpLength > middleLen) {
                lengthCI[1] = Math.max(lengthCI[1], tmpLength - middleLen);
            } else {
                lengthCI[0] = Math.min(lengthCI[0], tmpLength - middleLen);
            }
        }
        double stdev_pos = 0;
        double stdev_end = 0;
        float averagePos = (float) startSum / size;
        float averageEnd = (float) endSum / size;
        float averageLen = (float) lengthSum / size;

        for (UnifiedSV simpleSV : simpleSVs) {
            stdev_pos += Math.pow(simpleSV.getPos() - averagePos, 2);
            stdev_end += Math.pow(simpleSV.getEnd() - averageEnd, 2);
        }
        stdev_pos = Math.pow(stdev_pos / size, 0.5);
        stdev_end = Math.pow(stdev_end / size, 0.5);
        Array<Object> infoFeatureArray = res.infoFeatureArray;
        // 1. CIPOS, CILEN, CIEND
        if (containCI) {
            int[] CIPosArray = new int[6];
            if (outputMeanPosFunc) {
                CIPosArray[0] = posCI[0] - (int) averagePos + middlePos;
                CIPosArray[1] = posCI[1] - (int) averagePos + middlePos;
                CIPosArray[2] = lengthCI[0] - (int) averageLen + middleLen;
                CIPosArray[3] = lengthCI[1] - (int) averageLen + middleLen;
                CIPosArray[4] = endCI[0] - (int) averageEnd + middleEnd;
                CIPosArray[5] = endCI[1] - (int) averageEnd + middleEnd;
            } else {
                CIPosArray[0] = posCI[0];
                CIPosArray[1] = posCI[1];
                CIPosArray[2] = lengthCI[0];
                CIPosArray[3] = lengthCI[1];
                CIPosArray[4] = endCI[0];
                CIPosArray[5] = endCI[1];
            }
            infoFeatureArray.set(0, CIPosArray);
        }
        // 2. AVG
        if (containAVG) {
            float[] averageArray = new float[3];
            averageArray[0] = averagePos;
            averageArray[1] = (float) lengthSum / size;
            averageArray[2] = averageEnd;
            infoFeatureArray.set(1, averageArray);
        }
        // 3. AF
        if (containAF) {
            float AF = (float) relatedSampleSize.size() / SDFAMergeManager.fileSize;
            infoFeatureArray.set(2, AF);
        }
        // 4. SUPP_VEC
        if (containSupportVector) {
            VolumeByteStream cache = res.cache;
            cache.writeSafety(combineSupportReads(supportReads));
            infoFeatureArray.set(3, cache.toByteCode().asUnmodifiable());
            cache.reset();
        }
        // 5. ID List
        if (containIDList) {
            infoFeatureArray.set(4, IDList);
        }
        // 6. STDEV_POS and STDEV_LEN
        if (containStandardDev) {
            double[] standardDev = new double[]{stdev_pos, stdev_end};
            infoFeatureArray.set(5, standardDev);
        }
        // modify the SV genotypes
        middleSV.setGenotypes(new SVGenotypes(svGenotypes));
        res.sv = middleSV;
        if (outputMeanPosFunc) {
            res.sv.setLength((int)averageLen).setCoordinate(new SVCoordinate((int) averagePos, (int) averageEnd, res.sv.getChr()));
        }
        return res;
    }

    public boolean getMergedInfo(Array<Object> mergedInfoArray, VolumeByteStream cache) {
        boolean infoChanged = false;
        // modify the info in mergedSV
        if (mergedInfoArray != null && !mergedInfoArray.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                if (mergedInfoArray.get(i) == null) {
                    continue;
                }
                infoChanged = true;
                switch (i) {
                    case 0:
                        //region CI for start, length, end
                        if ((mergedInfoArray.get(0)) instanceof int[]) {
                            int[] CIPosArray = (int[]) mergedInfoArray.get(0);
                            addWrappedTagValues(cache, CIPOS_BYTECODE, ValueUtils.Value2Text.int2bytes(CIPosArray[0]), ValueUtils.Value2Text.int2bytes(CIPosArray[1]));
                            addWrappedTagValues(cache, CILEN_BYTECODE, ValueUtils.Value2Text.int2bytes(CIPosArray[2]), ValueUtils.Value2Text.int2bytes(CIPosArray[3]));
                            addWrappedTagValues(cache, CIEND_BYTECODE, ValueUtils.Value2Text.int2bytes(CIPosArray[4]), ValueUtils.Value2Text.int2bytes(CIPosArray[5]));
                        }
                        break;
                    //endregion
                    case 1:
                        //region AVG for start, length, end
                        if (mergedInfoArray.get(1) instanceof float[]) {
                            float[] averageArray = (float[]) mergedInfoArray.get(1);
                            // AVG-Start
                            addWrappedTagValue(cache, AVERAGESTART_BYTECODE, ValueUtils.Value2Text.float2bytes(averageArray[0], decimalPrecision));
                            // AVG-Length
                            addWrappedTagValue(cache, AVERAGELENGTH_BYTECODE, ValueUtils.Value2Text.float2bytes(averageArray[1], decimalPrecision));
                            // AVG-End
                            addWrappedTagValue(cache, AVERAGEEND_BYTECODE, ValueUtils.Value2Text.float2bytes(averageArray[2], decimalPrecision));
                        }
                        break;
                    //endregion
                    case 2:
                        //region AF for mergedSV
                        if (mergedInfoArray.get(2) instanceof Float) {
                            float AF = (float) mergedInfoArray.get(2);
                            addWrappedTagValue(cache, AF_BYTECODE, ValueUtils.Value2Text.float2bytes(AF, decimalPrecision));
                            break;
                        }
                        //endregion
                    case 3:
                        //region SUPP_VEC for mergedSV
                        if (mergedInfoArray.get(3) instanceof ByteCode) {
                            addWrappedTagValue(cache, SUPPVEC_BYTECODE, (ByteCode) mergedInfoArray.get(3));
                        }
                        break;
                    //endregion
                    case 4:
                        if (mergedInfoArray.get(4) instanceof ByteCodeArray) {
                            ByteCodeArray IDList = (ByteCodeArray) mergedInfoArray.get(4);
                            addWrappedTagValue(cache, IDLIST_BYTECODE, IDList);
                        }
                        break;
                    case 5:
                        if (mergedInfoArray.get(5) instanceof double[]) {
                            double[] standardDev = (double[]) mergedInfoArray.get(5);
                            addWrappedTagValue(cache, STDEV_POS, ValueUtils.Value2Text.double2bytes(standardDev[0], decimalPrecision));
                            addWrappedTagValue(cache, STDEV_END, ValueUtils.Value2Text.double2bytes(standardDev[1], decimalPrecision));
                        }
                    default:
                        break;
                }
            }
        }
        return infoChanged;
    }

    private void addWrappedTagValue(VolumeByteStream cache, ByteCode tag, ByteCode value) {
        cache.writeSafety(tag);
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedTagValue(VolumeByteStream cache, ByteCode tag, String value) {
        cache.writeSafety(tag);
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedIndexTagValue(VolumeByteStream cache, ByteCode tag, int index, String value) {
        cache.writeSafety(tag);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(index));
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedTagValue(VolumeByteStream cache, ByteCode tag, ByteCodeArray value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        cache.writeSafety(tag);
        cache.writeSafety(ByteCode.EQUAL);
        int size = value.size();
        for (int j = 0; j < size; j++) {
            if (value.get(j) == null) {
                continue;
            }
            cache.writeSafety(value.get(j));
            if (j != size - 1) {
                cache.writeSafety(ByteCode.COMMA);
            }
        }
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedTagValue(VolumeByteStream cache, ByteCode tag, byte[] value) {
        cache.writeSafety(tag);
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedIndexTagValue(VolumeByteStream cache, ByteCode tag, int index, byte[] value) {
        cache.writeSafety(tag);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(index));
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedTagValues(VolumeByteStream cache, ByteCode tag, byte[] value1, byte[] value2) {
        cache.writeSafety(tag);
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value1);
        cache.writeSafety(ByteCode.COMMA);
        cache.writeSafety(value2);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private void addWrappedIndexTagValues(VolumeByteStream cache, ByteCode tag, int index, byte[] value1, byte[] value2) {
        cache.writeSafety(tag);
        cache.writeSafety(ValueUtils.Value2Text.int2bytes(index));
        cache.writeSafety(ByteCode.EQUAL);
        cache.writeSafety(value1);
        cache.writeSafety(ByteCode.COMMA);
        cache.writeSafety(value2);
        cache.writeSafety(ByteCode.SEMICOLON);
    }

    private synchronized String combineSupportReads(short[] supports) {
        builder.setLength(0);
        for (short support : supports) {
            builder.append(support);
        }
        return builder.toString();
    }
}
