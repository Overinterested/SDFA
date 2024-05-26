package edu.sysu.pmglab.test.ukbb;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.CommandParser;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.SVGenotype;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.HardyWeinbergCalculator;
import edu.sysu.pmglab.sdfa.sv.vcf.quantitycontrol.sv.SVGenotypeFilter;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-05-18 09:08
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"vcf2sdf <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Convert vcf file(s) to sdf file(s)"}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"extract", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"--pool"},
                        type = Void.class, hidden = true,
                        description = "Specify whether applying the reusable vcf pool."
                ),
                @Option(
                        names = {"-c", "--compress-level"},
                        type = int.class,
                        defaultTo = "0",
                        description = "Specify the compressing level for SDF (0-3)."

                ),
                @Option(
                        names = {"-o", "--output"},
                        type = {File.class},
                        required = true,
                        description = "Specify the output directory."
                ),
                @Option(
                        names = {"-dir", "--directory"},
                        type = {File.class},
                        description = "Specify the input VCF directory, " +
                                "which SV files(including .vcf, .gz and .bgz) will be converted."
                ),
                @Option(names = "--threads", from = ThreadValidator.class),

        }
)
@OptionGroup(
        name = "filter",
        items = {
                @Option(
                        names = "--gty-qual", type = int.class, defaultTo = "-1",
                        description = "Filter genotypes which genotype quantity is less than value."
                ),
                @Option(
                        names = "--gty-dp", type = int.class, defaultTo = "-1",
                        description = "Exclude genotypes with the specif minimal read depth"
                ),
                @Option(
                        names = {"--hwe-test"}, type = Void.class,
                        description = "Exclude the SV whose hwe test fails."
                )
        }

)
public class UkbbFilter extends CommandLineProgram {
    private static final CommandParser parser = new CommandParser(UkbbFilter.class);
    private static final Logger logger = LoggerFactory.getLogger("UKBB Filter - Command Line");

    protected UkbbFilter(String[] args) {
        super(args);
    }


    @Override
    protected void work() throws Exception, Error {
        logger.info(options.toString());
        //region filter config
        int gqValueThreshold = options.value("--gty--qual");
        int dpValueThreshold = options.value("--gty--dp");
        boolean gqFilter = gqValueThreshold > 0;
        boolean dpFilter = dpValueThreshold > 0;
        boolean hweTest = options.passed("--hwe-test");
        SVFilterManager svFilterManager = null;
        if (gqFilter || dpFilter || hweTest) {
            svFilterManager = new SVFilterManager() {
                @Override
                protected void initSVLevelFilter() {
                }

                @Override
                protected void initGenotypeFilter() {
                    if (dpFilter) {
                        genotypeFilterManager.addFilter(
                                "DP",
                                dp -> {
                                    try {
                                        int dpValue = dp.toInt();
                                        return dpValue >= dpValueThreshold;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                },
                                "dp large than " + dpValueThreshold
                        );
                    }
                    if (gqFilter) {
                        genotypeFilterManager.addFilter(
                                "GQ",
                                gqByteCode -> {
                                    try {
                                        return gqByteCode.toInt() > 20;
                                    } catch (NumberFormatException e) {
                                        return false;
                                    }
                                }, "GQ > " + gqValueThreshold
                        );
                    }
                }

                @Override
                protected void initFieldFilter() {
                    if (hweTest) {
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
                                return pValue <= 0.05;
                            }

                            @Override
                            public String getDescription() {
                                return "HWE test";
                            }
                        });
                    }
                }
            };
        }
        //endregion
        File dir = options.value("-dir");
        File outputDir = options.value("-o");
        int encodeMode = options.value("-c");
        Array<File> vcfFiles = FileTool.getFilesFromDirWithValid(dir, "vcf", "vcf.bgz", "vcf.gz", "vcf.gtb");
        if (vcfFiles.isEmpty()) {
            logger.error("No vcf files in " + dir);
            return;
        }
        LogBackOptions.addFileAppender(
                outputDir.getSubFile("track.log").toString(),
                level -> level.isGreaterOrEqual(Level.ALL)
        );
        ProcessBar bar = new ProcessBar(vcfFiles.size()).setUnit("file(s)").setHeader("Convert Speed").start();
        for (File vcfFile : vcfFiles) {
            VCF2SDF vcf2SDF = new VCF2SDF(vcfFile, outputDir.getSubFile(vcfFile.getName() + SDFFormat.EXTENSION))
                    .setLogger(logger)
                    .setEncodeMode(encodeMode)
                    .setFilter(svFilterManager);
            if (svFilterManager!=null){
                vcf2SDF.setFilter(svFilterManager.copy());
            }
            vcf2SDF.convert();
            bar.addProcessed(1);
        }
        bar.setFinish();
        logger.info("Finish " + vcfFiles.size() + " vcf files extraction and filter.");
    }
}
