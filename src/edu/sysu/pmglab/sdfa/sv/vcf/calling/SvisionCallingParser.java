package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.CSVLocation;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-25 11:27
 * @description
 */
public class SvisionCallingParser extends AbstractCallingParser {
    public static byte[] CSV_BYTES = new byte[]{60, ByteCode.C, ByteCode.S, ByteCode.V};
    public static ByteCode BKPS_BYTECODE = new ByteCode(new byte[]{ByteCode.B, ByteCode.K, ByteCode.P, ByteCode.S});

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        ByteCode alt = encodeNoneFieldArray.get(2);
        int len1 = transferInt(infoField.get(SVLEN_BYTECODE));
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (!alt.startsWith(CSV_BYTES)) {
            loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
        } else {
            ByteCode csvInfo = infoField.get(BKPS_BYTECODE);
            BaseArray<ByteCode> unifiedSVArray = csvInfo.split(ByteCode.COMMA);
            int csvSize = unifiedSVArray.size();
            Array<CSVSubType> csvSubTypeArray = new Array<>(csvSize);
            for (ByteCode unifiedSVByteCode : unifiedSVArray) {
                BaseArray<ByteCode> split = unifiedSVByteCode.split(ByteCode.COLON);
                SVTypeSign tmpType = SVTypeSign.getCSVSubType(split.get(0).asUnmodifiable());
                BaseArray<ByteCode> tmpCoordinate = split.get(1).split(ByteCode.MINUS);
                int tmpLen = tmpCoordinate.get(0).toInt();
                int tmpPos = tmpCoordinate.get(1).toInt();
                int tmpEnd = tmpCoordinate.get(2).toInt();
                csvSubTypeArray.add(new CSVSubType(tmpPos, tmpEnd, tmpLen, tmpType));
            }
            csvSubTypeArray.sort(CSVSubType::compareTo);
            Array<ByteCodeArray> encodeFormatFieldArray = getEncodeFormatFieldArray(vcfFormatFields);
            ByteCodeArray encodeInfoField = asUnmodifiedInfoField(infoField.values());
            for (int i = 0; i < csvSize; i++) {
                CSVSubType tmp = csvSubTypeArray.get(i);
                if (i == 0) {
                    loadEncodeOne(tmp.typeSign, tmp.length, tmp.pos, tmp.end, chromosome,
                            encodeNoneFieldArray, encodeInfoField, encodeFormatFieldArray, genotypes,
                            new CSVLocation(indexOfFile, createArray(csvSize, chromosome.getIndex(), i)));
                }else {
                    loadEncodeOne(tmp.typeSign, tmp.length, tmp.pos, tmp.end, chromosome,
                            null, null, null, genotypes,
                            new CSVLocation(indexOfFile, createArray(csvSize, chromosome.getIndex(), i)));
                }
            }
        }
    }

    private static class CSVSubType implements Comparable<CSVSubType> {
        int pos;
        int end;
        int length;
        SVTypeSign typeSign;

        public CSVSubType(int pos, int end, int length, SVTypeSign typeSign) {
            this.pos = pos;
            this.end = end;
            this.length = length;
            this.typeSign = typeSign;
        }

        @Override
        public int compareTo(CSVSubType o) {
            int status = Integer.compare(pos, o.pos);
            if (status != 0) {
                status = Integer.compare(end, o.end);
                return status == 0 ? Integer.compare(length, o.length) : status;
            }
            return status;
        }
    }

    private static IntArray createArray(int size, int value, int index) {
        int[] res = new int[size];
        Arrays.fill(res, value);
        res[index] = -1;
        return IntArray.wrap(res);
    }

}
