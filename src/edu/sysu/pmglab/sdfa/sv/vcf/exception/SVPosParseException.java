package edu.sysu.pmglab.sdfa.sv.vcf.exception;

/**
 * @author Wenjie Peng
 * @create 2024-03-29 00:38
 * @description
 */
public class SVPosParseException extends SVParseException{
    public SVPosParseException() {
    }

    public SVPosParseException(String message) {
        super(message);
    }

    public SVPosParseException(Throwable cause) {
        super(cause);
    }

    public SVPosParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
