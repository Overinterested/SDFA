package edu.sysu.pmglab.sdfa;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.viewer.CCFViewer;
import edu.sysu.pmglab.ccf.viewer.CCFViewerReader;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.ArrayType;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.container.array.ShortArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.AbstractCallingParser;
import edu.sysu.pmglab.sdfa.sv.vcf.calling.SvisionCallingParser;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 04:22
 * @description
 */
public class SDFViewer {
    public static void view(String sdfFile) throws IOException {
        view(new File(sdfFile));
    }

    public static void view(File sdfFile) throws IOException {
        new CCFViewer(new SDFViewerReader(new SDFReader(sdfFile)), 20, false);
    }

    public static void view(File sdfFile, int pageSize, boolean compatible) throws IOException {
        new CCFViewer(new SDFViewerReader(new SDFReader(sdfFile)), pageSize, compatible);
    }

    static class SDFViewerReader extends CCFViewerReader {
        SDFMeta meta;
        int fieldSize;
        CCFReader reader;
        CallableSet<ByteCode> infoSet;
        CallableSet<ByteCode> formatSet;
        ContigBlockContainer contigBlockContainer;
        VolumeByteStream chrCache = new VolumeByteStream();
        VolumeByteStream gtyCache = new VolumeByteStream();
        VolumeByteStream infoCache = new VolumeByteStream();
        VolumeByteStream formatCache = new VolumeByteStream();
        static final ByteCode TRUE = new ByteCode("TRUE");
        static final ByteCode FALSE = new ByteCode("FALSE");
        static final ByteCode CHR_BYTECODE = new ByteCode("CHR");

