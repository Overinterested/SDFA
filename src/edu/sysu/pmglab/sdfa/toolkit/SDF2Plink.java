package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.easytools.ValueUtils;
import edu.sysu.pmglab.sdfa.SDFMeta;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Arrays;

public class SDF2Plink {
    File sdfFile;
    File outputDir;
    ByteCode familyID;
    Boolean controlSample;
    ByteCode separatorForFamily;

    private static final byte[] BED_HEADER = {0x6C, 0x1B, 0x01};
    private static final byte[] FATHER_MOTHER_SEX_TAG = {ByteCode.ZERO, ByteCode.TAB, ByteCode.ZERO, ByteCode.TAB, ByteCode.ZERO};
    private static final byte[] default_family_id = new byte[]{ByteCode.F, ByteCode.a, ByteCode.m, ByteCode.I, ByteCode.D};

    public void submit() throws IOException {
        SDFReader sdfReader = new SDFReader(sdfFile);
        SDFMeta meta = sdfReader.getMeta();
        writeFamFile(meta.getSubjects());
        int subjectSize = meta.getSubjects().numOfSubjects();
        FileStream bimFile = new FileStream(outputDir.getSubFile(".bim"), FileStream.DEFAULT_WRITER);
        FileStream bedFile = new FileStream(outputDir.getSubFile(".bed"), FileStream.DEFAULT_WRITER);
        bedFile.write(BED_HEADER);
        UnifiedSV sv = new UnifiedSV();
        while (sdfReader.read(sv) != null) {
            // write bim: contig, ID, 0, POS, REF, ALT
            bimFile.write(sv.getChr().getName());
            bimFile.write(ByteCode.TAB);
            bimFile.write(sv.getID());
            bimFile.write(ByteCode.TAB);
            bimFile.write(ByteCode.ZERO);
            bimFile.write(ByteCode.TAB);
            bimFile.write(ValueUtils.Value2Text.int2bytes(sv.getPos()));
            bimFile.write(ByteCode.TAB);
            bimFile.write(sv.getRef());
            bimFile.write(ByteCode.TAB);
            bimFile.write(sv.getAlt());
            bimFile.write(ByteCode.NEWLINE);
            // write bed: genotypes
            bedFile.write(gty2BEDBytes(sv.getGenotypes().getGenotypes(), subjectSize));
        }
        bimFile.close();
        bedFile.close();
    }

    private static byte[] gty2BEDBytes(SVGenotype[] gtys, int subjectSize) {
        byte[] genotypeBytes = new byte[(subjectSize + 3) / 4];
        Arrays.fill(genotypeBytes, (byte) 0x00);
        SVGenotype hom1 = SVGenotype.of(new ByteCode("0/0"));
        SVGenotype hom2 = SVGenotype.of(new ByteCode("0|0"));
        SVGenotype wildGty1 = SVGenotype.of(new ByteCode("1|1"));
        SVGenotype wildGty2 = SVGenotype.of(new ByteCode("1/1"));
        SVGenotype het1 = SVGenotype.of(new ByteCode("0/1"));
        SVGenotype het4 = SVGenotype.of(new ByteCode("0|1"));
        SVGenotype het2 = SVGenotype.of(new ByteCode("1/0"));
        SVGenotype het3 = SVGenotype.of(new ByteCode("1|0"));
        byte genoCode;
        for (int i = 0; i < gtys.length; i++) {
            SVGenotype gty = gtys[i];
            if (gty == hom1 || gty == hom2) {
                // 0/0
                genoCode = 0b00;
            } else if (gty == wildGty1 || gty == wildGty2) {
                // 1/1
                genoCode = 0b11;
            } else if (gty == het1 || gty == het2 || gty == het3 || gty == het4) {
                // 0/1
                genoCode = 0b01;
            } else {
                // missing
                genoCode = 0b10;
            }
            genotypeBytes[i / 4] |= (genoCode << ((i % 4) * 2));
        }
        return genotypeBytes;
    }

    public void writeFamFile(Subjects subjects) throws IOException {
        File famFile = outputDir.getSubFile(".fam");
        CallableSet<Subject> subjectSet = subjects.getSubjectSet();
        FileStream fs = new FileStream(famFile, FileStream.DEFAULT_WRITER);
        VolumeByteStream cache = new VolumeByteStream();
        for (int i = 0; i < subjectSet.size(); i++) {
            Subject subject = subjectSet.getByIndex(i);
            ByteCode name = subject.getName();
            //region parse the family ID and sample ID
            if (familyID != null) {
                //region has defined family
                cache.writeSafety(familyID);
                cache.writeSafety(ByteCode.TAB);
                cache.writeSafety(name);
                //endregion
            } else {
                //region need parse for family id
                separatorForFamily = separatorForFamily == null ? new ByteCode(new byte[]{ByteCode.UNDERLINE}) : separatorForFamily;
                BaseArray<ByteCode> split = name.split(ByteCode.UNDERLINE);
                if (split.size() == 2) {
                    cache.writeSafety(split.get(0));
                    cache.writeSafety(ByteCode.TAB);
                    cache.writeSafety(split.get(1));
                } else {
                    if (split.size() > 2) {
                        cache.writeSafety(split.get(0));
                        cache.writeSafety(ByteCode.TAB);
                        cache.writeSafety(name.subByteCode(split.size()));
                    } else {
                        cache.writeSafety(default_family_id);
                        cache.writeSafety(ByteCode.TAB);
                        cache.writeSafety(name);
                    }
                }
                //endregion
            }
            cache.writeSafety(ByteCode.TAB);
            cache.writeSafety(FATHER_MOTHER_SEX_TAG);
            cache.writeSafety(ByteCode.TAB);
            if (controlSample == null) {
                cache.writeSafety(ByteCode.ZERO);
            } else {
                if (controlSample) {
                    cache.writeSafety(ByteCode.ONE);
                } else {
                    cache.writeSafety(ByteCode.TWO);
                }
            }
            cache.writeSafety(ByteCode.NEWLINE);
            fs.write(cache);
            cache.reset();
            //endregion
        }
        fs.close();
    }

    public static void main(String[] args) {

    }
}