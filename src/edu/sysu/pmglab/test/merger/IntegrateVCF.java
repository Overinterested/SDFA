package edu.sysu.pmglab.test.merger;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-06-23 07:35
 * @description
 */
public class IntegrateVCF {
    public static void main(String[] args) throws IOException {
        FileStream HG002 = new FileStream("/Users/wenjiepeng/Desktop/SV/SVMerge/HG002_chr3.vcf", FileStream.DEFAULT_WRITER);
        FileStream HG003 = new FileStream("/Users/wenjiepeng/Desktop/SV/SVMerge/HG003_chr3.vcf", FileStream.DEFAULT_WRITER);
        FileStream HG004 = new FileStream("/Users/wenjiepeng/Desktop/SV/SVMerge/HG004_chr3.vcf", FileStream.DEFAULT_WRITER);
        Array<FileStream> fsArray = new Array<>(new FileStream[]{HG002, HG003, HG004});
        Array<Boolean> headerFlagArray = new Array<>(new Boolean[]{Boolean.FALSE, Boolean.FALSE, Boolean.FALSE});
        File dir = new File("/Users/wenjiepeng/Desktop/SV/SVMerge/trio");
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File subDir : files) {
                if (subDir.isDirectory()) {
                    File[] allFiles = subDir.listFiles();
                    if (allFiles != null && allFiles.length != 0) {
                        for (File file : allFiles) {
                            int fsIndex = -1;
                            fsIndex = file.getName().contains("HG002") ? 0 : fsIndex;
                            fsIndex = file.getName().contains("HG003") ? 1 : fsIndex;
                            fsIndex = file.getName().contains("HG004") ? 2 : fsIndex;
                            if (fsIndex == -1) {
                                continue;
                            }
                            FileStream fs = fsArray.get(fsIndex);
                            FileStream reader = new FileStream(file, FileStream.DEFAULT_READER);
                            VolumeByteStream cache = new VolumeByteStream();
                            while (reader.readLine(cache) != -1) {
                                if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                                    if (!headerFlagArray.get(fsIndex)) {
                                        fs.write(cache.toByteCode());
                                        fs.write(ByteCode.NEWLINE);
                                        cache.reset();
                                        continue;
                                    }
                                    continue;
                                }
                                headerFlagArray.set(fsIndex, true);
                                if (cache.toByteCode().startsWith("chr3")) {
                                    ByteCode byteCode = cache.toByteCode();
                                    fs.write(cache.toByteCode());
                                    fs.write(ByteCode.NEWLINE);
                                }
                                cache.reset();
                            }
                            cache.close();
                            reader.close();
                        }
                    }
                }
            }
        }
        for (FileStream fileStream : fsArray) {
            fileStream.close();
        }

    }
}
