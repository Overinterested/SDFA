package edu.sysu.pmglab.sdfa.sv.vcf.exception;

/**
 * @author Wenjie Peng
 * @create 2024-03-29 00:30
 * @description
 */
public class SVGenotypeParseException extends SVParseException {
    private static final SVGenotypeParseException instance = new SVGenotypeParseException();
    public static SVGenotypeParseException getInstance(){
        return instance;
    }
    public SVGenotypeParseException() {
    }

    public SVGenotypeParseException(String message) {
        super(message);
    }

    public SVGenotypeParseException(Throwable cause) {
        super(cause);
    }

    public SVGenotypeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
