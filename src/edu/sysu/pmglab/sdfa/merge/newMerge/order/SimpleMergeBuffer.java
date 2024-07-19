package edu.sysu.pmglab.sdfa.merge.newMerge.order;

import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.sdfa.merge.newMerge.principle.SVMergePrinciple;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

/**
 * @author Wenjie Peng
 * @create 2024-07-09 01:40
 * @description
 */
public class SimpleMergeBuffer {
    Array<UnifiedSV> buffer;
    private static SVMergePrinciple mergePrinciple;

    public SimpleMergeBuffer() {
        buffer = new Array<>(true);
    }

    public BaseArray<UnifiedSV> update(UnifiedSV sv) {
        if (buffer.isEmpty()) {
            return null;
        }
        if (!mergePrinciple.merge(buffer.get(0), sv)) {
            return buffer.popFirst(buffer.size());
        }
        buffer.add(sv);
        return null;
    }

    public BaseArray<UnifiedSV> popMergedSVs(){
        if (buffer.isEmpty()){
            return null;
        }
        return buffer.popFirst(buffer.size());
    }
}
