package edu.sysu.pmglab.sdfa.sv.vcf.exception;

/**
 * @author Wenjie Peng
 * @create 2024-03-29 00:37
 * @description
 */
public class SVParseException extends RuntimeException {
    public SVParseException() {
    }

    public SVParseException(String message) {
        super(message);
    }

    public SVParseException(Throwable cause) {
        super(cause);
    }

    public SVParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
