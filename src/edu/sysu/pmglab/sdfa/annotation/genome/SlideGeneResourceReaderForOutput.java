package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.VolumeByteStream;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.output.SlideResourceReaderForOutputter;
import edu.sysu.pmglab.sdfa.annotation.toolkit.OutputFrame;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-04-19 19:49
 * @description
 */
public class SlideGeneResourceReaderForOutput extends SlideResourceReaderForOutputter {
    RefGeneManager refGeneManager;

    private final VolumeByteStream cache = new VolumeByteStream(4 * 1024);

    public SlideGeneResourceReaderForOutput(AbstractResourceManager resourceManager, OutputFrame outputFrame) {
        super(resourceManager, outputFrame);
        if (resourceManager instanceof RefGeneManager) {
            refGeneManager = (RefGeneManager) resourceManager;
        }
        try {
            refGeneManager.outputConfig();
        } catch (IOException e) {
            throw new UnsupportedOperationException("Encounter IO Stream Exception.");
        }
    }

    public ByteCode output(Chromosome chr, ByteCode src) {
        if (src.equals(ByteCode.EMPTY)) {
            return nullAnnotationRes;
        }
        Array<RefGeneName> refGeneNames = refGeneManager.getBriefChromosomeRefGeneHashMap().get(chr);
        GeneAnnotFeature geneAnnotFeature = (GeneAnnotFeature) indexParser.apply(src);
        cache.reset();
        cache.writeSafety(geneAnnotFeature.getStartOutput(refGeneNames));
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(geneAnnotFeature.getEndPosOutput(refGeneNames));
        cache.writeSafety(ByteCode.TAB);
        cache.writeSafety(geneAnnotFeature.getCoveredOutput(refGeneNames));
        if (cache.size() == 3) {
            return nullAnnotationRes;
        }
        return cache.toByteCode();
    }

    @Override
    public void reset() {
    }
}
