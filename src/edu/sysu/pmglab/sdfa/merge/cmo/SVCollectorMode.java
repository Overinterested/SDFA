package edu.sysu.pmglab.sdfa.merge.cmo;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 08:44
 * @description
 */
public enum SVCollectorMode {
    CHR_LEVEL,
    MULTI_PER_LEVEL,
    MERGED_CHR_LEVEL,
    MERGED_PER_LEVEL,
    INTERVAL_LEVEL;
    public AbstractSVCollector getCollector(){
        switch (this){
            case CHR_LEVEL:
                return new ChromosomeLevelCollector();
            case MERGED_CHR_LEVEL:
                break;
        }
        throw new UnsupportedOperationException("Undefined merge collection mode: " + this.name());
    }
}
