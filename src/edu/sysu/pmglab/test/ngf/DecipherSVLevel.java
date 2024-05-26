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
 * @create 2024-05-12 12:25
 * @description
 */
public class DecipherSVLevel {
    public static void main(String[] args) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        File file = new File("/Users/wenjiepeng/Desktop/SV/data/private/dataset/global_developmental_delay/cuteSV.vcf.sdfa.ngf.txt");
        FileStream fs = new FileStream(file);
        FileStream output = new FileStream(
                "/Users/wenjiepeng/Desktop/SV/data/private/dataset/global_developmental_delay/gene_count_summary.txt",
                FileStream.DEFAULT_WRITER
        );
        HashMap<ByteCode, Integer> geneNameSVCountMap = new HashMap<>();
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                cache.reset();
                continue;
            }
            if (cache.size() == 0) {
                cache.reset();
                continue;
            }
            BaseArray<ByteCode> split = cache.toByteCode().split(ByteCode.TAB);
            for (int i = 6; i < split.size(); i++) {
                ByteCode tmp = split.get(i);
                if (tmp.length() == 0) {
                    continue;
                }
                BaseArray<ByteCode> item = tmp.split(ByteCode.COLON);
                ByteCode geneName = item.get(0).asUnmodifiable();
                if (geneNameSVCountMap.containsKey(geneName)) {
                    geneNameSVCountMap.put(geneName, geneNameSVCountMap.get(geneName) + 1);
                } else {
                    geneNameSVCountMap.put(geneName, 1);
                }
            }
        }
        output.write("gene\tcount\n");
        for (ByteCode geneName : geneNameSVCountMap.keySet()) {
            output.write(geneName);
            output.write(ByteCode.TAB);
            output.write(ValueUtils.Value2Text.int2bytes(geneNameSVCountMap.get(geneName)));
            output.write(ByteCode.NEWLINE);
        }
        output.close();
        fs.close();
    }
}
