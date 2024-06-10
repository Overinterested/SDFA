package edu.sysu.pmglab.test;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 01:22
 * @description
 */
public class UKBB {
    public static void main(String[] args) throws IOException {
        byte feature = (byte) 0b00001000;
        System.out.println((byte)(feature|0b10000000));
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
