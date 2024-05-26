package edu.sysu.pmglab.test.analyses;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-20 07:50
 * @description
 */
public class MergeComparison {
    static byte[] SUPP_BYTES = "SUPP".getBytes();
    static byte[] SUPP_EQUAL_BYTES = "SUPP=".getBytes();
    static byte[] SUPPORT_BYTES = "NUM_MERGED_SVS".getBytes();
    static byte[] NUMCONSOLIDATED_BYTES = "NumConsolidated".getBytes();

    public static void main(String[] args) throws IOException {
        String[] mergedTypes = new String[]{"cutesv", "sniff"};
        String[] callTypes = new String[]{"jasmine", "sdfa", "survivor", "svimmer", "truvira"};
        String[] files = new String[]{
                "/Users/wenjiepeng/Desktop/SDFA/merge/jasmine/jasmine_46_cutesv_merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/jasmine/jasmine_46_sniffles_merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/sdfa/cutesv_46_sample_merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/sdfa/sniffles_46_samples_merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/survivor/survivor_46_cutesv2_merge_vcf.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/survivor/survivor_46_sniffles_merge_vcf.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/svimmer/svimmer_cutesv_46.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/svimmer/svimmer_output_sniffles_46.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/truvira/cutesv_truvari_merge.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/merge/truvira/sniffiles_truvari_merge.vcf"
        };
        FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/SDFA/merge/summary.txt", FileStream.DEFAULT_WRITER);
        fs.write("#CallType\tMergedMethod\tChr\tPos\tEnd\tType\tLength\tCIPos" +
                "\tCIEnd\tCILEN\tJasmineSTDEV_POS\tJasmineSTDEV_END\tsvimmer_STDEV_POS\tsvimmer_STDEV_END\tSDFA_svimmer_STDEV_POS\tSDFA_svimmer_STDEV_END");
        fs.write(ByteCode.NEWLINE);
        for (String file : files) {
            FileStream fs1 = new FileStream(file);
            VolumeByteStream cache = new VolumeByteStream();
            while (fs1.readLine(cache) != -1) {
                if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                    cache.reset();
                    continue;
                }
                String callType = getType(file, callTypes);
                String mergeType = getType(file, mergedTypes);
                ByteCode line = cache.toByteCode();
                BaseArray<ByteCode> split = line.split(ByteCode.TAB);
                ByteCode info = split.get(7);
                BaseArray<ByteCode> infoSplit = info.split(ByteCode.SEMICOLON);
                boolean merged = isMerged(callType, info, info.split(ByteCode.SEMICOLON));
                if (merged) {
                    ByteCode chr = split.get(0);
                    ByteCode pos = split.get(1);
                    ByteCode end = getInfoValue(infoSplit, new ByteCode(new byte[]{ByteCode.E, ByteCode.N, ByteCode.D}));
                    ByteCode type = getInfoValue(infoSplit, new ByteCode("SVTYPE"));
                    ByteCode length = getInfoValue(infoSplit, new ByteCode("SVLEN"));
                    ByteCode CIPos = getInfoValue(infoSplit, new ByteCode("CIPOS"));
                    ByteCode CIEnd = getInfoValue(infoSplit, new ByteCode("CIEND"));
                    ByteCode CILen = getInfoValue(infoSplit, new ByteCode("CILEN"));
                    ByteCode jasmineStandardDevPos = getInfoValue(infoSplit, new ByteCode("STARTVARIANCE"));
                    ByteCode jasmineStandardDevEnd = getInfoValue(infoSplit, new ByteCode("ENDVARIANCE"));
                    ByteCode svimmerStandardDev = getInfoValue(infoSplit, new ByteCode("STDDEV_POS"));
                    ByteCode svimmerStandardDevPos = new ByteCode(new byte[]{ByteCode.PERIOD});
                    ByteCode svimmerStandardDevEnd = new ByteCode(new byte[]{ByteCode.PERIOD});
                    if (!svimmerStandardDev.equals(ByteCode.PERIOD)) {
                        svimmerStandardDevPos = svimmerStandardDev.split(ByteCode.COMMA).get(0).asUnmodifiable();
                        svimmerStandardDevEnd = svimmerStandardDev.split(ByteCode.COMMA).get(1).asUnmodifiable();
                    }
                    ByteCode sdfaStandardDevPos = getInfoValue(infoSplit, new ByteCode("STDEV_POS"));
                    ByteCode sdfaStandardDevEnd = getInfoValue(infoSplit, new ByteCode("STDEV_END"));
                    fs.write(callType);
                    fs.write(ByteCode.TAB);
                    fs.write(mergeType);
                    fs.write(ByteCode.TAB);
                    fs.write(chr);
                    fs.write(ByteCode.TAB);
                    fs.write(pos);
                    fs.write(ByteCode.TAB);
                    fs.write(end);
                    fs.write(ByteCode.TAB);
                    fs.write(type);
                    fs.write(ByteCode.TAB);
                    fs.write(length);
                    fs.write(ByteCode.TAB);
                    fs.write(CIPos);
                    fs.write(ByteCode.TAB);
                    fs.write(CIEnd);
                    fs.write(ByteCode.TAB);
                    fs.write(CILen);
                    fs.write(ByteCode.TAB);
                    fs.write(jasmineStandardDevPos);
                    fs.write(ByteCode.TAB);
                    fs.write(jasmineStandardDevEnd);
                    fs.write(ByteCode.TAB);
                    fs.write(svimmerStandardDevPos);
                    fs.write(ByteCode.TAB);
                    fs.write(svimmerStandardDevEnd);
                    fs.write(ByteCode.TAB);
                    fs.write(sdfaStandardDevPos);
                    fs.write(ByteCode.TAB);
                    fs.write(sdfaStandardDevEnd);
                    fs.write(ByteCode.NEWLINE);
                }
                cache.reset();
            }
        }
        fs.close();

    }

    static String getType(String fileName, String[] types) {
        for (String type : types) {
            if (fileName.contains(type)) {
                return type;
            }
        }
        return ".";
    }

    static ByteCode getInfoValue(BaseArray<ByteCode> infoSplit, ByteCode key) {
        for (ByteCode infoField : infoSplit) {
            if (infoField.startsWith(key)) {
                BaseArray<ByteCode> split = infoField.split(ByteCode.EQUAL);
                return split.get(1).asUnmodifiable();
            }
        }
        return new ByteCode(new byte[]{ByteCode.PERIOD});
    }

    static boolean isMerged(String call, ByteCode info, BaseArray<ByteCode> splitInfo) {
        switch (call) {
            case "jasmine":
                if (info.indexOf(SUPP_BYTES) != -1) {
                    for (ByteCode infoItem : splitInfo) {
                        if (infoItem.startsWith(SUPP_EQUAL_BYTES)) {
                            return infoItem.split("=").get(1).toInt() != 1;
                        }
                    }
                }
                break;
            case "sdfa":
                if (info.indexOf(SUPP_BYTES) != -1) {
                    for (ByteCode infoItem : splitInfo) {
                        if (infoItem.startsWith(SUPP_BYTES)) {
                            int count = 0;
                            for (int i = 0; i < infoItem.length(); i++) {
                                if (infoItem.byteAt(i) == ByteCode.ONE) {
                                    count++;
                                }
                            }
                            return count != 1;
                        }
                    }
                }
                break;
            case "survivor":
                if (info.indexOf(SUPP_EQUAL_BYTES) != -1) {
                    for (ByteCode infoItem : splitInfo) {
                        if (infoItem.indexOf(SUPP_BYTES) != -1) {
                            return infoItem.split("=").get(1).toInt() != 1;
                        }
                    }
                }
                break;
            case "svimmer":
                if (info.indexOf(SUPP_EQUAL_BYTES) != -1) {
                    for (ByteCode infoItem : splitInfo) {
                        if (infoItem.startsWith(SUPPORT_BYTES)) {
                            return infoItem.split("=").get(1).toInt() != 1;
                        }
                    }
                }
                break;
            case "truvira":
                String string = info.toString();
                if (string.contains("NumConsolidated")) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }
}
