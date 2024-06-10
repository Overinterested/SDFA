package edu.sysu.pmglab.sdfa.command;

import ch.qos.logback.classic.Level;
import edu.sysu.pmglab.LogBackOptions;
import edu.sysu.pmglab.commandParser.CommandLineProgram;
import edu.sysu.pmglab.commandParser.annotation.Synopsis;
import edu.sysu.pmglab.commandParser.annotation.option.Option;
import edu.sysu.pmglab.commandParser.annotation.option.OptionGroup;
import edu.sysu.pmglab.commandParser.annotation.rule.OptionRule;
import edu.sysu.pmglab.commandParser.annotation.rule.Rule;
import edu.sysu.pmglab.commandParser.usage.DefaultUsageStyle;
import edu.sysu.pmglab.commandParser.validator.ThreadValidator;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.ngf.*;
import edu.sysu.pmglab.sdfa.sv.idividual.SubjectManager;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Synopsis(
        style = DefaultUsageStyle.STYLE_1,
        usage = {"ngf <input> [options]"},
        version = {"stable-2 (last edited on 2024.03.28)"},
        about = {"Annotate SV in numeric gene feature."}
)
@OptionGroup(
        items = {
                @Option(
                        names = {"ngf", ""},
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
                        names = "--hg38", type = Void.class,
                        description = "Choose hg38 genome version to annotate SVs."
                ),
                @Option(
                        names = "--hg19", type = Void.class,
                        description = "Choose hg19 genome version to annotate SVs."
                ),
                @Option(
                        names = "--sample-level", type = Void.class,
                        description = "Annotate SV in gene feature in population level."
                ),
                @Option(
                        names = "--sv-level", type = Void.class,
                        description = "Annotate SV in gene feature in population level."
                ),
                @Option(
                        names = "--gene-level", type = Void.class,
                        description = "Annotation SV in gene level with numeric gene feature."
                ),
                @Option(
                        names = "--rna-level", type = Void.class,
                        description = "Annotation SV in rna level with numeric gene feature."
                ),
                @Option(
                        names = "--show-coverage", type = Void.class,
                        description = "Show the coverage of specific region between SV and gene feature."
                ),
                @Option(
                        names = "--coverage-cutoff", type = byte[].class, defaultTo = "-1,-1,-1,-1,-1",
                        description = "Specify the coverage thresholds of 5 regions: exon, promoter, utr, intro, nearby." +
                                "And each threshold must be a byte value ranged in [0,100]"
                ),
                @Option(
                        names = "--filter-coding", type = Void.class,
                        description = "Only output the SVs affecting the coding feature."
                ),
                @Option(
                        names = "--filter-noncoding", type = Void.class,
                        description = "Only output the SVs affecting the noncoding feature."
                ),
                @Option(
                        names = {"--af-cutoff", "--AF-cutoff"}, type = float.class, defaultTo = "-1",
                        description = "Specify the AF threshold for sample level NGF result."
                )
        }
)
@OptionRule(
        basic = {
                @Rule(
                        items = {"--sample-level", "--sv-level"},
                        description = "Choose one of summary level as the unit of object for numeric gene feature."
                )
        }
)
public class SDFNGFProgram extends CommandLineProgram {
    private static final Logger logger = LoggerFactory.getLogger("SDFA NGF(Numeric Gene Feature) - Command Line");

    protected SDFNGFProgram(String[] args) {
        super(args);
    }

