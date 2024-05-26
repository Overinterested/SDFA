package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.sdfa.annotation.genome.GeneAnnotManager;

/**
 * @author Wenjie Peng
 * @create 2023-05-05 14:25
 * @description
 */
public enum AnnotManagerType {
    GenomeAnnot,
    IntervalAnnot,
    KnownSVAnnot;

    public String getString(AnnotManagerType type) {
        switch (type) {
            case GenomeAnnot:
                return "GenomeAnnot";
            case IntervalAnnot:
                return "IntervalAnnot";
            case KnownSVAnnot:
                return "KnownSVAnnot";
            default:
                return null;
        }
    }

    public AnnotManager getManager(AnnotManagerType type) {
        switch (type) {
            case GenomeAnnot:
                return new GeneAnnotManager();
//            case IntervalAnnot:
//                return new IntervalAnnotManager();
//            case KnownSVAnnot:
//                return new KnownSVAnnotManager();
            default:
                return null;
        }
    }

    public AnnotManager getManager() {
        switch (this) {
            case GenomeAnnot:
                return new GeneAnnotManager();
//            case IntervalAnnot:
//                return new IntervalAnnotManager();
//            case KnownSVAnnot:
//                return new KnownSVAnnotManager();
            default:
                return null;
        }
    }

    public static AnnotManagerType cast(String type){
        switch (type.toLowerCase()){
            case "interval":
                return IntervalAnnot;
            case "knownsv":
                return KnownSVAnnot;
            default:
                return GenomeAnnot;
        }
    }
}
