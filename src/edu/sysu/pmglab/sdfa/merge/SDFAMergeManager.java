package edu.sysu.pmglab.sdfa.merge;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.merge.cmo.*;
import edu.sysu.pmglab.sdfa.toolkit.GlobalVCFContigConvertor;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-03-18 08:44
 * @description
 */
public class SDFAMergeManager {
    int thread;
    File inputDir;
    File outputDir;
    Workflow workflow;
    private Logger logger;
    static boolean isParallel;
    public static int fileSize = -1;
    private AbstractSVCollector collector;
    private AbstractSVMergeStrategy merger;
    private AbstractSVMergeOutput outputGenerator;
    SVMergeMode mergeMode = SVMergeMode.POS_LEVEL;
    SVCollectorMode collectorMode = SVCollectorMode.CHR_LEVEL;
    SVMergedOutputMode outputMode = SVMergedOutputMode.VCF_MODE;

    public void submit(int thread) throws IOException {
        workflow = new Workflow(thread);
        workflow.setParam(Logger.class, logger);
        initMode();
        initInput(thread);
        initOutput();
        boolean first = true;
        while (true) {
            if (AbstractSVCollector.getChromosome().equals(Chromosome.unknown)) {
                workflow.addTasks(((status, context) -> {
                    merger.acceptAllCompleteSVsFromCollection();
                    merger.merge();
                    outputGenerator.acceptMergedSV();
                    Array<AbstractSVMergeStrategy.MergedSV> mergedSVArray = outputGenerator.getMergedSVArray();
                    if (mergedSVArray.size() != 0) {
                        Chromosome chromosome = AbstractSVMergeStrategy.getChromosome();
                        logger.info(chromosome.getName() + " collects " + mergedSVArray.size() + " SVs.");
                    }
                    outputGenerator.writeAll();
                    outputGenerator.endWrite();
                }));
                workflow.execute();
                workflow.clearTasks();
                break;
            } else {
                if (first) {
                    workflow.addTasks(new Pipeline(((status, context) -> collector.collect())));
                    first = false;
                    workflow.addTasks(Pipeline.BARRIER);
                }
                workflow.addTasks(((status, context) -> merger.acceptAllCompleteSVsFromCollection()));
                workflow.execute();
                workflow.clearTasks();
                workflow.addTasks(((status, context) -> {collector.collect();}));
                workflow.addTasks(((status, context) -> {
                    merger.merge();
                    outputGenerator.acceptMergedSV();
                    Array<AbstractSVMergeStrategy.MergedSV> mergedSVArray = outputGenerator.getMergedSVArray();
                    if (mergedSVArray.size() != 0) {
                        Chromosome chromosome = AbstractSVMergeStrategy.getChromosome();
                        logger.info(chromosome.getName() + " collects " + mergedSVArray.size() + " SVs.");
                    }
                    outputGenerator.writeAll();
                }));
                workflow.execute();
                workflow.clearTasks();
            }
        }
    }

    public void submit() throws IOException {
        initMode();
        initInput();
        initOutput();
        // no parallel
        isParallel = false;
        while (true) {
            collector.collect();
            merger.acceptAllCompleteSVsFromCollection();
            merger.merge();
            outputGenerator.acceptMergedSV();
            outputGenerator.writeAll();
            if (AbstractSVCollector.getChromosome().equals(Chromosome.unknown)) {
                outputGenerator.endWrite();
                break;
            }
        }
    }

    /**
     * specify the mode od collect, merge and output
     */
    public void initMode() {
        merger = mergeMode.getMergeStrategy();
        outputGenerator = outputMode.getOutput();
        collector = collectorMode.getCollector();
    }

    /**
     * get sdf files of input and parse them
     *
     * @throws IOException when no sdf files, return
     */
    public void initInput() throws IOException {
        VCF2SDF.dropRefField = true;
//        VCF2SDF.dropAltField = true;
        VCF2SDF.dropQualField = true;
        VCF2SDF.dropFilterField = true;
        VCF2SDF.dropFormat = true;
        SDFManager.of(inputDir, outputDir).collectSDF();
        Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
        int sdfReaderSize = sdfReaderArray.size();
        if (sdfReaderSize == 0) {
            throw new UnsupportedOperationException("No SV files is passed.");
        }
        SDFAMergeManager.fileSize = sdfReaderSize;
        for (SDFReader sdfReader : sdfReaderArray) {
            sdfReader.redirectSVFeature();
            sdfReader.close();
        }
        GlobalVCFContigConvertor.Builder.getInstance().build();
        collector.buildCSVAssembly();
        collector.setSdfReaderArray(sdfReaderArray);
    }

    public void initInput(int thread) throws IOException {
        VCF2SDF.dropFormat = true;
        VCF2SDF.dropRefField = true;
        VCF2SDF.dropQualField = true;
        VCF2SDF.dropFilterField = true;
        workflow.addTasks(SDFManager.of(inputDir, outputDir).setLogger(logger).collectTask(thread));
        workflow.execute();
        workflow.clearTasks();
        Array<SDFReader> sdfReaderArray = SDFManager.getInstance().getSdfReaderArray();
        int sdfReaderSize = sdfReaderArray.size();
        if (sdfReaderSize == 0) {
            throw new UnsupportedOperationException("No SV files is passed.");
        }
        logger.info("Load " + sdfReaderArray.size() + " SV files for merge.");
        SDFAMergeManager.fileSize = sdfReaderSize;
        for (SDFReader sdfReader : sdfReaderArray) {
            sdfReader.redirectSVFeature();
            sdfReader.close();
        }
        GlobalVCFContigConvertor.Builder.getInstance().build();
        collector.buildCSVAssembly();
        collector.setSdfReaderArray(sdfReaderArray);
    }

    /**
     * check output dir with create and create a random file in
     */
    public void initOutput() throws IOException {
        outputGenerator.initOutput(outputDir);
        outputGenerator.initHeader();
    }


    public SDFAMergeManager setInputDir(Object inputDir) {
        this.inputDir = new File(inputDir.toString());
        return this;
    }

    public SDFAMergeManager setOutputDir(Object outputDir) {
        this.outputDir = new File(outputDir.toString());
        return this;
    }

    public SDFAMergeManager setCollectorMode(SVCollectorMode collectorMode) {
        this.collectorMode = collectorMode;
        return this;
    }

    public SDFAMergeManager setMergeMode(SVMergeMode mergeMode) {
        this.mergeMode = mergeMode;
        return this;
    }

    public SDFAMergeManager setOutputMode(SVMergedOutputMode outputMode) {
        this.outputMode = outputMode;
        return this;
    }

    public SDFAMergeManager setThread(int thread) {
        this.thread = thread;
        isParallel = thread > 1;
        return this;
    }

    public synchronized static boolean isIsParallel() {
        return isParallel;
    }

    public SDFAMergeManager setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
}
