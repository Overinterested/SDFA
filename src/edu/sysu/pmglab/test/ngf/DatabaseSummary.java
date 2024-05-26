package edu.sysu.pmglab.test.ngf;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-05-05 23:02
 * @description
 */
public class DatabaseSummary {
    public static void main(String[] args) throws IOException {
        boolean forOne = false;
        VolumeByteStream cache = new VolumeByteStream();
        File file = new File("/Users/wenjiepeng/Desktop/SV/AnnotFile/SVAFotate_core_SV_popAFs.GRCh38.bed.sdfa.ngf.txt");
        Array<HashMap<ByteCode, Array<Entry<Byte, Float>>>> groupByDatabase = new Array<>();
        for (int i = 0; i < 3; i++) {
            groupByDatabase.add(new HashMap<>());
        }
        VolumeByteStream cache1 = new VolumeByteStream();
        FileStream fs = new FileStream(file, FileStream.DEFAULT_READER);
        int line = -2;
        FileStream raw = new FileStream("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/SVAFotate_core_SV_popAFs.GRCh38.bed", FileStream.DEFAULT_READER);
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
            int recordLine = split.get(1).toInt();
            while (recordLine != line) {
                cache1.reset();
                raw.readLine(cache1);
                line++;
            }
            // get database name
            BaseArray<ByteCode> split1 = cache1.toByteCode().split(ByteCode.TAB);
            int groupIndex = getGroupIndex(split1.get(5));
            float af;
            try {
                af = split1.get(7).toFloat();
            } catch (Exception x) {
                cache.reset();
                continue;
            }
            HashMap<ByteCode, Array<Entry<Byte, Float>>> byteCodeArrayHashMap = groupByDatabase.get(groupIndex);
            for (int i = 4; i < split.size(); i++) {
                ByteCode tmp = split.get(i);
                if (tmp.length() == 0) {
                    continue;
                }
                BaseArray<ByteCode> item = tmp.split(ByteCode.COLON);
                ByteCode geneName = item.get(0).asUnmodifiable();
                byte ngfValue = item.get(1).toByte();
                Array<Entry<Byte, Float>> entries = byteCodeArrayHashMap.get(geneName);
                if (entries == null) {
                    Array<Entry<Byte, Float>> x = new Array<>();
                    x.add(new Entry<>(ngfValue, af));
                    byteCodeArrayHashMap.put(geneName, x);
                } else {
                    entries.add(new Entry<>(ngfValue, af));
                }
            }
            cache.reset();
        }
        fs.close();
        raw.close();
        fs = new FileStream("/Users/wenjiepeng/Desktop/paper_res/ngf/database.txt",FileStream.DEFAULT_WRITER);
        for (int i = 0; i < 3; i++) {
            HashMap<ByteCode, Array<Entry<Byte, Float>>> byteCodeArrayHashMap = groupByDatabase.get(i);
            byte[] groupID = ValueUtils.Value2Text.int2bytes(i);
            for (ByteCode geneName : byteCodeArrayHashMap.keySet()) {
                fs.write(groupID);
                fs.write(ByteCode.TAB);
                fs.write(geneName);
                fs.write(ByteCode.TAB);
                Array<Entry<Byte, Float>> entries = byteCodeArrayHashMap.get(geneName);
                for (Entry<Byte, Float> entry : entries) {
                    fs.write(ValueUtils.Value2Text.byte2bytes(entry.getKey()));
                    fs.write(ByteCode.COLON);
                    fs.write(ValueUtils.Value2Text.float2bytes(entry.getValue()));
                    fs.write(ByteCode.TAB);
                }
                fs.write(ByteCode.NEWLINE);
            }
        }
        fs.close();
    }

    public static int getGroupIndex(ByteCode src) {
        if (src.startsWith(new byte[]{ByteCode.C, ByteCode.C, ByteCode.D, ByteCode.G})) {
            return 0;
        }
        if (src.startsWith(new byte[]{ByteCode.ONE, ByteCode.ZERO, 48, 48, ByteCode.G})) {
            return 1;
        }
        if (src.startsWith(new byte[]{ByteCode.g, ByteCode.n, ByteCode.o, ByteCode.m})) {
            return 2;
        }
        throw new UnsupportedOperationException();
    }
}
