package edu.sysu.pmglab.sdfa.merge.cmo;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 08:45
 * @description
 */
public enum SVMergedOutputMode {
    VCF_MODE,
    SDF_MODE,
    BED_MODE,
    VCF_GZ_MODE,
    VCF_BGZ_MODE;
    public AbstractSVMergeOutput getOutput(){
        switch (this){
            case VCF_MODE:
                return new VCFOutputGenerator();
            case VCF_BGZ_MODE:
                break;
        }
        throw new UnsupportedOperationException("Undefined merge output mode: " + this.name());
    }
}
