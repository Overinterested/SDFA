package edu.sysu.pmglab.sdfa.merge.cmo;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 08:44
 * @description
 */
public enum SVMergeMode {
    POS_LEVEL,
    POS_OVERLAP_LEVEL,
    POS_GRAPH_LEVEL;

    public AbstractSVMergeStrategy getMergeStrategy() {
        switch (this) {
            case POS_LEVEL:
                return new PositionOnlyMergeStrategy();
            case POS_OVERLAP_LEVEL:
                return new PositionOverlapMergeStrategy();
        }
        throw new UnsupportedOperationException("Undefined merge strategy mode: " + this.name());
    }
}
