package edu.sysu.pmglab.sdfa.annotation;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.easytools.ProcessBar;
import edu.sysu.pmglab.easytools.wrapper.FileTool;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.index.IndexAnnotator;
import edu.sysu.pmglab.sdfa.annotation.output.UnifiedOutput;
import edu.sysu.pmglab.sdfa.toolkit.SDFGlobalContig;
import edu.sysu.pmglab.sdfa.toolkit.SDFManager;
import edu.sysu.pmglab.sdfa.toolkit.VCF2SDF;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author Wenjie Peng
 * @create 2024-04-01 21:59
 * @description
 */
public class SDFAAnnotator {
    Logger logger;
    File inputDir;
    File outputDir;
    final int thread;
    UnifiedOutput outputter;
    File annotationConfigFile;
    IndexAnnotator indexAnnotator;
    public static Workflow workflow;
    Array<SDFReader> allFullAnnotateWaitQueue;

    public SDFAAnnotator(File inputSVFileDir, File annotationConfigFile, File outputDir, int thread) {
        this.thread = thread;
        this.inputDir = inputSVFileDir;
        this.outputDir = outputDir;
        workflow = new Workflow(thread);
        indexAnnotator = new IndexAnnotator();
        workflow.setParam("output", outputDir);
        this.allFullAnnotateWaitQueue = new Array<>();
        this.annotationConfigFile = annotationConfigFile;
        outputter = new UnifiedOutput(FileTool.checkDirWithCreate(outputDir)).setThread(thread);
    }


    public void submit() throws IOException {
        check();
        // load less than thread to indexed-annotation
        logger.info("Start indexed annotating the coordinate of SVs.");
        ProcessBar bar = new ProcessBar(allFullAnnotateWaitQueue.size())
                .setHeader("SDF files annotating speed")
                .setUnit("files")
                .start();
        workflow.addTasks((status, context) -> context.put(ProcessBar.class, bar)).execute();
        workflow.clearTasks();
        int numOfAnnotatedFile;
        while ((numOfAnnotatedFile = loadIndexAnnotateFileQueue()) != -1) {
            indexAnnotator.execute(workflow);
            int finalNumOfAnnotatedFile = numOfAnnotatedFile;
            workflow.addTasks(((status, context) -> {
                ProcessBar processBar = (ProcessBar) context.get(ProcessBar.class);
                processBar.addProcessed(finalNumOfAnnotatedFile);
            })).execute();
            workflow.clearTasks();
        }
        workflow.addTasks(((status, context) -> {
            ((ProcessBar) context.get(ProcessBar.class)).setFinish();
            clearRefIndex();
        })).execute();
        workflow.clearTasks();
        outputter.setOutputQueue(SDFManager.getInstance().getAnnotatedSDFArray())
                .execute(workflow);
    }

    /**
     * prepare SV(in sdf) and annotation resources
     *
     */
    public void check() throws IOException {
        // region load resource
        // load vcf resource
        workflow.setParam(Logger.class, logger);
        workflow.setParam("output", outputDir);
        VCF2SDF.dropRefField = true;
        VCF2SDF.dropFormat = true;
        VCF2SDF.dropInfoField = true;
        VCF2SDF.dropFilterField = true;
        VCF2SDF.dropQualField = true;
        workflow.addTasks(SDFManager.of(inputDir, outputDir).setLogger(logger).collectTask(thread));
        GlobalResourceManager.getInstance().setConfigFile(annotationConfigFile).setOutputDir(outputDir);
        workflow.execute();
        workflow.clearTasks();
        // load annotation resource
        workflow.addTasks((status, context) -> GlobalResourceManager.getInstance().setLogger(logger).init());
        //endregion
        workflow.execute();
        workflow.clearTasks();
        //region check resource size
        if (SDFManager.getInstance().getSdfReaderArray().isEmpty()) {
            throw new UnsupportedOperationException(inputDir + " has no SV files to be annotated.");
        }
        if (GlobalResourceManager.getInstance().numOfResource() == 0) {
            throw new UnsupportedOperationException("No annotation resource is passed.");
        }
        //endregion
        allFullAnnotateWaitQueue.addAll(SDFManager.getInstance().getSdfReaderArray());
        allFullAnnotateWaitQueue.sort(Comparator.comparingInt(SDFReader::getFileID));
        SDFGlobalContig.Builder.getInstance().build();
    }

    int loadIndexAnnotateFileQueue() {
        if (allFullAnnotateWaitQueue.isEmpty()) {
            return -1;
        }
        BaseArray<SDFReader> toAnnotateArray;
        if (allFullAnnotateWaitQueue.size() >= thread) {
            toAnnotateArray = allFullAnnotateWaitQueue.popFirst(thread);
            indexAnnotator.addAnnotateQueue(toAnnotateArray);
        } else {
            toAnnotateArray = allFullAnnotateWaitQueue.popFirst(allFullAnnotateWaitQueue.size());
            indexAnnotator.addAnnotateQueue(toAnnotateArray);
        }
        return toAnnotateArray.size();
    }

    void clearRefIndex() {
        GlobalResourceManager.getInstance().clearIndex();
    }

    public SDFAAnnotator setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }
}
