package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.*;
import edu.sysu.pmglab.sdfa.toolkit.SDFConstruction;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2025-04-24 17:56
 * @description the columns of tsv file contain 5 columns: CHR, POS, END, LENGTH, TYPE
 */
public class TSV2SDF extends SDFConstruction {

    @Override
    public SDFConstruction.LineConversionStatus lineConversion(ByteCode line, UnifiedSV sv) {
        if (line.startsWith(ByteCode.NUMBER_SIGN)) {
            return LineConversionStatus.SKIP_STATUS;
        }
        BaseArray<ByteCode> items = line.split(ByteCode.TAB);
        Chromosome chromosome = Chromosome.get(items.get(0));
        if (chromosome == null||chromosome==Chromosome.unknown){
            chromosome = Chromosome.add(items.get(0).toString());
        }
        int start = items.get(1).toInt();
        int end = items.get(2).toInt();
        int length = Math.abs(items.get(3).toInt());
        SVTypeSign svTypeSign = SVTypeSign.getByName(items.get(4));
        sv.setType(svTypeSign)
                .setCoordinate(new SVCoordinate(start, end, chromosome))
                .setLength(length)
                .setGenotypes(new SVGenotypes(new SVGenotype[0]));
        return LineConversionStatus.ACCEPT_RECORD;
    }

    public static void main(String[] args) throws IOException {
        new TSV2SDF().setInputFile("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/tmp.txt")
                .setOutputFile("/Users/wenjiepeng/Desktop/SV/AnnotFile/KnownSV/tmp_1.sdf")
                .submit();
    }
}
