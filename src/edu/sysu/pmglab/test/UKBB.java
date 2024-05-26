package edu.sysu.pmglab.test;

import edu.sysu.pmglab.sdfa.command.SDFAEntry;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 01:22
 * @description
 */
public class UKBB {
    public static void main(String[] args) throws IOException {
        VCF2SDF.multiInfoForCSV = true;
        long l = System.currentTimeMillis();
        SDFAEntry.main(("vcf2sdf -dir " +
                "/Users/wenjiepeng/Desktop/tmp/ukb -o " +
                "/Users/wenjiepeng/Desktop/SV/test/tmp " +
                "-c 1 --pool--threshold 200000,500000,700000 --pool").split(" "));
        System.out.println(System.currentTimeMillis()-l);
//        FileStream fs = new FileStream("/Users/wenjiepeng/Desktop/tmp/ukb/ukbb.vcf.gz");
//        VolumeByteStream cache = new VolumeByteStream();
//        int count = 0;
//        while(fs.readLine(cache)!=-1){
//            if (!cache.startWith(ByteCode.NUMBER_SIGN)){
//                count++;
//            }
//            cache.reset();
//        }
//        System.out.println(count);
    }
}
