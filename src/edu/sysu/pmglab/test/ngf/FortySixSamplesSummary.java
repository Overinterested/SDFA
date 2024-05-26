package edu.sysu.pmglab.test.ngf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-05-05 02:55
 * @description
 */
public class FortySixSamplesSummary {
    public static void main(String[] args) throws IOException {
        File dir = new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/46_samples_ngf_lncRNA_0.5_threshold");
        Array<File> filesFromDir = FileTool.getFilesFromDir(dir, "ngf.txt");
        FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/46_samples_ngf_lncRNA_0.5_threshold/type_file_SV_count.txt", FileStream.DEFAULT_WRITER);
        fs.write("#FileID\tType\tSVCount\n");
        HashMap<ByteCode, Integer> SVTypeSVCountMap = new HashMap<>();
        int fileID = 0;
        for (File file : filesFromDir) {
            VolumeByteStream cache = new VolumeByteStream();
            FileStream fs1 = new FileStream(file, FileStream.DEFAULT_READER);
            while (fs1.readLine(cache) != -1) {
                if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                    cache.reset();
                    continue;
                }
                if (cache.size() == 0) {
                    cache.reset();
                    continue;
                }
                BaseArray<ByteCode> split = cache.toByteCode().split(ByteCode.TAB);
                ByteCode typeOfSV = split.get(5);
                if (SVTypeSVCountMap.containsKey(typeOfSV)) {
                    SVTypeSVCountMap.put(typeOfSV.asUnmodifiable(), SVTypeSVCountMap.get(typeOfSV) + 1);
                } else {
                    SVTypeSVCountMap.put(typeOfSV.asUnmodifiable(), 1);
                }
                cache.reset();
            }
            for (ByteCode type : SVTypeSVCountMap.keySet()) {
                fs.write(ValueUtils.Value2Text.int2bytes(fileID));
                fs.write(ByteCode.TAB);
                fs.write(type);
                fs.write(ByteCode.TAB);
                fs.write(ValueUtils.Value2Text.int2bytes(SVTypeSVCountMap.get(type)));
                fs.write(ByteCode.NEWLINE);
            }
            fs1.close();
            fileID++;
        }
        fs.close();
    }
}
