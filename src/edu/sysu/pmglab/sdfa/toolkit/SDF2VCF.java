package edu.sysu.pmglab.sdfa.toolkit;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.idividual.Subjects;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFile;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Date;

/**
 * @author Wenjie Peng
 * @create 2024-03-24 11:13
 * @description
 */
public class SDF2VCF {
    boolean loadFormat;
    final File sdfFile;
    final File vcfOutputFile;

    public SDF2VCF(Object sdfFile, Object vcfOutput) {
        this.sdfFile = new File(sdfFile.toString());
        this.vcfOutputFile = new File(vcfOutput.toString());
    }

    public File convert() throws IOException {
        FileStream fs = new FileStream(vcfOutputFile, FileStream.DEFAULT_WRITER);
        VolumeByteStream cache = new VolumeByteStream();
        SDFReader reader = new SDFReader(sdfFile);
        UnifiedSV sv = new UnifiedSV();
        Subjects subjects = reader.getMeta().getSubjects();
        fs.write("##fileformat=VCFv4.1\n");
        fs.write("##source=sdfa\n");
        fs.write("##fileDate=");
        fs.write(new Date().toString());
        fs.write("\n");
        fs.write(VCFFile.HEADER_COLUMN);
        for (int i = 0; i < subjects.numOfSubjects(); i++) {
            fs.write(subjects.getSubject(i).getName());
            if (i != subjects.numOfSubjects() - 1) {
                fs.write(ByteCode.TAB);
            }
        }
        fs.write(ByteCode.NEWLINE);
        int ID = 0;
        while (reader.read(sv) != null) {
            fs.write(sv.toCCFOutput(cache,ID++));
            fs.write(ByteCode.NEWLINE);
        }
        fs.close();
        return vcfOutputFile;
    }

    public static void main(String[] args) throws IOException {
        new SDF2VCF(
                "/Users/wenjiepeng/Downloads/extract_concatResult_new.sdf",
                "/Users/wenjiepeng/Downloads/extract_concatResult_new.sdf.vcf"
        )
                .convert();
    }
}
