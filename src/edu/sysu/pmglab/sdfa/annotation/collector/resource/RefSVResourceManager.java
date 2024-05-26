package edu.sysu.pmglab.sdfa.annotation.collector.resource;

import edu.sysu.pmglab.ccf.CCFFieldGroupMetas;
import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.ByteCode;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.SDFAAnnotator;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.preprocess.Tsv2AnnotateResource;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-03-31 20:20
 * @description
 */
public class RefSVResourceManager extends AbstractResourceManager {

    HashMap<Chromosome, RefSVResourceCoordinateTree> chromosomeResourceTreeMap;

    public static final CCFFieldGroupMetas FIX_INTERVAL_FIELD = new CCFFieldGroupMetas()
            .addField(CCFFieldMeta.of("Location::chr", FieldType.varInt32))
            .addField(CCFFieldMeta.of("Location::pos", FieldType.varInt32))
            .addField(CCFFieldMeta.of("Location::end", FieldType.varInt32))
            .addField(CCFFieldMeta.of("SVLen::length", FieldType.varInt32))
            .addField(CCFFieldMeta.of("SVType::type", FieldType.varInt32));


    public RefSVResourceManager(File resourcePath, String resourceType) {
        super(resourcePath, resourceType);
        chromosomeResourceTreeMap = new HashMap<>();
    }


    void update(IRecord record, int line) {
        Chromosome chr = getContigBlockContainer().getChromosomeByIndex(record.get(0));
        RefSVResourceCoordinateTree refSVResourceCoordinateTree = chromosomeResourceTreeMap.get(chr);
        if (refSVResourceCoordinateTree == null){
            refSVResourceCoordinateTree = new RefSVResourceCoordinateTree();
            chromosomeResourceTreeMap.put(chr, refSVResourceCoordinateTree);
        }
        refSVResourceCoordinateTree.update(record.get(1), record.get(2), record, line);
    }

    public int[] getOverlaps(Chromosome chromosome, BriefSVAnnotationFeature svAnnotationFeature) {
        return chromosomeResourceTreeMap.get(chromosome).getOverlap(svAnnotationFeature);
    }

    @Override
    public void checkResourceAndOutputFrame(File outputDir) {
        File resourcePath = getResourcePath();
        if (!resourcePath.withExtension("ccf")) {
            try {
                File convertedResource = new Tsv2AnnotateResource(resourcePath)
                        .setOutputDir(outputDir)
                        .loadHeader(true)
                        .isSVDatabase(true)
                        .setResource(resourcePath.getName())
                        .convert();
                setResourcePath(convertedResource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            CCFReader reader = new CCFReader(getResourcePath());
            int fieldNum = reader.numOfFields();
            CallableSet<ByteCode> rawNameOfFile = new CallableSet<>(fieldNum);
            for (int i = 0; i < fieldNum; i++) {
                rawNameOfFile.add(new ByteCode(reader.getField(i).getSimpleFieldName()));
            }
            getOutputFrame().initRawColNameArray(rawNameOfFile);
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void buildCoordinateTree() throws IOException {
        File resourcePath = getResourcePath();
        if (!resourcePath.withExtension("ccf")) {
            File convert = new Tsv2AnnotateResource(resourcePath)
                    .setResource(resourcePath.getName())
                    .setOutputDir(SDFAAnnotator.workflow.getParam("output"))
                    .isSVDatabase(true)
                    .convert();
            setResourcePath(convert);
        }
        int line = 0;
        CCFReader reader = new CCFReader(getResourcePath(), FIX_INTERVAL_FIELD);
        setContigBlockContainer(ContigBlockContainer.decode(reader.getMeta().get("contigBlock").values().get(0)));
        IRecord record = reader.getRecord();
        while (reader.read(record)) {
            update(record, line);
        }
        reader.close();
    }

    @Override
    public void clearCoordinate() {
        for (Chromosome chromosome : chromosomeResourceTreeMap.keySet()) {
            chromosomeResourceTreeMap.get(chromosome).getCoordinateSVTree().clear();
        }
    }

    @Override
    public void annotate(Chromosome chr, int resourceIndex) {
        RefSVResourceCoordinateTree SVResourceCoordinateTree = chromosomeResourceTreeMap.get(chr);
        Array<BriefSVAnnotationFeature> SVList = BriefSVAnnotationManager.getInstance().getBriefSVContainer().get(chr);
        if (SVResourceCoordinateTree == null || SVResourceCoordinateTree.isEmpty() || SVList == null || SVList.isEmpty()) {
            return;
        }
        for (BriefSVAnnotationFeature sv : SVList) {
            sv.setEncodeAnnotation(resourceIndex, FieldType.int32Array.encode(SVResourceCoordinateTree.getOverlap(sv)));
        }
    }
}
