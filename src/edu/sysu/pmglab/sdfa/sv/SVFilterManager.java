package edu.sysu.pmglab.sdfa.sv;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.gty.GenotypeFilterManager;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.*;

import java.util.function.Function;

/**
 * @author Wenjie Peng
 * @create 2024-03-26 01:31
 * @description
 */
public abstract class SVFilterManager {
    boolean svFilter;
    boolean gtyFilter;
    boolean fieldFilter;
    private boolean init = false;
    private boolean check = false;
    protected SVLevelFilterManager svFilterManager = new SVLevelFilterManager();
    protected SVFieldFilterManager fieldFilterManager = new SVFieldFilterManager();
    protected GenotypeFilterManager genotypeFilterManager = new GenotypeFilterManager();

    /**
     * init sv level management: use svFilterManager.add(filter) to design your function
     */
    protected abstract void initSVLevelFilter();

    /**
     * init genotype level management: use genotypeFilterManager.add(filter) to design your function
     */
    protected abstract void initGenotypeFilter();

    protected abstract void initFieldFilter();

    public final void init() {
        if (!init) {
            initSVLevelFilter();
            initGenotypeFilter();
            initFieldFilter();
            svFilter = svFilterManager != null && !svFilterManager.isEmpty();
            fieldFilter = fieldFilterManager != null && !fieldFilterManager.isEmpty();
            gtyFilter = genotypeFilterManager != null && !genotypeFilterManager.isEmpty();
            init = true;
        }
    }

    /**
     * check whether genotype format field is in VCF format
     *
     * @param formatFieldArray vcf format array
     */
    public void check(BaseArray<ByteCode> formatFieldArray) {
        if (!check) {
            if (gtyFilter) {
                genotypeFilterManager.init(formatFieldArray);
            }
            check = true;
        }
    }

    public boolean filterSV() {
        return svFilter;
    }

    public boolean filterGty() {
        return gtyFilter;
    }

    public boolean filterField() {
        return fieldFilter;
    }

    public boolean isCheck() {
        return check;
    }

    public SVLevelFilterManager getSvFilterManager() {
        return svFilterManager;
    }

    public GenotypeFilterManager getGenotypeFilterManager() {
        return genotypeFilterManager;
    }

    public SVFieldFilterManager getFieldFilterManager() {
        return fieldFilterManager;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fieldFilter) {
            sb.append(fieldFilterManager.toString());
        }
        if (svFilter) {
            sb.append(" ");
            sb.append(svFilterManager.toString());
        }
        if (gtyFilter) {
            sb.append(" ");
            sb.append(genotypeFilterManager.toString());
        }
        return sb.toString();
    }

    public SVFilterManager copy() {
        boolean containSVFilter = svFilter;
        Array<AbstractSVFilter> svLevelFilter = new Array<>();
        if (svFilter) {
            svLevelFilter.addAll(svFilterManager.getCsvFilterArray());
            svLevelFilter.addAll(svFilterManager.getUnifiedSVFilterArray());
        }
        boolean containFieldFilter = fieldFilter;
        Array<AbstractSVFilter> fieldLevelFilter = new Array<>();
        if (containFieldFilter) {
            fieldLevelFilter.addAll(fieldFilterManager.getFieldFilterArray());
        }
        boolean containGenotypeFilter = gtyFilter;
        GenotypeFilterManager tmpGenotypeFilterManager = genotypeFilterManager;
        return new SVFilterManager() {
            /**
             * init sv level management: use svFilterManager.add(filter) to design your function
             */
            @Override
            protected void initSVLevelFilter() {
                if (containSVFilter) {
                    for (AbstractSVFilter abstractSVFilter : svLevelFilter) {
                        if (abstractSVFilter instanceof AbstractUnifiedSVFilter) {
                            this.svFilterManager.add((AbstractUnifiedSVFilter) abstractSVFilter);
                        } else if (abstractSVFilter instanceof AbstractComplexSVFilter) {
                            this.svFilterManager.add((AbstractComplexSVFilter) abstractSVFilter);
                        }
                    }
                }
            }

            /**
             * init genotype level management: use genotypeFilterManager.add(filter) to design your function
             */
            @Override
            protected void initGenotypeFilter() {
                if (containGenotypeFilter) {
                    CallableSet<ByteCode> filterTagArray = tmpGenotypeFilterManager.getFilterTagArray();
                    Array<ByteCode> gtyFilterDescriptionArray = tmpGenotypeFilterManager.getGtyFilterDescriptionArray();
                    Array<Function<ByteCode, Boolean>> gtyFilterFunctionArray = tmpGenotypeFilterManager.getGtyFilterFunctionArray();
                    for (int i = 0; i < filterTagArray.size(); i++) {
                        this.genotypeFilterManager.addFilter(
                                filterTagArray.getByIndex(i), gtyFilterFunctionArray.get(i),
                                gtyFilterDescriptionArray.get(i)
                        );
                    }
                }
            }

            @Override
            protected void initFieldFilter() {
                if (containFieldFilter) {
                    for (AbstractSVFilter abstractSVFilter : fieldLevelFilter) {
                        if (abstractSVFilter instanceof SVQualFieldFilter) {
                            this.fieldFilterManager.add((SVQualFieldFilter) abstractSVFilter);
                        } else if (abstractSVFilter instanceof SVFilterFieldFilter) {
                            this.fieldFilterManager.add((SVFilterFieldFilter) abstractSVFilter);
                        } else if (abstractSVFilter instanceof SVInfoFieldFilter) {
                            this.fieldFilterManager.add((SVInfoFieldFilter) abstractSVFilter);
                        } else if (abstractSVFilter instanceof SVGenotypeFilter) {
                            this.fieldFilterManager.add((SVGenotypeFilter) abstractSVFilter);
                        }
                    }
                }
            }
        };
    }

    public void clear() {
        svFilterManager = null;
        fieldFilterManager = null;
        genotypeFilterManager = null;
    }
}
