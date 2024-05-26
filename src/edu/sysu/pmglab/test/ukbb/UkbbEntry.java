package edu.sysu.pmglab.test.ukbb;

import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.CommandParser;
import edu.sysu.pmglab.commandParser.ICommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.annotation.rule.OptionRule;
import edu.sysu.pmglab.commandParser.annotation.rule.Rule;
import edu.sysu.pmglab.commandParser.rule.EQUAL;
import edu.sysu.pmglab.commandParser.splitter.EmptySplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-05-18 09:09
 * @description
 */
@Synopsis(
        usage = {"ukbb <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.10)"}
)
@OptionGroup(
        name = "SDFA Basic Options",
        items = {
                @Option(names = {"extract"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "extract <input> [options]",
                        description = {"Convert VCF file(s) to SDF file(s)."}
                ),
                @Option(names = {"annotate"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "annotate <input> [options]",
                        description = {"Annotate SV files(including vcf and sdf format) with annotation resources."}
                ),
                @Option(names = {"filter"}, type = {String[].class}, splitter = EmptySplitter.class,
                        greedy = true, arity = -1, format = "filter <input> [options]",
                        description = {"Filter SV files with specific filter conditions."}
                ),
        }
)
@OptionRule(
        basic = {@Rule(
                items = {"extract", "annotate", "filter"},
                count = 1,
                rule = EQUAL.class
        )}
)
public class UkbbEntry extends CommandLineProgram {
    public static final Logger logger = LoggerFactory.getLogger(UkbbEntry.class);
    public static final CommandParser parser = new CommandParser(UkbbEntry.class, LogBackOptions.class);

    protected UkbbEntry(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        LogBackOptions.init();
        try {
            ICommandLineProgram.execute(UkbbEntry.class, args);
        } catch (Error | Exception var2) {
            var2.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    protected void work() throws Exception, Error {
        if (this.options.passed("extract")) {
            ICommandLineProgram.execute(UkbbFilter.class, this.options.value("extract"));
        } else if (this.options.passed("annotate")) {
            ICommandLineProgram.execute(UkbbAnnotate.class, this.options.value("annotate"));
        } else if (this.options.passed("filter")) {
            ICommandLineProgram.execute(UkbbExtract.class, this.options.value("concat"));
        }
    }
}
