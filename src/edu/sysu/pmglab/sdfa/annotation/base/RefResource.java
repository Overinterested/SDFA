package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.container.File;

import java.util.Objects;

/**
 * @author Wenjie Peng
 * @create 2023-09-30 15:19
 * @description
 */
public class RefResource {
    File file;
    String tag;
    String relativePath;
    AnnotManagerType type;

    public RefResource(File file, AnnotManagerType type) {
        this.file = file;
        this.type = type;
        this.tag = file.getName();
    }

    public RefResource(String tag, File file, AnnotManagerType type) {
        this.file = file;
        this.tag = tag;
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public String getTag() {
        return tag;
    }

    public AnnotManagerType getType() {
        return type;
    }

    public AnnotManager toAnnotManager() {
        return Objects.requireNonNull(type.getManager()).setAnnotationTag(tag).setAnnotFile(file);
    }

    public RefResource setFile(File file) {
        this.file = file;
        return this;
    }

    public RefResource setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        return this;
    }

    public String getRelativePath() {
        return relativePath;
    }
}
