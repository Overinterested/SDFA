package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

/**
 * @author Wenjie Peng
 * @create 2024-03-25 11:27
 * @description
 */
public class SnifflesCallingParser extends AbstractCallingParser {
    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        int len1 = transferInt(infoField.get(SVLEN_BYTECODE));
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (!svTypeSign1.isComplex()) {
            loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
            return;
        }
        Entry<ByteCode, ByteCode> bndInfo = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
        if (end1 == -1 && bndInfo != null) {
            ByteCode endPos = bndInfo.getValue();
            if (endPos != null) {
                end1 = transferInt(endPos);
            }
        }

        loadTwo(indexOfFile, chromosome, wrapToChr(infoField.get(CHR2_BYTECODE), chromosome),
                svTypeSign1, svTypeSign1, pos, end1, -1, -1, len1, -1,
                encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
    }

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos, ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes svGenotypes) {
        int len1 = transferInt(infoField.get(SVLEN_BYTECODE));
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (!svTypeSign1.isComplex()) {
            loadOneLatest(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    svGenotypes, new CSVLocation(indexOfFile));
            return;
        }
        Entry<ByteCode, ByteCode> bndInfo = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
        if (end1 == -1 && bndInfo != null) {
            ByteCode endPos = bndInfo.getValue();
            if (endPos != null) {
                end1 = transferInt(endPos);
            }
        }
        loadTwoLatest(indexOfFile, chromosome, wrapToChr(infoField.get(CHR2_BYTECODE), chromosome),
                svTypeSign1, svTypeSign1, pos, end1, -1, -1, len1, -1,
                encodeNoneFieldArray, infoField, svGenotypes);
    }

}
