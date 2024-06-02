package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.DoubleArray;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.SVTypeSign;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.unifyIO.FileStream;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Wenjie Peng
 * @create 2024-06-01 09:33
 * @description
 */
public class SDFInfo {
    File sdfDir;
    File sdfFile;
    File outputDir;
    private Logger logger;
    boolean calcAFOfEachSV;
    boolean countSVOfEachType;
    boolean countSVOfEachSubject;
    boolean listNameOfEachSubject;

    private SDFInfo(File sdfFileOrDir, File outputDir, boolean isFile) {
        if (isFile) {
            this.sdfFile = sdfFileOrDir;
        } else {
            this.sdfDir = sdfFileOrDir;
        }
        this.outputDir = outputDir;
    }

    public static SDFInfo of(Object infoObject, Object outputDir, boolean infoObjectIsFile) {
        File trueOutputDir = new File(outputDir.toString());
        if (!trueOutputDir.exists()) {
            boolean mkdirs = trueOutputDir.mkdirs();
            if (!mkdirs) {
                throw new UnsupportedOperationException("Output directory(" + trueOutputDir + ") can't be created");
            }
        }
        return new SDFInfo(new File(infoObject.toString()), trueOutputDir, infoObjectIsFile);
    }

    public void submit() throws IOException {
        int[] numOfSVInEachSubject = null;
        DoubleArray afOfAllSVs = new DoubleArray();
        int[] numOfEachSVType = new int[SVTypeSign.support().size()];
        Array<File> sdfArray = new Array<>();
        if (sdfFile != null) {
            sdfArray.add(sdfFile);
        } else {
            sdfArray.addAll(FileTool.getFilesFromDirWithHandle(sdfDir, ".sdf"));
        }
        for (File file : sdfArray) {
            afOfAllSVs.clear();
            ProcessBar bar = new ProcessBar().setHeader("Collecting").setUnit("records").start();
            Arrays.fill(numOfEachSVType, 0);
            SDFReader reader = new SDFReader(file);
            Subjects subjects = reader.getMeta().getSubjects();
            int subjectSize = subjects.numOfSubjects();
            if (countSVOfEachSubject) {
                numOfSVInEachSubject = new int[subjectSize];
            }
            if (listNameOfEachSubject) {
                FileStream fs = new FileStream(outputDir.getSubFile("subject_name.txt"), FileStream.DEFAULT_WRITER);
                for (int i = 0; i < subjectSize; i++) {
                    fs.write(subjects.getSubject(i).getName());
                    fs.write(ByteCode.NEWLINE);
                }
                fs.close();
            }
            reader.redirectPlinkConvertFeatures();
            CCFReader reader1 = reader.getReader();
            IRecord record = reader1.getRecord();
            boolean nonHomGty;
            double numOfNoneHomGty;
            short homGtyEncoder = SVGenotype.homozygousVariantType.getEncoder();
            while (reader1.read(record)) {
                numOfNoneHomGty = 0;
                int type = record.get(2);
                numOfEachSVType[type] += 1;
                short[] genotypes = record.get(3);
                for (int i = 0; i < genotypes.length; i++) {
                    nonHomGty = genotypes[i] != homGtyEncoder;
                    if (nonHomGty) {
                        numOfNoneHomGty++;
                        if (countSVOfEachSubject) {
                            assert numOfSVInEachSubject != null;
                            numOfSVInEachSubject[i] += 1;
                        }
                    }
                }
                if (calcAFOfEachSV) {
                    afOfAllSVs.add(numOfNoneHomGty / genotypes.length);
                }
                bar.addProcessed(1);
            }
            bar.setFinish();
            if (logger != null) {
                logger.info("Starting output the summary result(s).");
            }
            if (countSVOfEachType) {
                FileStream fs = new FileStream(outputDir.getSubFile("sv_type_count.txt"), FileStream.DEFAULT_WRITER);
                for (int i = 0; i < SVTypeSign.support().size(); i++) {
                    if (numOfEachSVType[i] == 0) {
                        continue;
                    }
                    fs.write(SVTypeSign.getByIndex(i).getName());
                    fs.write(ByteCode.TAB);
                    fs.write(ValueUtils.Value2Text.int2bytes(numOfEachSVType[i]));
                    fs.write(ByteCode.NEWLINE);
                }
                fs.close();
            }
            if (calcAFOfEachSV) {
                FileStream fs = new FileStream(outputDir.getSubFile("af_of_each_SV.txt"), FileStream.DEFAULT_WRITER);
                for (int i = 0; i < afOfAllSVs.size(); i++) {
                    fs.write(ValueUtils.Value2Text.double2bytes(afOfAllSVs.getDouble(i), 6));
                    fs.write(ByteCode.NEWLINE);
                }
                fs.close();
            }
            if (countSVOfEachSubject) {
                FileStream fs = new FileStream(outputDir.getSubFile("subject_sv_count.txt"), FileStream.DEFAULT_WRITER);
                fs.write("#Subject_ID\tSVCount\n");
                for (int i = 0; i < Objects.requireNonNull(numOfSVInEachSubject).length; i++) {
                    fs.write(subjects.getSubject(i).getName());
                    fs.write(ByteCode.TAB);
                    fs.write(ValueUtils.Value2Text.int2bytes(numOfSVInEachSubject[i]));
                    fs.write(ByteCode.NEWLINE);
                }
                fs.close();
            }
        }
    }


    public SDFInfo countSVOfEachType(boolean countSVOfEachType) {
        this.countSVOfEachType = countSVOfEachType;
        return this;
    }

    public SDFInfo listNameOfEachSubject(boolean listNameOfEachSubject) {
        this.listNameOfEachSubject = listNameOfEachSubject;
        return this;
    }

    public SDFInfo calcAFOfEachSV(boolean calcAFOfEachSV) {
        this.calcAFOfEachSV = calcAFOfEachSV;
        return this;
    }

    public SDFInfo countSVOfEachSubject(boolean countSVOfEachSubject) {
        this.countSVOfEachSubject = countSVOfEachSubject;
        return this;
    }

    public SDFInfo setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public static void main(String[] args) throws IOException {
        SDFInfo.of("/Users/wenjiepeng/Desktop/tmp/extract_RAW_ukbb.sdf", "/Users/wenjiepeng/Desktop/tmp", true)
                .countSVOfEachType(true)
                .calcAFOfEachSV(true)
                .countSVOfEachSubject(true)
                .submit();
    }
}
