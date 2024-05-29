package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDFExtract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-05-29 00:44
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"extract <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Extract some specific subjects from all subjects in raw SDF file."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"extract", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-f", "--file"},
                        type = {File.class},
                        description = "Specify the SDF file path."
                ),
                @Option(
                        names = {"-dir", "--dir"},
                        type = {File.class},
                        description = "Specify the output dir of result file for extraction."
                ),
                @Option(
                        names = {"--extract-subject"},
                        type = {File.class},
                        description = "Specify the subjects name for extraction."
                ),
                @Option(
                        names = {"--store-fullHom"},
                        type = Void.class,
                        description = "Decide whether stores the the genotype with full homozygous."
                )
        }
)
public class SDFExtractProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger(SDFExtractProgram.class);

    protected SDFExtractProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        SDFExtract.of(
                options.value("-f"),
                options.value("--extract-subject"),
                options.value("-dir")
        ).storeAllHomGtys(options.passed("--store-fullHom")).submit();
    }
}
