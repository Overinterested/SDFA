package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.sdfa.sv.vcf.exception.SVGenotypeParseException;

/**
 * @author Wenjie Peng
 * @create 2023-06-20 16:07
 * @description
 */
public class SVGenotype {
    final int i;
    final int j;
    final byte begCode;
    final int byteCode;
    /**
     * i|j 字符串格式
     */
    private final ByteCode phasedString;

    /**
     * i/j 字符串格式
     */
    private final ByteCode unPhasedString;
    /**
     * 基因型数据编码器
     */
    static final SVGenotype[][] genotypeEncoder = new SVGenotype[255][255];

    /**
     * 基因型数据解码器
     */
    static final SVGenotype[] genotypeDecoder = new SVGenotype[255 * 255];
    public static final SVGenotype noneGenotye;

    SVGenotype(int i, int j) {
        this.i = i;
        this.j = j;
        this.byteCode = encode(i + 1, j + 1);
        if (this.i <= 14 && this.j <= 14) {
            this.begCode = (byte) byteCode;
        } else {
            this.begCode = -1;
        }
        String left = i == 0 ? "." : String.valueOf(i - 1);
        String right = j == 0 ? "." : String.valueOf(j - 1);
        this.phasedString = new ByteCode(left + "|" + right).asUnmodifiable();
        if (i >= j) {
            this.unPhasedString = new ByteCode(right + "|" + left).asUnmodifiable();
        } else {
            this.unPhasedString = new ByteCode(left + "|" + right).asUnmodifiable();
        }
    }

    /**
     * 将基因型 i|j 或 i/j 编码为一个整数值
     *
     * @param i 基因型 i
     * @param j 基因型 j
     * @return 基因型编码值
     */
    private int encode(int i, int j) {
        if (i >= j) {
            return (i + 1) * (i + 1) - j;
        } else {
            return j * j + i + 1;
        }
    }

    static {
        for (int i = 0; i <= 253; i++) {
            for (int j = 0; j <= 253; j++) {
                genotypeEncoder[i][j] = new SVGenotype(i, j);
            }
        }

        for (int i = 0; i <= 253; i++) {
            for (int j = 0; j <= 253; j++) {
                SVGenotype genotype = genotypeEncoder[i][j];
                genotypeDecoder[genotype.hashCode()] = genotype;
            }
        }
        noneGenotye = genotypeEncoder[0][0];
    }

    public static final SVGenotype homozygousVariantType = SVGenotype.of(0, 0);
    public static final SVGenotype homozygousWildType = SVGenotype.of(1, 1);

    public int hashCode() {
        return this.byteCode;
    }

    public static SVGenotype of(int i, int j) {
        return genotypeEncoder[i + 1][j + 1];
    }

    private static SVGenotype of(byte i, byte j) {
        return genotypeEncoder[i == 46 ? 0 : i - ByteCode.ZERO + 1][j == 46 ? 0 : j - ByteCode.ZERO + 1];
    }

    public static SVGenotype of(String i, String j) {
        int left = i.equals(".") ? -1 : Integer.parseInt(i);
        int right = j.equals(".") ? -1 : Integer.parseInt(j);
        return genotypeEncoder[left + 1][right + 1];
    }

    public static SVGenotype of(byte begCode) {
        return genotypeDecoder[begCode];
    }

    public static SVGenotype of(ByteCode byteCode) throws SVGenotypeParseException {
        int left = 0;
        int right = 0;
        int sepIndex = -1;
        for (int i = 0; i < byteCode.length(); i++) {
            if (byteCode.byteAt(i) == 46) {
                left = 0;
                sepIndex = 1;
                break;
            }
            if (byteCode.byteAt(i) == 47 || byteCode.byteAt(i) == 124) {
                sepIndex = i;
                break;
            }
            left = left * 10 + byteCode.byteAt(i) - 48;
        }
        for (int i = sepIndex + 1; i < byteCode.length(); i++) {
            if (byteCode.byteAt(i) == 46) {
                right = 0;
                break;
            }
            if (byteCode.byteAt(i) < 48 || byteCode.byteAt(i) > 57) {
                break;
            }
            right = right * 10 + byteCode.byteAt(i) - 48;
        }
        if (sepIndex == -1) {
            throw SVGenotypeParseException.getInstance();
        }
        return of(left, right);
    }

    public static SVGenotype of(int byteCode) {
        return genotypeDecoder[byteCode];
    }

    public String toString() {
        return this.phasedString.toString();
    }

    public byte getBegCode() {
        return begCode;
    }

    public short getEncoder() {
        return begCode == -1 ? (short) byteCode : begCode;
    }

    public int getByteCode() {
        return byteCode;
    }

    public ByteCode getPhasedString() {
        return phasedString;
    }

}
