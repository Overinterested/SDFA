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
 * @create 2024-03-27 19:24
 * @description
 */
public class StandardVCFCallingParser extends AbstractCallingParser {
    /**
     * For more information, visit the pdf:
     * <a href="https://samtools.github.io/hts-specs/VCFv4.3.pdf">VCF v4.3 file</a>
     */
    public static final String version = "VCFv4.3";

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos,
                      ByteCodeArray encodeNoneFieldArray, ReusableMap<ByteCode, ByteCode> infoField,
                      Array<VCFFormatField> vcfFormatFields, SVGenotype[] genotypes) {

    }

    @Override
    public void parse(int indexOfFile, Chromosome chromosome, int pos, ByteCodeArray encodeNoneFieldArray,
                      ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes svGenotypes) {
        SVTypeSign type = SVTypeSign.getByName(infoField.get(SVTYPE_BYTECODE));
        if (type.getIndex() == BND_TYPE_INDEX) {
            loadOneLatest(type, -1, pos, -1, chromosome, encodeNoneFieldArray,
                    infoField, svGenotypes, new CSVLocation(indexOfFile));
        }
        int end = wrapToInt(infoField.get(END_BYTECODE));
        int len = wrapToInt(infoField.get(SVLEN_BYTECODE));
        loadOneLatest(type, len, pos, end, chromosome, encodeNoneFieldArray,
                infoField, svGenotypes, new CSVLocation(indexOfFile));
    }

}
