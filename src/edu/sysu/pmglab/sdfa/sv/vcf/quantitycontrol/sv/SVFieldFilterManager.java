package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 23:05
 * @description
 */
public class SVFieldFilterManager {
    boolean init;
    boolean filter;
    int[] fieldFilterFailCount;
    Array<AbstractSVFieldFilter> fieldFilterArray;
    private final StringBuilder outputConcat = new StringBuilder();

    public SVFieldFilterManager() {
        init = false;
        filter = false;
        fieldFilterArray = new Array<>();
    }

    public boolean isEmpty() {
        return fieldFilterArray.isEmpty();
    }

    public boolean isFilter() {
        return filter;
    }

    public SVFieldFilterManager add(SVGenotypeFilter genotypeFilter) {
        filter = true;
        fieldFilterArray.add(genotypeFilter);
        return this;
    }

    public SVFieldFilterManager add(SVQualFieldFilter qualFieldFilter) {
        filter = true;
        fieldFilterArray.add(qualFieldFilter);
        return this;
    }

    public SVFieldFilterManager add(SVInfoFieldFilter infoFieldFilter) {
        filter = true;
        fieldFilterArray.add(infoFieldFilter);
        return this;
    }

    public SVFieldFilterManager add(SVFilterFieldFilter filterFieldFilter) {
        filter = true;
        fieldFilterArray.add(filterFieldFilter);
        return this;
    }

    public boolean filter(ByteCode qualField, ByteCode filterField,
                          ReusableMap<ByteCode, ByteCode> infoField, SVGenotypes genotypes) {
        if (!init) {
            fieldFilterFailCount = new int[fieldFilterArray.size()];
            init = true;
        }
        if (isFilter()) {
            boolean flag;
            int count = 0;
            for (AbstractSVFieldFilter abstractSVFieldFilter : fieldFilterArray) {
                if (abstractSVFieldFilter instanceof SVQualFieldFilter) {
                    flag = ((SVQualFieldFilter) abstractSVFieldFilter).filter(qualField);
                    if (!flag) {
                        fieldFilterFailCount[count] += 1;
                        return false;
                    }
                } else if (abstractSVFieldFilter instanceof SVFilterFieldFilter) {
                    flag = ((SVFilterFieldFilter) abstractSVFieldFilter).filter(filterField);
                    if (!flag) {
                        fieldFilterFailCount[count] += 1;
                        return false;
                    }
                } else if (abstractSVFieldFilter instanceof SVInfoFieldFilter) {
                    flag = ((SVInfoFieldFilter) abstractSVFieldFilter).filter(infoField);
                    if (!flag) {
                        fieldFilterFailCount[count] += 1;
                        return false;
                    }
                } else if (abstractSVFieldFilter instanceof SVGenotypeFilter) {
                    flag = ((SVGenotypeFilter) abstractSVFieldFilter).filter(genotypes.getGenotypes());
                    if (!flag) {
                        fieldFilterFailCount[count] += 1;
                        return false;
                    }
                }
                count++;
            }

        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int sumFailureCount = 0;
        for (int count : fieldFilterFailCount) {
            sumFailureCount += count;
        }
        if (sumFailureCount == 0){
            return "";
        }
        builder.append(sumFailureCount);
        builder.append(" SVs is filtered by field[");
        for (int i = 0; i < fieldFilterArray.size(); i++) {
            int count = fieldFilterFailCount[i];
            if (count == 0){
                continue;
            }
            builder.append(count);
            builder.append(" for ");
            builder.append(fieldFilterArray.get(i).getDescription());
            if (i != fieldFilterArray.size() - 1) {
                builder.append(",");
            } else {
                builder.append(".");
            }
        }
        builder.append("].");
        return builder.toString();
    }

    public Array<AbstractSVFieldFilter> getFieldFilterArray() {
        return fieldFilterArray;
    }
}
