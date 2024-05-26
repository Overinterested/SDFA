package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;

import java.util.Iterator;
import java.util.Objects;

/**
 * @author Wenjie Peng
 * @create 2023-05-05 14:31
 * @description
 */
public class AnnotResourceBuilder {

    public static int annotIndex = 0;
    Array<AnnotManager> globalAnnotArray = new Array<>();
    public static Array<RefResource> refResourceArray = new Array<>();
    private static final AnnotResourceBuilder instance = new AnnotResourceBuilder();

    private AnnotResourceBuilder() {
    }

    public static AnnotResourceBuilder getInstance() {
        return instance;
    }

    public AnnotResourceBuilder add(String annotFilePath, AnnotManagerType annotManagerType) {
        return add(annotFilePath, annotManagerType, null, null);
    }

    public AnnotResourceBuilder add(String annotFilePath, AnnotManagerType annotManagerType, String tag) {
        return add(annotFilePath, annotManagerType, tag, null);
    }

    public AnnotResourceBuilder addAll(Iterator<Entry<File, AnnotManagerType>> annotFileAndType) {
        if (annotFileAndType.hasNext()) {
            Entry<File, AnnotManagerType> next = annotFileAndType.next();
            File key = next.getKey();
            AnnotManagerType value = next.getValue();
            add(key.getAbsolutePath(), value, key.getName());
        }
        return this;
    }

    public AnnotResourceBuilder add(String annotFilePath, AnnotManagerType annotManagerType, String tag, String version) {
        AnnotManager annotManager = Objects.requireNonNull(annotManagerType.getManager())
                .setAnnotFile(annotFilePath)
                .setTagMapIndex(annotIndex++)
                .setAnnotationTag(tag)
                .setAnnotationVersion(version);
        globalAnnotArray.add(annotManager);
        File annotFile = new File(annotFilePath);
        refResourceArray.add(new RefResource(tag, new File(annotFilePath), annotManagerType).setRelativePath(annotFile.getName()));
        return this;
    }

    // TODO
    public static Array<RefResource> setRefResourceArray(File file) {
        return refResourceArray;
    }

    public static AnnotResourceBuilder setRefResourceArray(Array<RefResource> refResourceArray) {
        AnnotResourceBuilder.refResourceArray = refResourceArray;
        return instance;
    }

    public static Array<RefResource> getRefResourceArray() {
        return refResourceArray;
    }
}
