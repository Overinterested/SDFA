package edu.sysu.pmglab.test;

import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-22 08:25
 * @description
 */
public class FilterExtract {
    static Logger logger = LoggerFactory.getLogger(FilterExtract.class);

    public static void main(String[] args) throws IOException {
        SVFilterManager svFilterManager = new SVFilterManager() {
            /**
             * init sv level management: use svFilterManager.add(filter) to design your function
             */
            @Override
            protected void initSVLevelFilter() {
                return;
            }

            /**
             * init genotype level management: use genotypeFilterManager.add(filter) to design your function
             */
            @Override
            protected void initGenotypeFilter() {
                return;
            }

            @Override
            protected void initFieldFilter() {

            }
        };
        new VCF2SDF(
                "/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/cutesv2_output/HG002_HiFi_aligned_GRCh38_winnowmap.cuteSV2.vcf",
                "/Users/wenjiepeng/Desktop/SV/data/private/VCF/curated_data/sv_calls_2023-06-10/test.sdf")
                .setLogger(logger)
                .setFilter(svFilterManager)
                .setFileID(1)
                .convert();
    }
}
