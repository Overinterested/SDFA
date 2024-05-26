package edu.sysu.pmglab.sdfa.annotation.preprocess;

import edu.sysu.pmglab.ccf.CCFMeta;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.TrieTree;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.easytools.wrapper.DefaultValueWrapper;

/**
 * @author Wenjie Peng
 * @create 2024-03-30 06:45
 * @description
 */
public class AnnotationResourceMeta extends CCFMeta {
    byte posType;
    ByteCode version;
    ByteCode resource;
    CallableSet<ByteCode> fieldArray;
    ContigBlockContainer contigBlockContainer;
    private static final DefaultValueWrapper<ByteCode> emptyWrapper = DefaultValueWrapper.emptyBytecodeWrapper;

    public AnnotationResourceMeta write() {
        add("posType", String.valueOf(posType));
        add("version", emptyWrapper.getValue(version));
        add("resource", emptyWrapper.getValue(resource));
        add("contigBlock", contigBlockContainer.encode());
        if (fieldArray != null) {
            for (ByteCode item : fieldArray) {
                add("field", item);
            }
        }
        return this;
    }

    public static AnnotationResourceMeta decode(CCFMeta meta) {
        AnnotationResourceMeta resourceMeta = new AnnotationResourceMeta();
        resourceMeta.version = meta.get("version").values().get(0);
        resourceMeta.resource = meta.get("resource").values().get(0);
        resourceMeta.posType = meta.get("posType").values().get(0).toByte();
        resourceMeta.contigBlockContainer = ContigBlockContainer.decode(meta.get("contigBlock").values().get(0));
        TrieTree<ByteCode> field = meta.get("field");
        if (field != null){
            resourceMeta.fieldArray.addAll(field.values());
        }
        meta.clear();
        return resourceMeta;
    }

    public AnnotationResourceMeta setChromosomeBlockContainer(ContigBlockContainer contigBlockContainer) {
        this.contigBlockContainer = contigBlockContainer;
        return this;
    }

    public AnnotationResourceMeta setResource(ByteCode resource) {
        this.resource = resource;
        return this;
    }

    public AnnotationResourceMeta setFieldArray(CallableSet<ByteCode> fieldArray) {
        this.fieldArray = fieldArray;
        return this;
    }

    public AnnotationResourceMeta setVersion(ByteCode version) {
        this.version = version;
        return this;
    }

    public ContigBlockContainer getContigBlockContainer() {
        return contigBlockContainer;
    }
}
