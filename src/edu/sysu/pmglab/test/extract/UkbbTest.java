package edu.sysu.pmglab.test.extract;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.ReusableMap;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.HardyWeinbergCalculator;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.SVGenotypeFilter;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.SVInfoFieldFilter;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-17 02:46
 * @description
 */
public class UkbbTest {
    private static final Logger logger = LoggerFactory.getLogger(UkbbTest.class);

    public static void main(String[] args) throws IOException {
        long l = System.currentTimeMillis();
        SVFilterManager filterManager = new SVFilterManager() {
            /**
             * init sv level management: use svFilterManager.add(filter) to design your function
             */
            @Override
            protected void initSVLevelFilter() {

            }

            /**
             * init genotype level management: use genotypeFilterManager.add(filter) to design your function
             */
            @Override
            protected void initGenotypeFilter() {
                genotypeFilterManager.addFilter(
                        "DP",
                        dp -> {
                            try {
                                int dpValue = dp.toInt();
                                return dpValue >= 8;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        },
                        "dp large than 8"
                );
                genotypeFilterManager.addFilter(
                        "GQ",
                        gqByteCode -> {
                            try {
                                return gqByteCode.toInt() > 20;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }, "GQ >" + 20
                );
            }

            @Override
            protected void initFieldFilter() {
                fieldFilterManager.add(new SVInfoFieldFilter() {
                    @Override
                    public boolean filter(ReusableMap<ByteCode, ByteCode> reusableMap) {
                        ByteCode re = reusableMap.get(new ByteCode("RE"));
                        if (re == null){
                            return false;
                        }
                        try {
                            int num = re.toInt();
                            return num>=10;
                        }catch (NumberFormatException e){
                            return false;
                        }
                    }

                    @Override
                    public String getDescription() {
                        return "RE>=10";
                    }
                });
                fieldFilterManager.add(new SVGenotypeFilter() {
                    @Override
                    public boolean filter(SVGenotype[] genotypes) {
                        int aa = 0;
                        int bb = 0;
                        int ab = 0;
                        for (SVGenotype genotype : genotypes) {
                            if (genotype == SVGenotype.homozygousWildType) {
                                aa++;
                            } else if (genotype == SVGenotype.homozygousVariantType) {
                                bb++;
                            } else {
                                ab++;
                            }
                        }
                        double pValue = HardyWeinbergCalculator.hwCalculate(aa, ab, bb);
                        return pValue <= 0.01;
                    }

                    @Override
                    public String getDescription() {
                        return "HWE test";
                    }
                });
            }
        };
        File file = new File("/Users/wenjiepeng/Desktop/SV/data/ukbb/ukb23353_c1_b123_v1.vcf.gz");
        VCF2SDF.lineExtractAndSort = true;
        new VCF2SDF(file, "/Users/wenjiepeng/Desktop/SV/data/ukbb/ukbb.sdf")
                .setLogger(logger)
                .setEncodeMode(2)
                .setFilter(null)
                .convert();
        System.out.println(System.currentTimeMillis()-l);
    }
}
