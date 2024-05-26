package edu.sysu.pmglab.test;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.array.ByteCodeArray;
import edu.sysu.pmglab.container.array.IntArray;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

/**
 * @author Wenjie Peng
 * @create 2024-05-08 02:12
 * @description
 */
public class UkbbPhenotypeExtract {
    File subjectNameFile;
    File phenotypeNameFile;
    final File ukbbPhenotypeFile;
    final File extractOutputFile;
    HashSet<ByteCode> subjectNameSet = new HashSet<>();
    HashSet<ByteCode> phenotypeNameSet = new HashSet<>();

    private UkbbPhenotypeExtract(File ukbbPhenotypeFile, File extractOutputFile, File subjectNameFile, File phenotypeNameFile) {
        this.subjectNameFile = subjectNameFile;
        this.ukbbPhenotypeFile = ukbbPhenotypeFile;
        this.phenotypeNameFile = phenotypeNameFile;
        this.extractOutputFile = extractOutputFile;
    }

    public UkbbPhenotypeExtract of(File ukbbPhenotype, File extractOutputFile, File subjectNameFile, File phenotypeName) {
        return new UkbbPhenotypeExtract(ukbbPhenotype, extractOutputFile, subjectNameFile, phenotypeName);
    }

    public void extract() throws IOException {
        extractFeatureName(subjectNameFile, FileType.SUBJECT);
        extractFeatureName(phenotypeNameFile, FileType.PHENOTYPE);
        IntArray colIndexArray = new IntArray();
        boolean loadAllSubjects = subjectNameSet.isEmpty();
        boolean loadAllPhenotypes = phenotypeNameSet.isEmpty();
        if (loadAllPhenotypes && loadAllSubjects) {
            throw new UnsupportedOperationException("There is no sample or phenotype input.");
        }
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(ukbbPhenotypeFile, FileStream.DEFAULT_READER);
        fs.readLine(cache);
        BaseArray<ByteCode> phenotypeArray = cache.toByteCode().split(ByteCode.TAB);
        if (loadAllPhenotypes) {
            for (int i = 0; i < phenotypeArray.size(); i++) {
                colIndexArray.add(i);
            }
        } else {
            for (int i = 0; i < phenotypeArray.size(); i++) {
                ByteCode phenotype = phenotypeArray.get(i);
                if (phenotypeNameSet.contains(phenotype)) {
                    colIndexArray.add(i);
                    phenotypeNameSet.remove(phenotype);
                }
            }
        }
        if (!phenotypeNameSet.isEmpty()) {
            throw new UnsupportedEncodingException("Contain invalid phenotypes:" + new ByteCodeArray(phenotypeNameSet));
        }
        cache.reset();
        int phenotypeSize = colIndexArray.size();
        FileStream output = new FileStream(extractOutputFile, FileStream.DEFAULT_WRITER);
        while (fs.readLine(cache) != -1) {
            BaseArray<ByteCode> lineSplit = cache.toByteCode().split(ByteCode.TAB);
            ByteCode individualName = lineSplit.get(0);
            if (loadAllSubjects) {
                output.write(individualName);
                output.write(ByteCode.TAB);
                for (int i = 0; i < phenotypeSize; i++) {
                    output.write(lineSplit.get(colIndexArray.get(i)));
                    if (i != phenotypeSize - 1) {
                        output.write(ByteCode.TAB);
                    }
                }
                output.write(ByteCode.NEWLINE);
            }else {
                if (subjectNameSet.contains(individualName)){
                    output.write(individualName);
                    output.write(ByteCode.TAB);
                    for (int i = 0; i < phenotypeSize; i++) {
                        output.write(lineSplit.get(colIndexArray.get(i)));
                        if (i != phenotypeSize - 1) {
                            output.write(ByteCode.TAB);
                        }
                    }
                    output.write(ByteCode.NEWLINE);
                    subjectNameSet.remove(individualName);
                }
            }
            cache.reset();
        }
        if (!subjectNameSet.isEmpty()){
            System.out.println("There are subjects are not in ukbb:"+new ByteCodeArray(subjectNameSet));
        }
        fs.close();
        output.close();
    }

    public void extractFeatureName(File file, FileType status) throws IOException {
        if (file == null) {
            return;
        }
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(file, FileStream.DEFAULT_READER);
        boolean isSubject = status == FileType.SUBJECT;
        while (fs.readLine(cache) != -1) {
            ByteCode name = cache.toByteCode().asUnmodifiable();
            if (isSubject) {
                subjectNameSet.add(name);
            } else {
                phenotypeNameSet.add(name);
            }
            cache.reset();
        }
        fs.close();
        cache.close();
    }

    enum FileType {
        PHENOTYPE,
        SUBJECT;
    }
}
