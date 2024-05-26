package edu.sysu.pmglab.sdfa.command;

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
import edu.sysu.pmglab.sdfa.merge.SDFAMergeManager;
import edu.sysu.pmglab.sdfa.merge.cmo.PosSVMergeStrategy;
import edu.sysu.pmglab.sdfa.merge.cmo.SVCollectorMode;
import edu.sysu.pmglab.sdfa.merge.cmo.SVMergeMode;
import edu.sysu.pmglab.sdfa.merge.cmo.SVMergedOutputMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"merge <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Merge individual-level SV files into one population-level file based on the position."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"merge", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-o", "--output"},
                        from = Vcf2SdfProgram.class
                ),
                @Option(
                        names = {"-dir", "--directory"},
                        type = {File.class},
                        description = "Specify the input VCF directory, which SV files(including .vcf, .gz and .bgz) will be converted."
                ),
                @Option(names = "--threads", from = ThreadValidator.class),
                @Option(names="--max--mergeSize", type = int.class, defaultTo = "1000",
                        description = "Specify the maximum merged range for SVs."
                )

        }
)

public class SDFMergerProgram extends CommandLineProgram {
    private static final CommandParser parser = new CommandParser(SDFMergerProgram.class);
    private static final Logger logger = LoggerFactory.getLogger("SDFA Merger - Command Line");

    private SDFMergerProgram(String[] args) {
        super(args, true);
    }

    @Override
    protected void work() throws Exception, Error {
        File outputDir = options.value("-o");
        LogBackOptions.addFileAppender(outputDir.getSubFile("merge_track.log").toString(),level -> level.isGreaterOrEqual(Level.ALL));
        int thread = this.options.value("--threads");
        PosSVMergeStrategy.mergePosRange = options.value("--max--mergeSize");
        logger.info(options.toString());
        new SDFAMergeManager()
                .setCollectorMode(SVCollectorMode.CHR_LEVEL)
                .setMergeMode(SVMergeMode.POS_LEVEL)
                .setOutputMode(SVMergedOutputMode.VCF_MODE)
                .setInputDir(options.value("-dir"))
                .setOutputDir(outputDir)
                .setLogger(logger)
                .submit(thread);
        logger.info("Finish the merging tasks.");
    }
}