        public SDFViewerReader(SDFReader reader) {
            super(reader.reader);
            this.reader = reader.getReader();
            SDFMeta decode = SDFMeta.decode(reader);
            infoSet = decode.infoFieldSet;
            this.formatSet = decode.formatFieldSet;
            this.fieldSize = this.reader.numOfFields();
            contigBlockContainer = decode.contigBlockContainer;
            CallableSet<ByteCode> uniqueInfoSet = new CallableSet<>(infoSet.size());
            for (ByteCode infoItem : infoSet) {
                if (infoItem.startsWith(AbstractCallingParser.END_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.CHR_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.CHR2_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.POS_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.POS2_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.SVLEN_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.END2_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.SVTYPE_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(AbstractCallingParser.SVTYPE2_BYTECODE)) {
                    continue;
                }
                if (infoItem.startsWith(SvisionCallingParser.BKPS_BYTECODE)) {
                    continue;
                }
                uniqueInfoSet.add(infoItem);
            }
            this.infoSet = uniqueInfoSet;
        }

        @Override
        public Object[] read() throws IOException {
            int index = 0;
            IRecord record;
            if ((record = this.reader.read()) == null) {
                return null;
            } else {
                Object[] objects = new Object[fieldSize + 1];
                objects[index++] = this.reader.tell() - 1L;
                for (CCFFieldMeta field : reader.getAllFields()) {
                    int recordIndex = index - 1;
                    switch (index - 1) {
                        case 0:
                            chrCache.reset();
                            int[] coordinate = record.get(recordIndex);
                            chrCache.writeSafety(contigBlockContainer.getChromosomeByIndex(coordinate[0]).getName());
                            chrCache.writeSafety(ByteCode.COLON);
                            chrCache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[1]));
                            chrCache.writeSafety(ByteCode.MINUS);
                            if (coordinate[2] == -1) {
                                chrCache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[1] + 1));
                            } else {
                                chrCache.writeSafety(ValueUtils.Value2Text.int2bytes(coordinate[2]));
                            }
                            objects[index++] = chrCache.toUnmodifiableByteCode().toString();
                            break;
                        case 1:
                            objects[index++] = String.valueOf((int) record.get(recordIndex));
                            break;
                        case 2:
                            objects[index++] = SVTypeSign.getByIndex(record.get(2)).getName();
                            break;
                        case 3:
                            // gty
                            gtyCache.reset();
                            ShortArray encodeGtys = ShortArray.wrap(record.get(3));
                            for (short gty : encodeGtys) {
                                gtyCache.writeSafety(SVGenotype.of(gty).toString());
                                gtyCache.writeSafety(ByteCode.SEMICOLON);
                            }
                            objects[index++] = gtyCache.toUnmodifiableByteCode();
                            break;
                        case 4:
                            // FORMAT:
                            formatCache.reset();
                            ByteCodeArray formatArray = ByteCodeArray.wrap((ByteCode[]) record.get(4));
                            for (int i = 0; i < formatArray.size(); i++) {
                                ByteCode formatField = formatArray.get(i);
                                if (formatField.equals(ByteCode.EMPTY)) {
                                    continue;
                                }
                                ByteCodeArray tmp = (ByteCodeArray) ArrayType.decode(formatField);
                                for (int j = 0; j < tmp.size(); j++) {
                                    ByteCode item = tmp.get(j);
                                    if (item.equals(ByteCode.EMPTY)) {
                                        tmp.set(j, FALSE);
                                    }
                                }
                                formatCache.writeSafety(formatSet.getByIndex(i + 1));
                                formatCache.writeSafety(ByteCode.COLON);
                                formatCache.writeSafety(tmp.toString());
                                formatCache.writeSafety(ByteCode.SEMICOLON);
                            }
                            objects[index++] = formatCache.toByteCode().asUnmodifiable();
                            break;
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 11:
                            objects[index++] = record.get(recordIndex).toString();
                            break;
                        case 10:
                            infoCache.reset();
                            ByteCodeArray infoValues = ByteCodeArray.wrap((ByteCode[]) record.get(10));
                            if (infoValues.isEmpty()) {
                                objects[index++] = ByteCode.EMPTY;
                                continue;
                            }
                            for (int i = 0; i < infoValues.size(); i++) {
                                ByteCode tmp = infoValues.get(i);
                                if (tmp.equals(ByteCode.EMPTY)) {
                                    continue;
                                }
                                if (tmp.equals(new byte[]{ByteCode.PERIOD})) {
                                    infoCache.writeSafety(infoSet.getByIndex(i));
                                    infoCache.writeSafety(ByteCode.SEMICOLON);
                                }
                                ByteCode infoKey = infoSet.getByIndex(i);
                                infoCache.writeSafety(infoKey);
                                infoCache.writeSafety(ByteCode.EQUAL);
                                infoCache.writeSafety(tmp);
                                infoCache.writeSafety(ByteCode.SEMICOLON);
                            }
                            objects[index++] = infoCache.toByteCode().asUnmodifiable();
                            break;
                        case 12:
                            IntArray csvChrIndex = IntArray.wrap(record.get(12));
                            if (csvChrIndex.isEmpty()) {
                                objects[index++] = ByteCode.EMPTY;
                            } else {
                                int count = 2;
                                VolumeByteStream cache = new VolumeByteStream();
                                for (int chrIndex : csvChrIndex) {
                                    if (chrIndex != -1) {
                                        cache.writeSafety(CHR_BYTECODE);
                                        cache.writeSafety(ValueUtils.Value2Text.int2bytes(count++));
                                        cache.writeSafety(ByteCode.EQUAL);
                                        cache.writeSafety(contigBlockContainer.getChromosomeByIndex((int) chrIndex).getName());
                                        cache.writeSafety(ByteCode.SEMICOLON);
                                    }
                                }
                                objects[index++] = cache.toUnmodifiableByteCode();
                                cache.close();
                            }
                            break;
                    }
                }
                return objects;
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        view(new File("/Users/wenjiepeng/Desktop/SV/data/private/disease/46+1_vcf/HG01258_HiFi_aligned_GRCh38_winnowmap.cuteSV2.vcf.sdf"));
    }
}
