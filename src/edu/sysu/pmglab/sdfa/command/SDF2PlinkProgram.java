package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.File;

/**
 * @author Wenjie Peng
 * @create 2024-05-29 11:43
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
                        names = {"sdf2plink", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-f", "-sdf"},
                        type = {File.class}, required = true,
                        description = "Specify a input sdf file."
                ),
                @Option(
                        names = {"-o", "--output"},
                        type = {File.class},
                        required = true,
                        description = "Specify the output directory."
                ),
                @Option(
                        names = {"--disable-bed", "--no-bed"},
                        type = Void.class,
                        description = "No output bed file."
                ),
                @Option(
                        names = {"--disable-fam", "--no-bed"},
                        type = Void.class,
                        description = "No output family file."
                ),
                @Option(
                        names = {"--disable-bim", "--no-bed"},
                        type = Void.class,
                        description = "No output bim file."
                )
        }
)
public class SDF2PlinkProgram {
}
