package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.annotation.rule.OptionRule;
import edu.sysu.pmglab.commandParser.annotation.rule.Rule;
import edu.sysu.pmglab.commandParser.rule.EQUAL;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
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
public class SDF2BED {
}
