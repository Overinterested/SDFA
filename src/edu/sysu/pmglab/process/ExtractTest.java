package edu.sysu.pmglab.process;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.executor.Context;
import edu.sysu.pmglab.unifyIO.BGZIPReaderStream;
import edu.sysu.pmglab.unifyIO.FileStream;
import sun.rmi.transport.LiveRef;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2025-06-05 07:31
 * @description
 */
public class ExtractTest {
    public static final ByteCode TAG = new ByteCode("Truvari");

    //    /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/survivor/one_sample_vcf.vcf
//    /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/sdfa/merged.vcf
//    /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/jasmine/merged.vcf
//    /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/svimmer/svimmer_merged.vcf
    public static void main(String[] args) throws IOException {

        String[] input = new String[]{
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/survivor/one_sample_vcf.vcf",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/sdfa/merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/jasmine/merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/svimmer/svimmer_merged.vcf",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_truvari_merged.vcf"
        };
        String[] output = new String[]{
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/survivor_sv.txt",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/sdfa_sv.txt",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/jasmine_sv.txt",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/svimmer_sv.txt",
                "/Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari_sv.txt",
        };
        for (int j = 0; j < input.length; j++) {
            String file = input[j];
            ByteCode TAG;
            switch (j){
                case 0:
                    TAG = new ByteCode("survivor");
                    break;
                case 1:
                    TAG = new ByteCode("sdfa");
                    break;
                case 2:
                    TAG = new ByteCode("jasmine");
                    break;
                case 3:
                    TAG = new ByteCode("svimmer");
                    break;
                case 4:
                    TAG = new ByteCode("truvari");
                    break;
                default:
                    TAG = new ByteCode("NONE");
            }
            String outputFile = output[j];

            ByteCode number = new ByteCode("#");
            VolumeByteStream cache = new VolumeByteStream();
            FileStream reader = new FileStream(file, FileStream.DEFAULT_READER);
            FileStream writer = new FileStream(outputFile, FileStream.DEFAULT_WRITER);
            while (reader.readLine(cache) != -1) {
                if (cache.startWith(number.byteAt(0))) {
                    cache.reset();
                    continue;
                }
                ByteCode line = cache.toByteCode();
                BaseArray<ByteCode> split = line.split("\t");
                ByteCode info = split.get(7);
                BaseArray<ByteCode> items = info.split(";");
                writer.write(TAG);
                writer.write(ByteCode.TAB);
                writer.write(split.get(0));
                writer.write(ByteCode.TAB);
                ByteCode pos = split.get(1);
                writer.write(pos);
                writer.write(ByteCode.TAB);
                boolean flag = false;
                ByteCode type = null, end = null;
                for (int i = 0; i < items.size(); i++) {
                    ByteCode item = items.get(i);
                    if (item.startsWith("SVTYPE")) {
                        type = item.split("=").get(1);
                        continue;
                    }
                    if (item.startsWith("END=")) {
                        end = item.split("=").get(1);
                        continue;
                    }
                }
                if (end == null) {
                    writer.write(new ByteCode(String.valueOf(pos.toInt() + 1)));
                    writer.write(ByteCode.TAB);
                }else{
                    if (type.equals(new ByteCode("INS"))){
                        writer.write(new ByteCode(String.valueOf(pos.toInt() + 1)));
                    }else
                        writer.write(end);

                    writer.write(ByteCode.TAB);
                }
                writer.write(type != null ? type : new ByteCode("None"));
                writer.write(ByteCode.NEWLINE);
                cache.reset();
            }
            cache.close();
            reader.close();
            writer.close();
        }
    }
}
