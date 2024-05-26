package edu.sysu.pmglab.sdfa.annotation.base;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.genome.GeneAnnotManager;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2023-02-02 14:57
 * @description
 */
public abstract class AnnotManager {
    ByteCode tag;
    File annotFile;
    int tagMapIndex;
    ByteCode version;
    public static boolean EXIST_CSV = false;
    final static byte[] GenomeBytes = "Genome".getBytes();
    final static byte[] KnownSVBytes = "KnownSV".getBytes();
    final static byte[] IntervalBytes = "Interval".getBytes();

    abstract public void loadRefRecord() throws IOException;

    public AnnotManager setAnnotationVersion(String version) {
        if (version == null) {
            this.version = null;
            return this;
        }
        this.version = new ByteCode(version);
        return this;
    }

    public AnnotManager setAnnotFile(String file) {
        this.annotFile = new File(file);
        return this;
    }


    public AnnotManager setAnnotationTag(Object tag) {
        this.tag = new ByteCode(tag.toString());
        return this;
    }

    public String getStringAnnotationTag() {
        return tag.toString();
    }

    public ByteCode getAnnotationTag() {
        return tag;
    }

    abstract public boolean annotChrForInterval(Chromosome chr, int annotIndex);


    public AnnotManager setTagMapIndex(int tagMapIndex) {
        this.tagMapIndex = tagMapIndex;
        return this;
    }

    public int getTagMapIndex() {
        return tagMapIndex;
    }

    abstract public AnnotFeature decode(ByteCode src);

    public File getAnnotFile() {
        return annotFile;
    }

    public AnnotManager setAnnotFile(File file) {
        this.annotFile = file;
        return this;
    }

    public static AnnotManagerType getType(AnnotManager annotManager) {
        if (annotManager instanceof GeneAnnotManager) {
            return AnnotManagerType.GenomeAnnot;
        }
//        if (annotManager instanceof IntervalAnnotManager) {
//            return AnnotManagerType.IntervalAnnot;
//        }
//        if (annotManager instanceof KnownSVAnnotManager) {
//            return AnnotManagerType.KnownSVAnnot;
//        }
        return null;
    }

    public static AnnotManager of(Object type) {
        if (type instanceof String || type instanceof ByteCode) {
            ByteCode annotType = new ByteCode(type.toString());
//            if (annotType.equals(AnnotManager.IntervalBytes)) {
//                return new IntervalAnnotManager();
//            } else if (annotType.equals(AnnotManager.KnownSVBytes)) {
//                return new KnownSVAnnotManager();
//            } else if (annotType.equals(AnnotManager.GenomeBytes)) {
//                // ref genome can only be loaded for one time.
//                return new GeneAnnotManager();
//            }
        } else if (type instanceof AnnotManagerType) {
            switch ((AnnotManagerType) type) {
//                case IntervalAnnot:
//                    return new IntervalAnnotManager();
//                case KnownSVAnnot:
//                    return new KnownSVAnnotManager();
                case GenomeAnnot:
                    return new GeneAnnotManager();
                default:
                    break;
            }
        }
        System.exit(-1);
        return null;
    }

    public abstract void initOutput();

//    public abstract IAnnotOutput getOutput();


    public String[] getRefFieldNames() {
        return new String[0];
    }

//    public abstract AnnotManager setOutput(IAnnotOutput output);
}

