package edu.sysu.pmglab.test.tools;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDF2Plink;
import edu.sysu.pmglab.sdfa.toolkit.SDFExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-06-10 11:33
 * @description
 */
public class SDFExtractTest {
    private static final Logger logger = LoggerFactory.getLogger(SDFExtract.class);
    public static void main(String[] args) throws IOException {
        File subjectRecordFile = new File("/Users/wenjiepeng/Desktop/SDFA/G30/G30_fam.ped");
        File outputDir = new File("/Users/wenjiepeng/Desktop/SDFA/E11");
//        SDFExtract.of(
//                "/Users/wenjiepeng/Desktop/SDFA/concatResult.sdf",
//                subjectRecordFile,
//                outputDir
//        ).setLogger(logger).isPedFile(true).submit();
        SDF2Plink.of(
                "/Users/wenjiepeng/Desktop/SDFA/G30/extract_concatResult.sdf",
               "/Users/wenjiepeng/Desktop/SDFA/G30"
        ).setPedFile(subjectRecordFile).submit();
    }
}
