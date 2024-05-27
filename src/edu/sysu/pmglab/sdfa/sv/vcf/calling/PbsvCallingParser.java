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
 * @create 2024-03-25 11:26
 * @description
 */
public class PbsvCallingParser extends AbstractCallingParser{
    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        ByteCode len1ByteCode = infoField.get(SVLEN_BYTECODE);
        int len1 = transferInt(len1ByteCode);
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (!svTypeSign1.isComplex()) {
            loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
            return;
        }
        Entry<ByteCode, ByteCode> bndInfo = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
        if (bndInfo == null){
            loadOne(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
            return;
        }
        loadTwo(indexOfFile, chromosome, wrapToChr(bndInfo.getKey(), chromosome), svTypeSign1, svTypeSign1,
                pos, wrapToInt(bndInfo.getValue()), end1, -1, len1, -1,
                encodeNoneFieldArray, infoField, vcfFormatFields, genotypes);
    }

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos, ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes svGenotypes) {
        ByteCode len1ByteCode = infoField.get(SVLEN_BYTECODE);
        int len1 = transferInt(len1ByteCode);
        int end1 = transferInt(infoField.get(END_BYTECODE));
        SVTypeSign svTypeSign1 = SVTypeSign.get(infoField.get(SVTYPE_BYTECODE));
        if (!svTypeSign1.isComplex()) {
            loadOneLatest(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    svGenotypes, new CSVLocation(indexOfFile));
            return;
        }
        Entry<ByteCode, ByteCode> bndInfo = UnifiedSV.parseBNDAlt(encodeNoneFieldArray.get(2));
        if (bndInfo == null){
            loadOneLatest(svTypeSign1, len1, pos, end1, chromosome, encodeNoneFieldArray, infoField,
                    svGenotypes, new CSVLocation(indexOfFile));
            return;
        }
        loadTwoLatest(indexOfFile, chromosome, wrapToChr(bndInfo.getKey(), chromosome), svTypeSign1, svTypeSign1,
                pos, wrapToInt(bndInfo.getValue()), end1, -1, len1, -1,
                encodeNoneFieldArray, infoField, svGenotypes);
    }

}
