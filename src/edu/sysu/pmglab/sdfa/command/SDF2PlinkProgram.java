package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDF2Plink;

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
                        names = {"--fam-id"},
                        type = String.class,
                        description = "Specify a family ID for all subjects in file. "
                ),
                @Option(
                        names = {"--fam-id-separator"},
                        type = String.class, defaultTo = "_",
                        description = "Specify a separator for parse family ID from subject name (default `_`)."
                )
        }
)
public class SDF2PlinkProgram extends CommandLineProgram {


    protected SDF2PlinkProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        Boolean control = options.passed("--control-sample") ? Boolean.TRUE : options.passed("--case-sample") ? Boolean.FALSE : null;
        SDF2Plink sdf2Plink = SDF2Plink.of(options.value("-f"), options.value("-o"))
                .setControlSample(control)
                .setFamilyID(new ByteCode());
        if (options.passed("--fam-id")) {
            sdf2Plink.setFamilyID(options.value("--fam-id"));
        } else {
            if (options.passed("--fam-id-separator")) {
                sdf2Plink.setSeparatorForFamily(new ByteCode((String) options.value("--fam-id-separator")));
            }
        }
        sdf2Plink.submit();
    }
}
