package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.ICommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.annotation.rule.OptionRule;
import edu.sysu.pmglab.commandParser.annotation.rule.Rule;
import edu.sysu.pmglab.commandParser.rule.EQUAL;
import edu.sysu.pmglab.commandParser.splitter.EmptySplitter;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-03-28 07:48
 * @description
 */

@Synopsis(
        usage = {"sdf <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.10)"}
)
@OptionGroup(
        name = "SDFA Basic Options",
        items = {
                @Option(names = {"vcf2sdf"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "vcf2sdf <input> [options]",
                        description = {"Convert VCF file(s) to SDF file(s)."}
                ),
                @Option(names = {"gui"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "show <input> [options]",
                        description = {"Show the content of SDF file."}
                ),
                @Option(names = {"annotate"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "annotate <input> [options]",
                        description = {"Annotate SV files(including vcf and sdf format) with annotation resources."}
                ),
                @Option(names = {"merge"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "merge <input> [options]",
                        description = {"Merge individual SV files into one population SV file."}
                ),
                @Option(names = {"ngf"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "ngf <input> [options]",
                        description = {"Annotate SV files with numeric gene feature."}
                ),
                @Option(names = {"concat"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "concat <input> [options]",
                        description = {"Concatenate SV file(s) into one SDF file(s)."}
                ),
                @Option(names = {"extract"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "extract <input> [options]",
                        description = {"Extract some specific subjects from all subjects in raw SDF file.."}
                ),
                @Option(names = "--line-sort", type = Void.class, hidden = true,
                        description = "Convert VCF(s) to SDF(s) by first extracting line, then writing finally sorting"
                )

        }
)
@OptionRule(
        basic = {@Rule(
                items = {"vcf2sdf", "gui", "annotate", "merge", "ngf", "concat","extract"},
                count = 1,
                rule = EQUAL.class
        )}
)
public class SDFAEntry extends CommandLineProgram {
    public static final Logger logger = LoggerFactory.getLogger(SDFAEntry.class);

    public static void main(String[] args) {
        LogBackOptions.init();
        try {
            ICommandLineProgram.execute(SDFAEntry.class, args);
        } catch (Error | Exception var2) {
            var2.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void work() throws Exception, Error {
        VCF2SDF.lineExtractAndSort = options.passed("--line-sort");
        if (this.options.passed("vcf2sdf")) {
            ICommandLineProgram.execute(Vcf2SdfProgram.class, this.options.value("vcf2sdf"));
        } else if (this.options.passed("gui")) {
            ICommandLineProgram.execute(SDFGuiProgram.class, this.options.value("gui"));
        } else if (this.options.passed("merge")) {
            ICommandLineProgram.execute(SDFMergerProgram.class, this.options.value("merge"));
        } else if (this.options.passed("annotate")) {
            ICommandLineProgram.execute(SDFAnnotatorProgram.class, this.options.value("annotate"));
        } else if (this.options.passed("ngf")) {
            ICommandLineProgram.execute(SDFNAGFProgram.class, this.options.value("ngf"));
        } else if (this.options.passed("concat")) {
            ICommandLineProgram.execute(SDFConcatProgram.class, this.options.value("concat"));
        } else if (this.options.passed("extract")) {
            ICommandLineProgram.execute(SDFExtractProgram.class, this.options.value("extract"));
        } else if (this.options.passed("sdf2plink")){
            ICommandLineProgram.execute(SDF2PlinkProgram.class, this.options.value("sdf2plink"));
        }
    }

    private SDFAEntry(String[] args) {
        super(args, false);
    }
}
