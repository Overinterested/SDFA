package edu.sysu.pmglab.sdfa.annotation.collector;

import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.IntervalResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.RefSVResourceManager;
import edu.sysu.pmglab.sdfa.annotation.genome.RefGeneManager;
import edu.sysu.pmglab.sdfa.annotation.genome.SlideGeneResourceReaderForOutput;
import edu.sysu.pmglab.sdfa.annotation.output.SlideResourceReaderForOutputter;
import edu.sysu.pmglab.sdfa.annotation.preprocess.AnnotationResourceMeta;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;

import java.io.IOException;


/**
 * @author Wenjie Peng
 * @create 2024-03-31 20:24
 * @description
 */
public abstract class AbstractResourceManager {
    int resourceIndex;
    File resourcePath;
    String resourceType;
    OutputFrame outputFrame;
    AnnotationResourceMeta resourceMeta;
    SlideResourceReaderForOutputter outputter;
    public static final String GENOME_ANNOTATION_RESOURCE = "genome";
    public static final String INTERVAL_ANNOTATION_RESOURCE = "interval";
    ContigBlockContainer contigBlockContainer = new ContigBlockContainer();
    public static final String SV_DATABASE_ANNOTATION_RESOURCE = "svdatabase";

    public AbstractResourceManager(File resourcePath, String resourceType) {
        this.resourcePath = resourcePath;
        this.resourceType = resourceType;
    }

    abstract public void buildCoordinateTree() throws IOException;

    abstract public void clearCoordinate();

    abstract public void checkResourceAndOutputFrame(File outputDir);

    public static AbstractResourceManager of(File resourcePath, String resourceType) {
        String lowerCase = resourceType.toLowerCase();
        AbstractResourceManager res;
        switch (lowerCase) {
            case "genome":
                res = new RefGeneManager(resourcePath, GENOME_ANNOTATION_RESOURCE);
                break;
            case "interval":
                res = new IntervalResourceManager(resourcePath, INTERVAL_ANNOTATION_RESOURCE);
                break;
            case "svdatabase":
                res = new RefSVResourceManager(resourcePath, SV_DATABASE_ANNOTATION_RESOURCE);
                break;
            default:
                throw new UnsupportedOperationException("Resource type only support `genome`, `interval` and `svdatabase`. "
                        + resourceType + " can't be parsed.");
        }
        return res;
    }

    public File getResourcePath() {
        return resourcePath;
    }

    public String getResourceType() {
        return resourceType;
    }

    public AbstractResourceManager setResourcePath(File resourcePath) {
        this.resourcePath = resourcePath;
        return this;
    }

    public OutputFrame getOutputFrame() {
        return outputFrame;
    }

    public AbstractResourceManager setResourceIndex(int resourceIndex) {
        this.resourceIndex = resourceIndex;
        return this;
    }

    public AbstractResourceManager setOutputFrame(OutputFrame outputFrame) {
        this.outputFrame = outputFrame;
        return this;
    }

    public AnnotationResourceMeta getResourceMeta() {
        return resourceMeta;
    }

    public int getResourceIndex() {
        return resourceIndex;
    }

    public Array<Pipeline> getIndexAnnotationPipelinesOfMultiChr() {
        Array<Pipeline> pipelines = new Array<>();
        for (Chromosome chromosome : getContigBlockContainer().getAllChromosomes()) {
            Pipeline pipeline = new Pipeline(((status, context) -> annotate(chromosome, resourceIndex)));
            pipelines.add(pipeline);
        }
        return pipelines;
    }

    abstract public void annotate(Chromosome chr, int resourceIndex);

    public ContigBlockContainer getContigBlockContainer() {
        return contigBlockContainer;
    }

    public void outputMode() {
        if (this instanceof RefGeneManager) {
            outputter = new SlideGeneResourceReaderForOutput(this, outputFrame);
        } else {
            outputter = new SlideResourceReaderForOutputter(this, outputFrame);
        }
    }

    public AbstractResourceManager setResourceMeta(AnnotationResourceMeta resourceMeta) {
        this.resourceMeta = resourceMeta;
        return this;
    }


    public AbstractResourceManager setContigBlockContainer(ContigBlockContainer contigBlockContainer) {
        this.contigBlockContainer = contigBlockContainer;
        return this;
    }

    public SlideResourceReaderForOutputter getOutputter() {
        return outputter;
    }
}
