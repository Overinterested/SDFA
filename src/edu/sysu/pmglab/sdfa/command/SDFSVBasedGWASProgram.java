package edu.sysu.pmglab.sdfa.command;

import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.sdfa.toolkit.SDF2Plink;
import edu.sysu.pmglab.sdfa.toolkit.SDFConcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wenjie Peng
 * @create 2024-08-20 06:11
 * @description
 */
@Synopsis(
        usage = {"gwas <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.10)"}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"gwas", ""},
                        type = Void.class, hidden = true, required = true
                ),
                @Option(
                        names={"-dir"}, type =File.class, required = true,
                        description = "Specify the input directory"
                ),
                @Option(
                        names = {"-output","-o"}, type = File.class, required = true,
                        description = "Specify the output directory"
                ),
                @Option(
                        names = {"--concat"}, type = Void.class,
                        description = "Concat all SDF files into One."
                ),
                @Option(
                        names = {"--ped-file"}, type = File.class,
                        required = true,
                        description = "Specify the ped file for GWAS."
                ),
                @Option(
                        names = {"--filter-homogtys"}, type = Void.class,
                        description = "Specify whether to filter one SV whose all genotypes are homozygous"
                ),
                @Option(
                        names = {"--threads", "-t"}, type = int.class,
                        defaultTo = "1", validateWith = ThreadValidator.class
                )
        }
)
public class SDFSVBasedGWASProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger("SDFA GWAS - Command Line");

    protected SDFSVBasedGWASProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        int numOfThread = options.value("-t");
        File input = options.value("-dir");
        File output = options.value("-o");
        if (!output.exists()){
            output.mkdirs();
        }
        File concatFile = output.getSubFile("concatResult.sdf");
        if (options.passed("--concat")){
            logger.info("Start concat all sdf files:");
            SDFConcat.of(input, output)
                    .setLogger(logger)
                    .threads(numOfThread)
                    .silent(false)
                    .submit();
        }
        logger.info("Start convert sdf to plink format");
        SDF2Plink sdf2Plink = SDF2Plink.of(concatFile, output)
                .setControlSample(null)
                .setFamilyID(new ByteCode())
                .setPedFile(options.value("--ped-file"));
        sdf2Plink.submit();
        logger.info("Finish SDF2Plink program and then please conduct GWAS using Plink2.");
    }

//    public static void main(String[] args) {
//        new SDFSVBasedGWASProgram(
//                ("gwas -dir /Users/wenjiepeng/projects/sdfa_latest/test/resource/gwas "
//                + "-o /Users/wenjiepeng/projects/sdfa_latest/test/resource/gwas/output "
//                +"--concat --ped-file /Users/wenjiepeng/projects/sdfa_latest/test/resource/gwas/sample.ped "
//                + "-t 4").split(" ")
//        );
//    }
}
