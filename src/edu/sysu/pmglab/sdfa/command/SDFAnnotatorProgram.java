package edu.sysu.pmglab.sdfa.command;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.annotation.SDFAAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"annotate <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Annotate SV files with multiply annotation resources files."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"annotate", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names = {"-o", "--output"},
                        from = Vcf2SdfProgram.class
                ),
                @Option(
                        names = {"-dir", "--directory"},
                        from = SDFMergerProgram.class
                ),
                @Option(names = "--threads", from = ThreadValidator.class),
                @Option(
                        names = "--annot-config", type = File.class,
                        description = "Specify the annotation config path."
                ),
                @Option(names = "--unified-output", type = Void.class,
                        description = "Choose to output into one file."
                )
        }
)
public class SDFAnnotatorProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger("SDFA Annotation - Command Line");

    protected SDFAnnotatorProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        File outputDir = options.value("-o");
        outputDir.mkdirs();
        LogBackOptions.addFileAppender(outputDir.getSubFile("annotation.track").toString(),
                level -> level.isGreaterOrEqual(Level.ALL));
        logger.info(options.toString());
        new SDFAAnnotator(
                options.value("-dir"),
                options.value("--annot-config"),
                options.value("-o"),
                options.value("--threads")
        ).setLogger(logger).submit();
    }
}
