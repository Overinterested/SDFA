package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:33
 * @description
 */
public abstract class AbstractSVMergeOutput {
    public static AtomicInteger SVCount = new AtomicInteger(0);

    Array<AbstractSVMergeStrategy.MergedSV> mergedSVArray = new Array<>();

    abstract public void write(AbstractSVMergeStrategy.MergedSV mergedSV) throws IOException;

    abstract public AbstractSVMergeOutput initOutput(File outputDir);

    public void acceptMergedSV(){
        mergedSVArray = AbstractSVMergeStrategy.getMergedSVArray();
    }

    abstract public void endWrite() throws IOException;

    public void writeAll() throws IOException {
        if (mergedSVArray == null || mergedSVArray.isEmpty()) {
            AbstractSVMergeStrategy.mergedSVSize = 0;
            return;
        }
        for (AbstractSVMergeStrategy.MergedSV mergedSV : mergedSVArray) {
            write(mergedSV);
            mergedSV.cache.reset();
            mergedSV.infoFeatureArray.set(3, null);
            mergedSV.infoFeatureArray.set(4, null);
        }
        mergedSVArray.clear();
        AbstractSVMergeStrategy.mergedSVSize = 0;
    }

    abstract public String getOutputFormat();

    /**
     * write the header of VCF
     * @throws IOException output filestream exception
     */
    abstract public void initHeader() throws IOException;
    public Array<AbstractSVMergeStrategy.MergedSV> getMergedSVArray() {
        return mergedSVArray;
    }

}