    @Override
    protected void work() throws Exception, Error {
        logger.info(options.toString());
        LogBackOptions.stop();
        LogBackOptions.reset();
        LogBackOptions.setLevel(Level.ALL);
        LogBackOptions.addConsoleAppender(level -> level.isGreaterOrEqual(Level.INFO));
        File outputDir = options.value("-o");
        outputDir.mkdirs();
        LogBackOptions.addFileAppender(outputDir.getSubFile("ngf.track").toString(),
                level -> level.isGreaterOrEqual(Level.ALL));
        if (options.passed("--coverage-cutoff")) {
            byte[] cutoffValues = options.value("--coverage-cutoff");
            if (cutoffValues.length == 5) {
                NumericTranscriptFeature.setCoverageCutOff(cutoffValues);
            } else {
                throw new UnsupportedOperationException("Input cutoff values' length is not equal 5.");
            }
        }
        if (options.passed("--filter-coding")) {
            NumericTranscriptFeature.setFilterForCoding((byte) 1);
        }
        if (options.passed("--filter-noncoding")) {
            NumericTranscriptFeature.setFilterForCoding((byte) 0);
        }
        if (options.passed("--af-cutoff")) {
            NumericGeneFeature.setAFCutOff(options.value("--af-cutoff"));
        }
        //region load SVs
        SDFManager.of(options.value("-dir"), options.value("-o")).setLogger(logger).collectSDF();
        //endregion
        File genomeFile = new File(options.passed("--hg19") ? "./resource/hg19_refGene.ccf" : "./resource/hg38_refGene.ccf");
        RefGeneManager of = RefGeneManager.of(genomeFile).setAnnotationLevel(GeneFeatureAnnotationType.HGVS_GENE_LEVEL);
        GlobalResourceManager.getInstance().putResource(of);
        FeatureType featureType = options.passed("--rna-level") ? FeatureType.RNA_LEVEL : FeatureType.GENE_LEVEL;
        boolean showCoverage = options.passed("--show-coverage");
        if (options.passed("--sample-level")) {
            logger.info("Start calculate NGF in sample level.");
            // sample level ngf
            of.load();
            // init parameters
            SDFGlobalContig.Builder.getInstance().build();
            // annotate and output6t7
            BriefSVAnnotationManager.getInstance().initChromosomes();
            BriefSVAnnotationManager.getInstance().load(SDFManager.getInstance().getSdfReaderArray(), 1);
            of.annotateAll(0);
            BriefSVAnnotationManager.getInstance().toWriteMode();
            BriefSVAnnotationManager.getInstance().writeOut(outputDir);
            // ngf
            // init parameters
            of.setAnnotationLevel(GeneFeatureAnnotationType.NGF_GENE_LEVEL);
            of.load();
            NumericGeneFeature.initSubjectSize(SubjectManager.getInstance().numOfAllSubjects());
            PopulationLevelNumericGeneFeature populationLevelNumericGeneFeature = new PopulationLevelNumericGeneFeature(outputDir.getSubFile("gene_summary.txt"))
                    .setFeatureLevel(featureType)
                    .addSDFReaders(SDFManager.getInstance().getAnnotatedSDFArray())
                    .setGeneResourceManager(of);
            populationLevelNumericGeneFeature.execute();
            logger.info("Finish NGF function and see output dir: "+outputDir);
        } else {
            //region sv level ngf
            // load SV to brief coordinate
            logger.info("Start calculate NGF in SV level.");
            Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
            for (SDFReader sdfReader : sdfReaderArray) {
                sdfReader.setFileID(0);
                SDFGlobalContig.reset();
                SDFGlobalContig.Builder.getInstance().addVCFContig(sdfReader.getContig()).build();
                Array<SDFReader> array = Array.wrap(new SDFReader[]{sdfReader});
                BriefSVAnnotationManager.getInstance().clear();
                BriefSVAnnotationManager.getInstance().initChromosomes().load(array, 1);
                of.setAnnotationLevel(GeneFeatureAnnotationType.HGVS_GENE_LEVEL).load().annotateAll(0);
                BriefSVAnnotationManager.getInstance().toWriteMode();
                of.setAnnotationLevel(GeneFeatureAnnotationType.NGF_GENE_LEVEL).load();
                BriefSVAnnotationManager.getInstance().writeOut(outputDir);
                NumericGeneFeature.initSubjectSize(1);
                File subFile = outputDir.getSubFile(sdfReader.getFilePath().changeExtension("sdfa", "sdf").getName());
                SVLevelNumericGeneFeature svLevelNumericGeneFeature = new SVLevelNumericGeneFeature()
                        .setFeatureLevel(featureType)
                        .setOutputFile(new File(outputDir.getSubFile(subFile.getName() + ".ngf.txt")))
                        .setSDFReader(new SDFReader(subFile))
                        .setGeneResourceManager(of)
                        .containCoverage(showCoverage);
                svLevelNumericGeneFeature.execute();
            }
            //endregion
        }
    }
}
