package edu.sysu.pmglab.test.ngf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-06 01:27
 * @description
 */
public class FortySixSamplesGeneLevel {
    static Entry<Boolean, Byte> empty = new Entry<>(null, (byte) 0);
    static ByteCode coding = new ByteCode("coding");
    static ByteCode noncoding = new ByteCode("non-coding");

    public static void main(String[] args) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        VolumeByteStream cache1 = new VolumeByteStream();
        FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/record/gene_summary.txt");
        FileStream fs1 = new FileStream("/Users/wenjiepeng/Desktop/paper_res/ngf/46samples_geneName_level.txt", FileStream.DEFAULT_WRITER);
        fs1.write("gene\tSV_Quantification_On_Gene\tcoding\n");
        int count = 1;
        while (fs.readLine(cache) != -1) {
            ByteCode line = cache.toByteCode();
            if (line.length() == 0) {
                cache.reset();
                continue;
            }
            BaseArray<ByteCode> split = line.split(ByteCode.TAB);
            Boolean flag = null;
            ByteArray featureArray = new ByteArray();
            for (int i = 4; i < split.size(); i++) {
                ByteCode byteCode = split.get(i);
                if (byteCode.length() != 0) {
                    Entry<Boolean, Byte> parse = parse(byteCode, 5, 5, 5, 5, 5);
                    if (!Boolean.TRUE.equals(flag) && parse.getKey() != null) {
                        flag = parse.getKey();
                    }
                    featureArray.add(parse.getValue());
                }
            }
            for (int i = 0; i < featureArray.size(); i++) {
                fs1.write(split.get(1));
                fs1.write(ByteCode.TAB);
                fs1.write(ValueUtils.Value2Text.int2bytes(Boolean.TRUE.equals(flag) ?Math.abs(featureArray.get(i)):featureArray.get(i)));
                fs1.write(ByteCode.TAB);
                fs1.write(Boolean.TRUE.equals(flag) ? coding : noncoding);
                fs1.write(ByteCode.NEWLINE);
            }
            count++;
            cache.reset();
        }
    }

    public static Entry<Boolean, Byte> parse(ByteCode src, int i1, int i2, int i3, int i4, int i5) {
        ByteArray csvChrIndex = new ByteArray(6);
        if (src.startsWith(ByteCode.PERIOD)) {
            return empty;
        }
        BaseArray<ByteCode> split = src.split("[");
        byte feature = split.get(0).toByte();
        csvChrIndex.add(feature);
        if (feature == 0) {
            return empty;
        }
        Boolean flag = null;
        if (feature > 0) {
            flag = true;
        }
        if (feature < 0) {
            flag = false;
        }
        if (feature <= 3 && feature >= 0) {
            flag = null;
        }
        BaseArray<ByteCode> split1 = split.get(1).split(ByteCode.COMMA);
        for (int i = 0; i < 4; i++) {
            csvChrIndex.add(split1.get(i).toByte());
        }
        csvChrIndex.add(split1.get(split1.size() - 1).split("]").get(0).toByte());
        boolean sign = csvChrIndex.get(0) < 0;
        csvChrIndex.set(0, (byte) Math.abs(feature));
        if (csvChrIndex.get(1) < i1) {
            csvChrIndex.set(0, (byte) (csvChrIndex.get(0) & ~0b01000000));
        }
        if (csvChrIndex.get(2) < i2) {
            csvChrIndex.set(0, (byte) (csvChrIndex.get(0) & ~0b100000));
        }
        if (csvChrIndex.get(3) < i3) {
            csvChrIndex.set(0, (byte) (csvChrIndex.get(0) & ~0b10000));
        }
        if (csvChrIndex.get(4) < i4) {
            csvChrIndex.set(0, (byte) (csvChrIndex.get(0) & ~0b1000));
        }
        if (csvChrIndex.get(5) < i5) {
            csvChrIndex.set(0, (byte) (csvChrIndex.get(0) & ~0b100));
        }
        csvChrIndex.set(0, sign ? (byte) -csvChrIndex.get(0) : (byte) csvChrIndex.get(0));
        return new Entry<>(flag, csvChrIndex.get(0));
    }
}
