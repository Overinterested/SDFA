package edu.sysu.pmglab.sdfa.annotation.index;

import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.executor.Workflow;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.GlobalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;

/**
 * @author Wenjie Peng
 * @create 2024-04-01 23:21
 * @description
 */
public class IndexAnnotator {
    File outputDir;
    int resourceSize;
    boolean loadResource;
    Array<SDFReader> annotateQueue;
    Array<SDFReader> finishAnnotateQueue;
    Array<Pipeline> tasks = new Array<>();
    GlobalResourceManager globalResourceManager;
    BriefSVAnnotationManager briefSVAnnotationManager = BriefSVAnnotationManager.getInstance();

    public IndexAnnotator() {
        this.annotateQueue = new Array<>();
        this.finishAnnotateQueue = new Array<>();
        this.globalResourceManager = GlobalResourceManager.getInstance();
        this.resourceSize = globalResourceManager.numOfResource();
    }

    public void execute(Workflow workflow) {
        this.outputDir = workflow.getParam("output");
        if (!loadResource) {
            loadCoordinateResource();
            executeTasks(workflow);
            loadResource = true;
        }
        // collect SVs
        collectSV();
        executeTasks(workflow);
        // annotate SVs with reference annotation
        annotate();
        executeTasks(workflow);
        // output index annotation result
        output();
        executeTasks(workflow);
    }

    void collectSV() {
        int numOfResource = GlobalResourceManager.getInstance().numOfResource();
        BriefSVAnnotationManager.getInstance().setFileSize(annotateQueue.size());
        BriefSVAnnotationManager.getInstance().setReaderArray(annotateQueue);
        for (int i = 0; i < annotateQueue.size(); i++) {
            int finalI = i;
            tasks.add(new Pipeline(
                    (status, context) -> {
                        SDFReader sdfReader = annotateQueue.get(finalI);
                        sdfReader.redirectSVFeature();
                        int lineCount = 0;
                        IRecord record;
                        while ((record = sdfReader.readRecord()) != null) {
                            briefSVAnnotationManager.updateSafety(
                                    sdfReader.getFileID(), record,
                                    lineCount++, false, numOfResource
                            );
                        }
                    }
            ));
        }
    }


    public void annotate() {
        int resourceSize = globalResourceManager.numOfResource();
        for (int i = 0; i < resourceSize; i++) {
            AbstractResourceManager resource = globalResourceManager.getResourceByIndex(i);
            tasks.addAll(resource.getIndexAnnotationPipelinesOfMultiChr());
        }
    }


    public IndexAnnotator setAnnotateQueue(Array<SDFReader> annotateQueue) {
        this.annotateQueue = annotateQueue;
        return this;
    }

    public void addAnnotateQueue(BaseArray<SDFReader> annotatedSDFWaitQueue) {
        this.annotateQueue.addAll(annotatedSDFWaitQueue);
    }

    public Array<SDFReader> getFinishAnnotateQueue() {
        return finishAnnotateQueue;
    }

    public void loadCoordinateResource() {
        for (int i = 0; i < globalResourceManager.numOfResource(); i++) {
            int finalI = i;
            tasks.add(new Pipeline((status, context) -> globalResourceManager
                    .getResourceByIndex(finalI)
                    .buildCoordinateTree()));
        }
    }

    public void output() {
        tasks.add(new Pipeline((status, context) ->
                BriefSVAnnotationManager.getInstance().toWriteMode()));
        tasks.add(Pipeline.BARRIER);
        tasks.addAll(BriefSVAnnotationManager.getInstance().writeOutTasks(outputDir));
    }

    public void executeTasks(Workflow workflow) {
        workflow.addTasks(tasks.popFirst(tasks.size())).execute();
        workflow.clearTasks();
    }
}
