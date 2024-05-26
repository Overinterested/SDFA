package edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.gty;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFormatField;

import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 01:10
 * @description
 */
public class GenotypeFilterManager {
    int[] gtyFilterCount;
    IntArray filterIndexMap = new IntArray();
    Array<ByteCode> gtyFilterDescriptionArray = new Array<>();
    CallableSet<ByteCode> filterTagArray = new CallableSet<>();
    CallableSet<ByteCode> formatFieldSet = new CallableSet<>();
    Array<Function<ByteCode, Boolean>> gtyFilterFunctionArray = new Array<>();

    public GenotypeFilterManager addFilter(ByteCode filterTag, Function<ByteCode, Boolean> filterFunction, ByteCode description) {
        filterIndexMap.add(-1);
        filterTagArray.add(filterTag.asUnmodifiable());
        gtyFilterFunctionArray.add(filterFunction);
        gtyFilterDescriptionArray.add(description.asUnmodifiable());
        return this;
    }

    public GenotypeFilterManager addFilter(String filterTag, Function<ByteCode, Boolean> filterFunction, String description) {
        return addFilter(new ByteCode(filterTag), filterFunction, new ByteCode(description));
    }

    public GenotypeFilterManager addFilter(String filterTag, Function<ByteCode, Boolean> filterFunction) {
        return addFilter(filterTag, filterFunction, filterTag);
    }

    public GenotypeFilterManager addFilter(ByteCode filterTag, Function<ByteCode, Boolean> filterFunction) {
        return addFilter(filterTag, filterFunction, filterTag);
    }

    public void init(BaseArray<ByteCode> formatFieldArray) {
        for (ByteCode field : formatFieldArray) {
            formatFieldSet.add(field.asUnmodifiable());
        }
        int count = 0;
        for (ByteCode filterTag : filterTagArray) {
            int index = formatFieldSet.indexOfValue(filterTag);
            if (index == -1) {
                throw new UnsupportedOperationException(filterTag + " isn't in " + formatFieldSet + ".");
            }
            filterIndexMap.set(count++, index-1);
        }
        gtyFilterCount = new int[gtyFilterFunctionArray.size()];
    }

    public void filter(VCFFormatField vcfFormatField) {
        if (vcfFormatField.getSVGenotype().equals(SVGenotype.noneGenotye)) {
            return;
        }
        for (int i = 0; i < gtyFilterFunctionArray.size(); i++) {
            Function<ByteCode, Boolean> gtyFunction = gtyFilterFunctionArray.get(i);
            int filterIndex = filterIndexMap.get(i);
            boolean filter = gtyFunction.apply(vcfFormatField.getProperty(filterIndex));
            if (!filter) {
                vcfFormatField.filterGenotype();
                gtyFilterCount[i] += 1;
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int sumFailureCount = 0;
        for (int count : gtyFilterCount) {
            sumFailureCount += count;
        }
        if (sumFailureCount == 0) {
            return "";
        }
        builder.append(sumFailureCount);
        builder.append(" genotypes are filtered by rules[ ");
        for (int i = 0; i < filterTagArray.size(); i++) {
            int count = gtyFilterCount[i];
            if (count == 0) {
                continue;
            }
            builder.append(count);
            builder.append(" for ");
            builder.append(gtyFilterDescriptionArray.get(i));
            if (i != filterIndexMap.size() - 1) {
                builder.append(", ");
            } else {
                builder.append(".");
            }
        }
        builder.append("].");
        return builder.toString();
    }

    public void clear() {
        filterIndexMap.clear();
        filterTagArray.clear();
        formatFieldSet.clear();
        gtyFilterFunctionArray.clear();
        gtyFilterDescriptionArray.clear();
    }

    public boolean isEmpty() {
        return filterTagArray.isEmpty();
    }

    public IntArray getFilterIndexMap() {
        return filterIndexMap;
    }

    public Array<ByteCode> getGtyFilterDescriptionArray() {
        return gtyFilterDescriptionArray;
    }

    public Array<Function<ByteCode, Boolean>> getGtyFilterFunctionArray() {
        return gtyFilterFunctionArray;
    }

    public CallableSet<ByteCode> getFilterTagArray() {
        return filterTagArray;
    }
}
