package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDFConcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-04-03 06:28
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"concat <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Concat all SV files into one."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"concat", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-dir", "--directory"},
                        type = {File.class},
                        description = "Specify the SDF file directory."
                ),
                @Option(
                        names = {"-o", "--output"},
                        type = {File.class},
                        description = "Specify the output dir of result file for extraction."
                ),
                @Option(names = "--threads", from = ThreadValidator.class)
        }
)
public class SDFConcatProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger(SDFConcatProgram.class);

    protected SDFConcatProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        SDFConcat.of((File) options.value("-dir"), (File) options.value("-o"))
                .setLogger(logger)
                .silent(false)
                .threads(options.value("--threads"))
                .submit();
    }

}
