package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDFInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-06-01 09:23
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"info <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Summary the details about SDF file."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"info", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-f", "--file"},
                        type = File.class,
                        description = "Specify the SDF file path."
                ),
                @Option(
                        names = {"-o", "--output"},
                        type = File.class, required = true,
                        description = "Specify the output directory of results."
                ),
                @Option(
                        names = {"-dir"},
                        type = File.class,
                        description = "Specify the storing directory of all SDF files."
                ),
                @Option(
                        names = {"--type-summary"},
                        type = Void.class,
                        description = "Summary counts of different SVs types"
                ),
                @Option(
                        names = {"--af-frequency"},
                        type = Void.class,
                        description = "Summary the AF information of all SVs."
                ),
                @Option(
                        names = {"--subject-gty"},
                        type = Void.class,
                        description = "Summary SVs count of each subject."
                ),
                @Option(
                        names = {"--subjects-list"},
                        type = Void.class,
                        description = "List names of all subjects in SDF files."
                )
        }
)
public class SDFInfoProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger(SDF2PlinkProgram.class);

    protected SDFInfoProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        SDFInfo.of(
                        options.passed("-f") ? options.value("-f") : options.value("-dir"),
                        options.value("-o"), options.passed("-f")
                ).setLogger(logger)
                .countSVOfEachType(options.passed("--type-summary"))
                .listNameOfEachSubject(options.passed("--subjects-list"))
                .countSVOfEachSubject(options.passed("--subject-gty"))
                .calcAFOfEachSV(options.passed("--af-frequency"))
                .submit();
    }
}
