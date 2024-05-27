package edu.sysu.pmglab.sdfa.merge.cmo;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.sv.idividual.Subject;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.sv.vcf.VCFFileLatest;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;
import edu.sysu.pmglab.unifyIO.FileStream;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 09:38
 * @description
 */
public class VCFOutputGenerator extends AbstractSVMergeOutput {
    static FileStream fs;
    static File outputFile;
    AtomicInteger SVCount = new AtomicInteger(0);
    static final VolumeByteStream cache = new VolumeByteStream();

    @Override
    public void write(AbstractSVMergeStrategy.MergedSV mergedSV) throws IOException {
        fs.write(mergedSV.sv.toCCFOutput(cache, SVCount.intValue()));
        SVCount.incrementAndGet();
        fs.write(ByteCode.NEWLINE);
        cache.reset();
    }

    @Override
    public AbstractSVMergeOutput initOutput(File outputDir) {
        try {
            outputFile = outputDir.getSubFile(new Random(System.currentTimeMillis()).nextLong() + getOutputFormat());
            fs = new FileStream(outputFile, FileStream.DEFAULT_WRITER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public String getOutputFormat() {
        return ".vcf";
    }

    @Override
    public void initHeader() throws IOException {
        Array<File> vcfFileArray = SDFManager.getInstance().getVcfFileArray();
        if (vcfFileArray != null && !vcfFileArray.isEmpty()) {
            FileStream tmpVCF = new FileStream(vcfFileArray.get(0), FileStream.DEFAULT_READER);
            cache.reset();
            fs.write("##fileformat=VCFv4.1\n");
            fs.write("##source=sdfa\n");
            fs.write("##fileDate=");
            fs.write(new Date().toString());
            fs.write("\n");
            while(tmpVCF.readLine(cache)!=-1){
                if (cache.startWith(new byte[]{ByteCode.NUMBER_SIGN,ByteCode.NUMBER_SIGN})){
                    if (cache.toByteCode().startsWith("##fileformat")||
                    cache.toByteCode().startsWith("##source")||
                    cache.toByteCode().startsWith("##fileDate=")){
                        cache.reset();
                        continue;
                    }
                    fs.write(cache.toByteCode());
                    fs.write(ByteCode.NEWLINE);
                    cache.reset();
                    continue;
                }
                cache.reset();
                break;
            }
            tmpVCF.close();
        }
        fs.write("##INFO=<ID=CIEND,Number=2,Type=Integer,Description=\"PE confidence interval around SVLEN\"\n");
        fs.write("##INFO=<ID=SUPP_VEC,Number=1,Type=String,Description=\"Vector of supporting samples.\"\n");
        fs.write("##INFO=<ID=AVG_POS,Number=1,Type=Float,Description=\"Average of SV POS.\"\n");
        fs.write("##INFO=<ID=AVG_END,Number=1,Type=Float,Description=\"Average of SV END.\"\n");
        fs.write("##INFO=<ID=AVG_LEN,Number=1,Type=Float,Description=\"Average of SV LEN.\"\n");
        fs.write("##INFO=<ID=AF,Number=1,Type=Float,Description=\"Allele Frequency of SV\"\n");
        fs.write("##INFO=<ID=STDEV_POS,Number=1,Type=Float,Description=\"Average of SV POS.\"\n");
        fs.write("##INFO=<ID=STDEV_END,Number=1,Type=Float,Description=\"Average of SV END.\"\n");
        fs.write("##INFO=<ID=STDEV_LEN,Number=1,Type=Float,Description=\"Average of SV LEN.\"\n");
        fs.write("##INFO=<ID=IDLIST,Number=1,Type=String,Description=\"Raw ID list of merged SV(default not show).\"\n");
        cache.writeSafety(VCFFileLatest.HEADER_COLUMN);
        CallableSet<Subject> indexableSubjects = SubjectManager.getInstance().getIndexableSubjects();
        int size = indexableSubjects.size();
        for (int i = 0; i < size; i++) {
            cache.writeSafety(indexableSubjects.getByIndex(i).getName());
            if (i != size - 1) {
                cache.writeSafety(ByteCode.TAB);
            }
        }
        cache.writeSafety(ByteCode.NEWLINE);
        fs.write(cache.toByteCode());
        cache.reset();
    }

    @Override
    public void endWrite() throws IOException {
        fs.close();
    }
}
