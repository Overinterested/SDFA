package edu.sysu.pmglab.sdfa.sv.vcf.calling;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.CSVLocation;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

/**
 * @author Wenjie Peng
 * @create 2024-04-08 01:17
 * @description
 */
public class UKBBCallingParser extends AbstractCallingParser {
    public static final ByteCode SVSIZE_BYTECODE = new ByteCode(new byte[]{ByteCode.S, ByteCode.V, ByteCode.S, ByteCode.I, ByteCode.Z, ByteCode.E});

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {
        SVTypeSign type = SVTypeSign.getByName(infoField.get(SVTYPE_BYTECODE));
        if (type != SVTypeSign.unknown) {
            if (type.isComplex()) {
                System.out.println(infoField.get(SVLEN_BYTECODE)+" is not defined!");
            } else {
                int len = Math.max(wrapToInt(infoField.get(SVSIZE_BYTECODE)), wrapToInt(infoField.get(SVLEN_BYTECODE)));
                int end = wrapToInt(infoField.get(END_BYTECODE));
                loadOne(type, len, pos, end, chromosome, encodeNoneFieldArray, infoField, vcfFormatFields, genotypes, new CSVLocation(indexOfFile));
            }
        }
    }

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos, ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes svGenotypes) {
        SVTypeSign type = SVTypeSign.getByName(infoField.get(SVTYPE_BYTECODE));
        if (type != SVTypeSign.unknown) {
            if (type.isComplex()) {
                System.out.println(infoField.get(SVLEN_BYTECODE)+" is not defined!");
            } else {
                int len = Math.max(wrapToInt(infoField.get(SVSIZE_BYTECODE)), wrapToInt(infoField.get(SVLEN_BYTECODE)));
                int end = wrapToInt(infoField.get(END_BYTECODE));
                loadOneLatest(type, len, pos, end, chromosome, encodeNoneFieldArray, infoField, svGenotypes, new CSVLocation(indexOfFile));
            }
        }
    }
}
