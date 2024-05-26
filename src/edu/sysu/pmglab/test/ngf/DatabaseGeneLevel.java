package edu.sysu.pmglab.test.ngf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-05 23:47
 * @description
 */
public class DatabaseGeneLevel {
    public static void main(String[] args) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/record/gene_summary.txt", FileStream.DEFAULT_READER);
        while (fs.readLine(cache) != -1) {
            if (cache.size() == 0){
                cache.reset();
                continue;
            }
            BaseArray<ByteCode> split = cache.toByteCode().split(ByteCode.TAB);

        }
    }
}
