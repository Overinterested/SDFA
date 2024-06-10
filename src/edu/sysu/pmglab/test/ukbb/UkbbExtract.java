package edu.sysu.pmglab.test.ukbb;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.sdfa.SDFFormat;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-05-18 09:23
 * @description
 */
public class UkbbExtract extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger("UKBB Extract - Command Line");

    protected UkbbExtract(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        File dir = options.value("-dir");
        File outputDir = options.value("-o");
        int encodeMode = options.value("-c");
        Array<File> vcfFiles = FileTool.getFilesFromDirWithValid(dir, "vcf", "vcf.bgz", "vcf.gz", "vcf.gtb");
        if (vcfFiles.isEmpty()) {
            logger.error("No vcf files in " + dir);
            return;
        }
        LogBackOptions.addFileAppender(
                outputDir.getSubFile("track.log").toString(),
                level -> level.isGreaterOrEqual(Level.ALL)
        );
        ProcessBar bar = new ProcessBar(vcfFiles.size()).setUnit("file(s)").setHeader("Convert Speed").start();
        for (File vcfFile : vcfFiles) {
            new VCF2SDF(vcfFile, outputDir.getSubFile(vcfFile.getName() + SDFFormat.EXTENSION))
                    .setLogger(logger)
                    .setEncodeMode(encodeMode)
                    .convert();
            bar.addProcessed(1);
        }
        bar.setFinish();
        logger.info("Finish " + vcfFiles.size() + " vcf files extraction.");
    }
}
