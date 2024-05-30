package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFWriter;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.*;
import edu.sysu.pmglab.container.array.*;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.SDFViewer;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVGenotypes;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.unifyIO.FileStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Wenjie Peng
 * @create 2024-05-28 11:18
 * @description
 */

public class SDFExtract {
    boolean isPedFile;
    ByteCode separate;
    final File sdfFile;
    final File outputDir;
    private Logger logger;
    final File subjectRecordFile;
    boolean storeAllHomGtys = false;
    boolean noCheckForUnMapSubject = true;
    IntArray filterCount = new IntArray();
    Array<Entry<Function<SVGenotype[], Boolean>, String>> filterConditionArray = new Array<>();

    private SDFExtract(File sdfFile, File subjectRecordFile, File outputDir) {
        this.sdfFile = sdfFile;
        this.outputDir = outputDir;
        this.subjectRecordFile = subjectRecordFile;
    }

    public static SDFExtract of(Object sdfFile, Object subjectRecordFile, Object outputDir) {
        return new SDFExtract(
                new File(sdfFile.toString()),
                new File(subjectRecordFile.toString()),
                new File(outputDir.toString())
        );
    }

    public SDFExtract storeAllHomGtys(boolean store) {
        storeAllHomGtys = store;
        return this;
    }

    public void submit() throws IOException {
        IndexableSet<ByteCode> extractSubjectSet = parseSubjectName();
        SDFReader reader = new SDFReader(sdfFile);
        SDFMeta meta = SDFMeta.decode(reader);
        Subjects subjects = meta.getSubjects();
        IntArray indexOfExtractSubject = checkSubjectName(extractSubjectSet, subjects);
        CCFReader ccfReader = reader.getReader();
        IRecord record = ccfReader.getRecord();
        File outputFile = outputDir.getSubFile(sdfFile.getName());
        CCFWriter writer = CCFWriter.Builder.of(outputFile)
                .addFields(ccfReader.getAllFields())
                .build();
        SVGenotype[] svGenotypes = new SVGenotype[indexOfExtractSubject.size()];
        boolean dropSV;
        int[] countSVOfContig = new int[meta.getContig().getAllChromosomes().size()];
        while (ccfReader.read(record)) {
            dropSV = false;
            ShortArray tmp = ShortArray.wrap(record.get(3));
            SVGenotypes gtys = SVGenotypes.decode(tmp);
            for (int i = 0; i < indexOfExtractSubject.size(); i++) {
                assert gtys != null;
                svGenotypes[i] = gtys.getGenotype(i);
            }
            //region filter
            // filter all hom
            if (!storeAllHomGtys) {
                dropSV = true;
                for (SVGenotype svGenotype : svGenotypes) {
                    if (!svGenotype.equals(SVGenotype.noneGenotye)) {
                        dropSV = false;
                        break;
                    }
                }
                if (dropSV) {
                    continue;
                }
            }
            for (int i = 0; i < filterConditionArray.size(); i++) {
                Entry<Function<SVGenotype[], Boolean>, String> tmpFunctionEntry = filterConditionArray.get(i);
                if (tmpFunctionEntry.getKey().apply(svGenotypes)) {
                    filterCount.set(i, filterCount.get(i) + 1);
                    dropSV = true;
                    break;
                }
            }
            if (dropSV) {
                continue;
            }
            //endregion
            record.set(3, new SVGenotypes(svGenotypes).encodeGTs());
            record.set(4, resetGtyFormat(record.get(4), indexOfExtractSubject));
            writer.write(record);
            int[] coordinate = record.get(0);
            countSVOfContig[coordinate[0]]++;
        }
        Subjects extractSubjectInFile = new Subjects(outputFile)
                .addAll(
                        extractSubjectSet.stream()
                                .map(Subject::new)
                                .collect(Collectors.toList())
                );
        meta.initChrBlockRange(countSVOfContig);
        meta.setSubjects(extractSubjectInFile);
        writer.writeMeta(meta.write());
        writer.close();
    }

