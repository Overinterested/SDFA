package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.SDFViewer;
import edu.sysu.pmglab.sdfa.toolkit.SDFIndex;

/**
 * @author Wenjie Peng
 * @create 2025-04-24 16:36
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"index <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Show the structure of SDF file."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"index", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-f", "--file"},
                        type = {String.class},
                        description = "Specify the SDF file path."
                ),
                @Option(
                        names = {"--search"},
                        type = String.class,
                        description = "Specify the position range"
                )
        }
)
public class SDFIndexProgram extends CommandLineProgram {
    protected SDFIndexProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        new SDFIndex().setRawSearch(options.value("--search"))
                .setFileName(options.value("-f"))
                .submit();
    }
}
