package edu.sysu.pmglab.test.extract;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-25 03:47
 * @description
 */
public class FilterCount {
    public static void main(String[] args) throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream("/Users/wenjiepeng/Downloads/1.log");
        int size = 0;
        while (fs.readLine(cache) != -1) {
            ByteCode byteCode = cache.toByteCode();
            BaseArray<ByteCode> split = byteCode.split(" ");
            int index = -1;
            for (int i = 0; i < split.size(); i++) {
                ByteCode code = split.get(i);
                if (code.equals(new ByteCode("genotypes"))) {
                    index = i;
                    break;
                }
            }
            size += split.get(index - 1).toInt();
            cache.reset();
        }
        System.out.println(size);
    }
}
