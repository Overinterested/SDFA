package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.annotation.Future;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 02:15
 * @description
 */
public class SVLevelFilterManager {
    boolean init = false;
    boolean filter = false;
    // CSV filter config
    int numOfCSVFilter = 0;
    int[] csvFilterFailCount;
    // unified SV filter config
    int numOfUnifiedSVFilter = 0;
    int[] unifiedFilterFailCount;
    Array<AbstractComplexSVFilter> csvFilterArray;
    Array<AbstractUnifiedSVFilter> unifiedSVFilterArray;
    private final StringBuilder outputConcat = new StringBuilder();


    @Future(value = "Create filter condition from a file like filter.config",
            reason = "Convenient for user with no coding skills")
    public static SVLevelFilterManager of(File filterFile) {
        return new SVLevelFilterManager();
    }

    public SVLevelFilterManager() {
        csvFilterArray = new Array<>();
        unifiedSVFilterArray = new Array<>();
    }

    public SVLevelFilterManager add(AbstractComplexSVFilter csvFilter) {
        filter = true;
        this.numOfCSVFilter++;
        this.csvFilterArray.add(csvFilter);
        return this;
    }

    public SVLevelFilterManager add(AbstractUnifiedSVFilter unifiedSVFilter) {
        filter = true;
        this.numOfUnifiedSVFilter++;
        this.unifiedSVFilterArray.add(unifiedSVFilter);
        return this;
    }

    public boolean filter(UnifiedSV sv) {
        if (!init) {
            init();
        }
        if (filter) {
            for (int i = 0; i < numOfUnifiedSVFilter; i++) {
                if (!unifiedSVFilterArray.get(i).filter(sv)) {
                    unifiedFilterFailCount[i] += 1;
                    return false;
                }
            }
        }
        return true;
    }

    public boolean filter(ComplexSV csv) {
        if (!init) {
            init();
        }
        if (filter) {
            for (int i = 0; i < numOfCSVFilter; i++) {
                if (!csvFilterArray.get(i).filter(csv)) {
                    csvFilterFailCount[i] += 1;
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public void clear() {
        init = false;
        filter = false;
        numOfCSVFilter = 0;
        csvFilterArray.clear();
        numOfUnifiedSVFilter = 0;
        unifiedSVFilterArray.clear();
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isEmpty() {
        return (numOfUnifiedSVFilter + numOfCSVFilter) == 0;
    }

    private void init() {
        if (!unifiedSVFilterArray.isEmpty()) {
            unifiedFilterFailCount = new int[unifiedSVFilterArray.size()];
        }
        if (!csvFilterArray.isEmpty()) {
            csvFilterFailCount = new int[csvFilterArray.size()];
        }
        init = true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int sumFailureCount = 0;
        for (int count : csvFilterFailCount) {
            sumFailureCount += count;
        }
        for (int count : unifiedFilterFailCount) {
            sumFailureCount += count;
        }
        if (sumFailureCount == 0) {
            return "";
        }
        builder.append(sumFailureCount);
        builder.append(" SVs are filter by more detail rules[");
        for (int i = 0; i < csvFilterArray.size(); i++) {
            int count = csvFilterFailCount[i];
            if (count == 0) {
                continue;
            }
            builder.append(count);
            builder.append(" CSVs for ");
            builder.append(csvFilterArray.get(i).getDescription());
            if (i != csvFilterArray.size() - 1) {
                builder.append(",");
            } else {
                builder.append(".");
            }
        }
        builder.append(" ");
        for (int i = 0; i < unifiedFilterFailCount.length; i++) {
            int count = unifiedFilterFailCount[i];
            if (count == 0) {
                continue;
            }
            builder.append(count);
            builder.append(" simple SVs for ");
            builder.append(unifiedSVFilterArray.get(i).getDescription());
            if (i != unifiedSVFilterArray.size() - 1) {
                builder.append(", ");
            } else {
                builder.append(".");
            }
        }
        builder.append("].");
        return builder.toString();
    }

    public Array<AbstractComplexSVFilter> getCsvFilterArray() {
        return csvFilterArray;
    }

    public Array<AbstractUnifiedSVFilter> getUnifiedSVFilterArray() {
        return unifiedSVFilterArray;
    }
}
