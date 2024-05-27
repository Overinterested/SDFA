package edu.sysu.pmglab.test.extract;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.sv.ComplexSV;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.AbstractComplexSVFilter;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.AbstractUnifiedSVFilter;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-24 03:14
 * @description
 */
public class ConvertTest {
    private static final Logger logger = LoggerFactory.getLogger(ConvertTest.class);

    public static void main(String[] args) throws IOException {
        LogBackOptions.init();
        LogBackOptions.addConsoleAppender(level -> level.isGreaterOrEqual(Level.INFO));
        LogBackOptions.addFileAppender("/Users/wenjiepeng/Desktop/SDFA/test/extract/1.log",
                level -> level.isGreaterOrEqual(Level.ALL));
        ;
        SVFilterManager svFilterManager = new SVFilterManager() {
            @Override
            protected void initSVLevelFilter() {
                svFilterManager.add(new AbstractUnifiedSVFilter() {
                    @Override
                    public boolean filter(UnifiedSV sv) {
                        return !Chromosome.get(sv.getCoordinate().getChr().getName()).equals(Chromosome.unknown);
                    }

                    @Override
                    public String getDescription() {
                        return "only contain chr1-chr22,X,Y";
                    }
                });
                svFilterManager.add(new AbstractComplexSVFilter() {
                    @Override
                    public boolean filter(ComplexSV sv) {
                        boolean flag;
                        for (UnifiedSV unifiedSV : sv.getSVs()) {
                            flag = Chromosome.get(unifiedSV.getCoordinate().getChr().getName()).equals(Chromosome.unknown);
                            if (flag) {
                                return false;
                            }
                        }
                        return true;
                    }

                    @Override
                    public String getDescription() {
                        return "only contain chr1-chr22,X,Y";
                    }
                });

            }

            @Override
            protected void initGenotypeFilter() {

            }

            @Override
            protected void initFieldFilter() {
            }
        };
        svFilterManager.init();
        SVFilterManager copy = svFilterManager.copy();
        new VCF2SDF(
                "/Users/wenjiepeng/Desktop/SV/SVMerge/trio/wm_10md_PBCCS/HG004vGRCh38_wm_10md_PBCCS_sniffles.s2l20.refined.nSVtypes.ism.vcf",
                "/Users/wenjiepeng/Desktop/SDFA/test/extract/test1")
                .setFileID(0)
                .setEncodeMode(0)
                .setFilter(copy)
                .setLogger(logger)
                .convert();
    }
}
