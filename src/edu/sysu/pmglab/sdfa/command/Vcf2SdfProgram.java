package edu.sysu.pmglab.sdfa.command;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.annotation.rule.OptionRule;
import edu.sysu.pmglab.commandParser.annotation.rule.Rule;
import edu.sysu.pmglab.commandParser.rule.EQUAL;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.sv.SVFilterManager;
import edu.sysu.pmglab.sdfa.sv.vcf.ReusableVCFPool;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"vcf2sdf <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Convert vcf file(s) to sdf file(s)"}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"vcf2sdf", ""},
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
                @Option(
                        names = "-f",
                        type = {File.class},
                        description = "Specify one input VCF file."
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
                )
        }

)
@OptionRule(
        basic = {
                @Rule(
                        items = {"-f", "-dir"},
                        count = 1,
                        rule = EQUAL.class,
                        description = "Only one input type(`File` or `Directory`) can be parsed."
                ),
        }
)
public class Vcf2SdfProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger("SDFA Convertor - Command Line");

    private Vcf2SdfProgram(String[] args) {
        super(args, true);
    }

    @Override
    protected void work() throws Exception, Error {
        File outputDir = options.value("-o");
        int compressMode = options.value("-c");
        //region output the track file
        LogBackOptions.addFileAppender(
                outputDir.getSubFile("track.log").toString(),
                level -> level.isGreaterOrEqual(Level.ALL)
        );
        //endregion
        int gq = options.passed("--gty-qual") ? options.value("--gty-qual") : -1;
        int dp = options.passed("--gty-dp") ? options.value("--gty-dp") : -1;
        SVFilterManager svFilterManager = new SVFilterManager() {
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
                if (dp != -1) {
                    genotypeFilterManager.addFilter(
                            "DP",
                            dpByteCode -> {
                                try {
                                    return dpByteCode.toInt() > dp;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }, "DP>" + dp);
                }
                if (gq != -1) {
                    genotypeFilterManager.addFilter(
                            "GQ",
                            gqByteCode -> {
                                try {
                                    return gqByteCode.toInt() > gq;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            }, "GQ>" + gq);
                }
            }

            @Override
            protected void initFieldFilter() {
            }
        };
        int thread = this.options.value("--threads");
        if (this.options.passed("-f")) {
            ReusableVCFPool.init(1);
            File vcfFile = options.value("-f");
            FileTool.checkDirWithCreate(outputDir);
            new VCF2SDF(
                    FileTool.checkFileExist(vcfFile, this.getLogger()),
                    outputDir.getSubFile(vcfFile.getName()).addExtension(".sdf")
            )
                    .setFileID(0)
                    .setLogger(this.getLogger())
                    .storeMeta(false)
                    .setFilter(svFilterManager)
                    .setLogger(logger)
                    .setEncodeMode(compressMode)
                    .convert();
            logger.info("Finish converting vcf file to sdf file.");
            return;
        }
        VCF2SDF.poolStrategy = options.passed("--pool");
        if (VCF2SDF.poolStrategy) {
            ReusableVCFPool.init(thread);
        }
        Workflow workflow = new Workflow(thread);
        File dir = options.value("-dir");
        File checkedOutput = FileTool.checkDirWithoutCreate(outputDir, logger);
        Array<File> vcfFiles = FileTool.getFilesFromDirWithValid(dir, "vcf", "vcf.gz", "vcf.bgz");
        logger.info("Collect " + vcfFiles.size() + " vcf format files in " + dir.getAbsolutePath());
        ProcessBar bar = new ProcessBar(vcfFiles.size())
                .setHeader("Convert VCF files to SDF files process")
                .setUnit("file(s)")
                .start();
        workflow.setParam(ProcessBar.class, bar);
        for (int i = 0; i < vcfFiles.size(); i++) {
            File vcfFile = vcfFiles.get(i);
            int finalI = i;
            workflow.addTasks(
                    (status, context) -> {
                        try {
                            SVFilterManager tmpFilter = svFilterManager.copy();
                            new VCF2SDF(vcfFile, checkedOutput.getSubFile(vcfFile.getName() + SDFFormat.EXTENSION))
                                    .setFileID(finalI)
                                    .setEncodeMode(compressMode)
                                    .setFilter(tmpFilter)
                                    .setLogger(logger)
                                    .convert();
                            tmpFilter.clear();
                            ((ProcessBar) context.cast(ProcessBar.class)).addProcessed(1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        workflow.execute();
        ((ProcessBar) workflow.getParam(ProcessBar.class)).setFinish();
        logger.info("Finish converting " + vcfFiles.size() + " vcf files to sdf files.");
    }

    public static void main(String[] args) {
        System.out.println((byte) 0b11100000);
    }

    public static void shuffleFiles(File[] files) {
        Random random = new Random();
        for (int i = files.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            File temp = files[i];
            files[i] = files[j];
            files[j] = temp;
        }
    }

}
