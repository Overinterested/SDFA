package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.CSVLocation;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

/**
 * @author Wenjie Peng
 * @create 2024-03-25 11:27
 * @description
 */
public class SvimCallingParser extends AbstractCallingParser {
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
        ByteCode pos2ByteCode = infoField.get(POS2_BYTECODE);
        int pos2 = wrapToInt(pos2ByteCode);
        ByteCode len2ByteCode = infoField.get(SVLEN2_BYTECODE);
        int len2 = wrapToInt(len2ByteCode);
        ByteCode end2ByteCode = infoField.get(END2_BYTECODE);
        int end2 = wrapToInt(end2ByteCode);
        Chromosome chromosome2;
        ByteCode chr2ByteCode = infoField.get(CHR2_BYTECODE);
        if (svTypeSign1.getIndex() == BND_TYPE_INDEX) {
            if (chr2ByteCode == null) {
                Entry<ByteCode, ByteCode> bndInfo = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
                if (bndInfo != null) {
                    chromosome2 = contig.get(bndInfo.getKey());
                    pos2 = wrapToInt(bndInfo.getValue());
                    loadTwo(indexOfFile, chromosome, chromosome2, svTypeSign1, svTypeSign1, pos, pos2,
                            end1, end2, len1, len2, encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
                    return;
                }
            } else {
                chromosome2 = wrapToChr(chr2ByteCode, chromosome);
                if (end1 == -1 && pos2 == -1) {
                    loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                            vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
                    return;
                } else {
                    pos2 = pos2 == -1 ? end1 : pos2;
                    loadTwo(indexOfFile, chromosome, chromosome2, svTypeSign1, svTypeSign1, pos, pos2,
                            -1, -1, -1, -1, encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
                    return;
                }
            }
        }
        loadTwo(indexOfFile, chromosome, wrapToChr(infoField.get(CHR2_BYTECODE), chromosome),
                svTypeSign1, svTypeSign1, pos, end1, -1, -1, len1, -1,
                encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
    }

}
