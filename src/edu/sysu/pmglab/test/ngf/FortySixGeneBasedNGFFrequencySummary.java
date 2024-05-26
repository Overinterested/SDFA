package edu.sysu.pmglab.test.ngf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-05-14 08:55
 * @description
 */
public class FortySixGeneBasedNGFFrequencySummary {
    public static void main(String[] args) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        File file = new File("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/46_sample_ngf_exon_0.05_AF_0.1/gene_summary.txt");
        FileStream fs = new FileStream(file);
        FileStream output = new FileStream(
                "/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sample_level_exon>5/AF.count",
                FileStream.DEFAULT_WRITER
        );
        HashMap<Integer, Integer> AFCount = new HashMap<>();
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                cache.reset();
                continue;
            }
            if (cache.size() == 0) {
                continue;
            }
            BaseArray<ByteCode> split = cache.toByteCode().split(ByteCode.TAB);
            int count = 0;
            for (int i = 4; i < split.size(); i++) {
                ByteCode tmp = split.get(i);
                if (!tmp.equals(new ByteCode(new byte[]{ByteCode.PERIOD}))) {
                    count++;
                }
            }
            if (count == 0){
                int a = 1;
            }
            if (AFCount.containsKey(count)) {
                AFCount.put(count, AFCount.get(count) + 1);
            } else {
                AFCount.put(count, 1);
            }
            cache.reset();
        }
        output.write("#AF\tCount\n");
        for (Integer af : AFCount.keySet()) {
            output.write(ValueUtils.Value2Text.float2bytes(af));
            output.write(ByteCode.TAB);
            output.write(ValueUtils.Value2Text.int2bytes(AFCount.get(af)));
            output.write(ByteCode.NEWLINE);
        }
        output.close();
    }
}
