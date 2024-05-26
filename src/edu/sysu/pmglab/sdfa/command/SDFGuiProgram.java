package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.SDFViewer;

/**
 * @author Wenjie Peng
 * @create 2024-04-03 06:25
 * @description
 */
@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"gui <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Show the structure of SDF file."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"gui", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-f", "--file"},
                        type = {File.class},
                        description = "Specify the SDF file path."
                ),
        }
)
public class SDFGuiProgram extends CommandLineProgram {
    protected SDFGuiProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        SDFViewer.view(options.value("-f"));
    }
}