    private ByteCode[] resetGtyFormat(ByteCode[] formatField, IntArray indexOfExtract) {
        if (formatField.length == 0) {
            return formatField;
        }
        ByteCode empty = ByteCodeArray.wrap(new ByteCode[0]).encode().toByteCode();
        ByteCode[] res = new ByteCode[formatField.length];
        ByteCodeArray tmp = new ByteCodeArray();
        for (int i = 0; i < formatField.length; i++) {
            ByteCodeArray decodeItem = (ByteCodeArray) ArrayType.decode(formatField[i]);
            if (decodeItem.isEmpty()) {
                res[i] = empty;
                continue;
            }
            for (int index : indexOfExtract) {
                tmp.add(decodeItem.get(index));
            }
            res[i] = tmp.encode().toUnmodifiableByteCode();
            tmp.clear();
        }
        return res;
    }

    private IntArray checkSubjectName(IndexableSet<ByteCode> extractSubjectSet, Subjects subjects) {
        IntArray indexOfExtractSubject = new IntArray();
        if (subjects == null || subjects.numOfSubjects() == 0) {
            throw new UnsupportedOperationException("No subjects in raw SDF file.");
        }
        CallableSet<ByteCode> subjectNameSet = new CallableSet<>();
        CallableSet<Subject> subjectSet = subjects.getSubjectSet();
        for (Subject subject : subjectSet) {
            subjectNameSet.add(subject.getName());
        }

        if (subjectNameSet.size() != 0) {
            for (ByteCode extractSubject : extractSubjectSet) {
                int indexOfSubject = subjectNameSet.indexOfValue(extractSubject);
                if (indexOfSubject == -1 && !noCheckForUnMapSubject) {
                    throw new UnsupportedOperationException(extractSubject.toString() + " isn't in subject set of raw file");
                }
                indexOfExtractSubject.add(indexOfSubject);
            }
        }
        if (logger != null) {
            if (extractSubjectSet.size() == indexOfExtractSubject.size()) {
                logger.info("Mapping " + extractSubjectSet + " subjects to raw sdf file.");
            } else {
                logger.error("Mapping " + indexOfExtractSubject.size() + " subjects(totally " + extractSubjectSet.size() + ") to raw sdf file.");
            }
        }
        return indexOfExtractSubject;
    }

    private IndexableSet<ByteCode> parseSubjectName() throws IOException {
        VolumeByteStream cache = new VolumeByteStream();
        FileStream fs = new FileStream(subjectRecordFile, FileStream.DEFAULT_READER);
        IndexableSet<ByteCode> extractSubjectSet = new IndexableSet<>();
        while (fs.readLine(cache) != -1) {
            if (cache.startWith(ByteCode.NUMBER_SIGN)) {
                cache.reset();
                continue;
            }
            if (isPedFile) {
                ByteCode line = cache.toByteCode();
                BaseArray<ByteCode> split = line.split(ByteCode.TAB);
                extractSubjectSet.add(split.get(1).trim().asUnmodifiable());
            } else if (separate == null) {
                extractSubjectSet.add(cache.toByteCode().trim().asUnmodifiable());
            } else {
                ByteCode line = cache.toByteCode();
                BaseArray<ByteCode> subjects = line.split(separate);
                for (ByteCode subject : subjects) {
                    extractSubjectSet.add(subject.trim().asUnmodifiable());
                }
            }
            cache.reset();
        }
        fs.close();
        cache.close();
        logger.info("Totally collect " + extractSubjectSet.size() + " subjects from files.");
        return extractSubjectSet;
    }

    public SDFExtract setSeparate(String separate) {
        if (separate.equals("\n")) {
            return this;
        }
        this.separate = new ByteCode(separate);
        return this;
    }

    public SDFExtract addGtyFilterForSV(Function<SVGenotype[], Boolean> filterCondition, String description) {
        this.filterConditionArray.add(new Entry<>(filterCondition, description));
        this.filterCount.add(0);
        return this;
    }

    public SDFExtract setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public SDFExtract isPedFile(boolean isPedFile) {
        this.isPedFile = isPedFile;
        return this;
    }

    public static void main(String[] args) throws IOException {
        new VCF2SDF(
                "/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_10md_PBCCS/sniffles_test.vcf",
                "/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_10md_PBCCS/sniffles.sdf"
        ).setEncodeMode(1).convert();
        SDFExtract.of(
                "/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_10md_PBCCS/sniffles.sdf",
                "/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_10md_PBCCS/subject_extract_test.txt",
                "/Users/wenjiepeng/Desktop/SV/SVMerge"
        ).storeAllHomGtys(false).submit();
        SDFViewer.view(new File("/Users/wenjiepeng/Desktop/SV/SVMerge/sniffles.sdf"));
    }
}
