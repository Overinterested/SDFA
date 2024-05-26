package edu.sysu.pmglab.sdfa.sv.vcf.exception;

import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;

import java.util.Arrays;

/**
 * @author Wenjie Peng
 * @create 2024-03-29 00:45
 * @description
 */
public class MissingGenotypeException extends SVParseException{
    public MissingGenotypeException() {
    }

    public MissingGenotypeException(String message) {
        super(message);
    }

    public MissingGenotypeException(Throwable cause) {
        super(cause);
    }

    public MissingGenotypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SVGenotype[] fillMissingGTs(int subjectSize){
        SVGenotype[] res = new SVGenotype[subjectSize];
        Arrays.fill(res, SVGenotype.noneGenotye);
        return res;
    }
}
