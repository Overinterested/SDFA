package edu.sysu.pmglab.sdfa.annotation.genome;

import edu.sysu.pmglab.ccf.CCFFieldMeta;
import edu.sysu.pmglab.ccf.CCFReader;
import edu.sysu.pmglab.ccf.CCFTable;
import edu.sysu.pmglab.ccf.record.IRecord;
import edu.sysu.pmglab.ccf.type.FieldType;
import edu.sysu.pmglab.container.CallableSet;
import edu.sysu.pmglab.container.Entry;
import edu.sysu.pmglab.container.File;
import edu.sysu.pmglab.container.array.Array;
import edu.sysu.pmglab.container.array.BaseArray;
import edu.sysu.pmglab.container.intervaltree.IntervalObject;
import edu.sysu.pmglab.container.intervaltree.IntervalTree;
import edu.sysu.pmglab.easytools.container.ContigBlockContainer;
import edu.sysu.pmglab.executor.Pipeline;
import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.annotation.SDFAAnnotator;
import edu.sysu.pmglab.sdfa.annotation.collector.AbstractResourceManager;
import edu.sysu.pmglab.sdfa.annotation.collector.resource.GeneFeatureAnnotationType;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationFeature;
import edu.sysu.pmglab.sdfa.annotation.collector.sv.BriefSVAnnotationManager;
import edu.sysu.pmglab.sdfa.annotation.preprocess.AnnotationResourceMeta;
import edu.sysu.pmglab.sdfa.annotation.preprocess.GFF2AnnotateResource;
import edu.sysu.pmglab.sdfa.annotation.preprocess.GTF2AnnotateResource;
import edu.sysu.pmglab.sdfa.genome.RefGene;
import edu.sysu.pmglab.sdfa.genome.RefRNA;
import edu.sysu.pmglab.sdfa.genome.brief.RefGeneName;
import edu.sysu.pmglab.sdfa.genome.brief.RefRNAName;
import edu.sysu.pmglab.sdfa.genome.transcript.RNAElement;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Wenjie Peng
 * @create 2024-04-09 00:40
 * @description
 */
public class RefGeneManager extends AbstractResourceManager {
    CCFReader reader;
    IRecord templateRecord;
    boolean fullLoad = false;
    GeneFeatureAnnotationType annotationLevel;
    ContigBlockContainer contigBlockContainer;
    HashMap<Chromosome, Array<RefGene>> chromosomeRefGeneHashMap;
    HashMap<Chromosome, Array<RefGeneName>> briefChromosomeRefGeneHashMap;
    HashMap<Chromosome, RefGeneFeatureCoordinateTree> chromosomeResourceTreeMap;

    public RefGeneManager(File resourcePath, String resourceType) {
        super(resourcePath, resourceType);
    }

    public RefGeneManager(CCFReader reader, IRecord record) {
        super(reader.getFilePath().getFileObject(), "genome");
        this.reader = reader;
        this.templateRecord = record;
        this.contigBlockContainer = new ContigBlockContainer();
        this.contigBlockContainer = ContigBlockContainer.decode(reader.getMeta().get("contigBlock").values().get(0));
        setContigBlockContainer(contigBlockContainer);
        this.setResourceMeta(AnnotationResourceMeta.decode(reader.getMeta()));
    }

    public static RefGeneManager of(Object geneFeatureFile) throws IOException {
        CCFReader reader = new CCFReader(new File(geneFeatureFile.toString()));
        IRecord record = reader.getRecord();
        reader.close();
        RefGeneManager res = new RefGeneManager(reader, record);
        CCFTable.clear(res.reader);
        return res;
    }

    public RefGeneManager load() throws IOException {
        if (annotationLevel == null) {
            annotationLevel = GeneFeatureAnnotationType.HGVS_SNP_LEVEL;
        }
        switch (annotationLevel) {
            case HGVS_GENE_LEVEL:
                chromosomeResourceTreeMap = new HashMap<>();
                loadGeneCoordinate();
                break;
            case HGVS_RNA_LEVEL:
            case HGVS_SNP_LEVEL:
                chromosomeResourceTreeMap = new HashMap<>();
                loadRNACoordinate();
                break;
            case NAGF_GENE_LEVEL:
            case NAGF_RNA_LEVEL:
                chromosomeRefGeneHashMap = new HashMap<>();
                chromosomeResourceTreeMap.clear();
                loadFullCoordinate();
                fullLoad = true;
                break;
            case BRIEF_LOAD:
                loadBriefName();
                break;
        }
        return this;
    }

    public RefGene skipToNextGene() {
        return new RefGene();
    }

    public RNAElement skipToNextElement() {
        return RNAElement.of((byte) 1);
    }

    private void loadGeneCoordinate() throws IOException {
        reader = new CCFReader(
                reader.getFilePath(),
                Array.wrap(new CCFFieldMeta[]{
                        CCFFieldMeta.of("chrIndex", FieldType.varInt32),
                        CCFFieldMeta.of("indexOfChr", FieldType.varInt32),
                        CCFFieldMeta.of("numOfSubElement", FieldType.varInt32),
                        CCFFieldMeta.of("type", FieldType.varInt32),
                        CCFFieldMeta.of("name", FieldType.bytecode),
                        CCFFieldMeta.of("preposition", FieldType.int32Array)
                })
        );
        reader.seek(0);
        templateRecord = reader.getRecord();
        while (reader.read(templateRecord)) {
            Chromosome chromosome = contigBlockContainer.getChromosomeByIndex(templateRecord.get(0));
            checkIfNullForCoordinateTree(chromosome);
            RefGeneFeatureCoordinateTree geneFeatureCoordinateTree = chromosomeResourceTreeMap.get(chromosome);
            RefGene refGene = RefGene.loadGene(templateRecord);
            for (int i = 0; i < refGene.numOfSubRNA(); i++) {
                reader.read(templateRecord);
            }
            geneFeatureCoordinateTree.update(refGene);
        }
        CCFTable.clear(reader);
    }

    /**
     * load RNA coordinates for RNA annotation level
     *
     * @throws IOException reader read IO except
     */
    private void loadRNACoordinate() throws IOException {
        reader = new CCFReader(
                reader.getFilePath(),
                Array.wrap(new CCFFieldMeta[]{
                        CCFFieldMeta.of("chrIndex", FieldType.varInt32),
                        CCFFieldMeta.of("indexOfChr", FieldType.varInt32),
                        CCFFieldMeta.of("numOfSubElement", FieldType.varInt32),
                        CCFFieldMeta.of("type", FieldType.varInt32),
                        CCFFieldMeta.of("name", FieldType.bytecode),
                        CCFFieldMeta.of("preposition", FieldType.int32Array),
                        CCFFieldMeta.of("exonArray", FieldType.int32Array)
                })
        );
        reader.seek(0);
        while (reader.read(templateRecord)) {
            RefGene refGene = RefGene.loadGene(templateRecord);
            Chromosome chromosome = contigBlockContainer.getChromosomeByIndex(templateRecord.get(0));
            checkIfNullForCoordinateTree(chromosome);
            RefGeneFeatureCoordinateTree geneFeatureCoordinateTree = chromosomeResourceTreeMap.get(chromosome);
            for (int i = 0; i < refGene.numOfSubRNA(); i++) {
                reader.read(templateRecord);
                RefRNA refRNA = RefRNA.loadRNA(templateRecord);
                refGene.putRNA(refRNA);
                geneFeatureCoordinateTree.update(refGene, refRNA);
            }
        }
        CCFTable.clear(reader);
    }

    private void checkIfNullForCoordinateTree(Chromosome chromosome) {
        RefGeneFeatureCoordinateTree geneFeatureCoordinateTree = chromosomeResourceTreeMap.get(chromosome);
        if (geneFeatureCoordinateTree == null) {
            geneFeatureCoordinateTree = new RefGeneFeatureCoordinateTree(annotationLevel);
            chromosomeResourceTreeMap.put(chromosome, geneFeatureCoordinateTree);
        }
    }

    private void putRefGeneIfNull(Chromosome chromosome, RefGene refGene) {
        Array<RefGene> refGenes = chromosomeRefGeneHashMap.get(chromosome);
        if (refGenes == null) {
            Array<RefGene> geneArray = new Array<>();
            geneArray.add(refGene);
            chromosomeRefGeneHashMap.put(chromosome, geneArray);
        } else {
            refGenes.add(refGene);
        }
    }

    private void loadBriefName() throws IOException {
        if (briefChromosomeRefGeneHashMap == null) {
            briefChromosomeRefGeneHashMap = new HashMap<>();
        }
        reader = new CCFReader(reader.getFilePath());
        templateRecord = reader.getRecord();
        reader.seek(0);
        while (reader.read(templateRecord)) {
            RefGeneName refGeneName = RefGeneName.decode(templateRecord);
            Chromosome chromosome = contigBlockContainer.getChromosomeByIndex(templateRecord.get(0));
            Array<RefGeneName> refGeneNames = briefChromosomeRefGeneHashMap.get(chromosome);
            if (refGeneNames == null) {
                refGeneNames = new Array<>();
                briefChromosomeRefGeneHashMap.put(chromosome, refGeneNames);
            }
            refGeneNames.add(refGeneName);
            for (int i = 0; i < refGeneName.numOfRNA(); i++) {
                reader.read(templateRecord);
                RefRNAName refRNAName = RefRNAName.decode(templateRecord);
                refGeneName.updateRNAName(refRNAName);
            }
        }
        CCFTable.clear(reader);
    }

    private void loadFullCoordinate() throws IOException {
        reader = new CCFReader(reader.getFilePath());
        templateRecord = reader.getRecord();
        reader.seek(0);
        while (reader.read(templateRecord)) {
            RefGene refGene = RefGene.loadGene(templateRecord);
            Chromosome chromosome = contigBlockContainer.getChromosomeByIndex(templateRecord.get(0));
            putRefGeneIfNull(chromosome, refGene);
            for (int i = 0; i < refGene.numOfSubRNA(); i++) {
                reader.read(templateRecord);
                RefRNA refRNA = RefRNA.loadRNA(templateRecord);
                refGene.putRNA(refRNA);
            }
        }
        CCFTable.clear(reader);
    }

    @Override
    public void buildCoordinateTree() throws IOException {
        reader = new CCFReader(getResourcePath());
        if (!reader.isClosed()) {
            reader.close();
        }
        this.templateRecord = reader.getRecord();
        this.contigBlockContainer = ContigBlockContainer.decode(reader.getMeta().get("contigBlock").values().get(0));
        setContigBlockContainer(contigBlockContainer);
        this.setResourceMeta(AnnotationResourceMeta.decode(reader.getMeta()));
        if (annotationLevel == null) {
            annotationLevel = GeneFeatureAnnotationType.HGVS_SNP_LEVEL;
        }
        load();
    }

    @Override
    public void clearCoordinate() {

    }

    @Override
    public void checkResourceAndOutputFrame(File outputDir) {
        try {
            File resourcePath = getResourcePath();
            if (!resourcePath.withExtension("ccf")) {
                if (resourcePath.withExtension("gff.gz", "gff", "gff.bgz")) {
                    File convert = new GFF2AnnotateResource(resourcePath, SDFAAnnotator.workflow.getParam("output"))
                            .convert();
                    setResourcePath(convert);
                } else if (resourcePath.withExtension("gtf", "gtf.gz", "gtf.bgz")) {
                    File convert = new GTF2AnnotateResource(resourcePath, SDFAAnnotator.workflow.getParam("output"))
                            .convert();
                    setResourcePath(convert);
                } else {
                    throw new UnsupportedOperationException("Reference genome file can't be parsed.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void annotate(Chromosome chr, int indexOfResource) {
        Array<BriefSVAnnotationFeature> briefSVAnnotationFeatureArray = BriefSVAnnotationManager.getInstance().getBriefSVContainer().get(chr);
        if (briefSVAnnotationFeatureArray == null || briefSVAnnotationFeatureArray.isEmpty()) {
            return;
        }
        switch (annotationLevel) {
            case HGVS_GENE_LEVEL:
            case HGVS_RNA_LEVEL:
                RefGeneFeatureCoordinateTree geneFeatureCoordinateTree = chromosomeResourceTreeMap.get(chr);
                if (geneFeatureCoordinateTree == null || geneFeatureCoordinateTree.isEmpty()) {
                    return;
                }
                for (BriefSVAnnotationFeature briefSVAnnotationFeature : briefSVAnnotationFeatureArray) {
                    briefSVAnnotationFeature.setEncodeAnnotation(
                            indexOfResource,
                            FieldType.int32Array.encode(geneFeatureCoordinateTree.getOverlap(briefSVAnnotationFeature))
                    );
                }
                break;
            case HGVS_SNP_LEVEL:
                RefGeneFeatureCoordinateTree geneRNAFeatureCoordinateTree = chromosomeResourceTreeMap.get(chr);
                if (geneRNAFeatureCoordinateTree == null || geneRNAFeatureCoordinateTree.isEmpty()) {
                    return;
                }
                IntervalTree<Entry<RefGene, RefRNA>, Integer> rnaCoordinateTree = geneRNAFeatureCoordinateTree.rnaCoordinateTree;
                for (BriefSVAnnotationFeature sv : briefSVAnnotationFeatureArray) {
                    BaseArray<IntervalObject<Entry<RefGene, RefRNA>, Integer>> overlapsIntervals = rnaCoordinateTree.getOverlapsIntervals(sv.getStart(), sv.getEnd());
                    GeneAnnotFeature geneAnnotFeature = new GeneAnnotFeature();
                    if (overlapsIntervals != null && !overlapsIntervals.isEmpty()) {
                        for (IntervalObject<Entry<RefGene, RefRNA>, Integer> overlapsInterval : overlapsIntervals) {
                            Entry<RefGene, RefRNA> data = overlapsInterval.getData();
                            RefRNA rna = data.getValue();
                            short geneIndex = data.getKey().getGeneIndex();
                            RNAAnnot posRNAAnnot = rna.singlePosLocation0(sv.getStart(), RefRNA.upstreamDis, RefRNA.downstreamDis, geneIndex);
                            RNAAnnot endRNAAnnot = rna.singlePosLocation0(sv.getEnd(), RefRNA.upstreamDis, RefRNA.downstreamDis, geneIndex);
                            posRNAAnnot.append(endRNAAnnot);
                            geneAnnotFeature.add(posRNAAnnot);
                        }
                        sv.setEncodeAnnotation(indexOfResource, geneAnnotFeature.encode());
                        geneAnnotFeature.reset();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void annotateAll(int indexOfResource) {
        CallableSet<Chromosome> allChromosomes = contigBlockContainer.getAllChromosomes();
        for (Chromosome chromosome : allChromosomes) {
            annotate(chromosome, indexOfResource);
        }
    }

    public Array<Pipeline> annotateAllTasks(int indexOfResource) {
        Array<Pipeline> pipelines = new Array<>();
        CallableSet<Chromosome> allChromosomes = contigBlockContainer.getAllChromosomes();
        for (Chromosome chromosome : allChromosomes) {
            pipelines.add(
                    new Pipeline(((status, context) -> annotate(chromosome, indexOfResource)))
            );
        }
        return pipelines;
    }

    public RefGeneManager setAnnotationLevel(GeneFeatureAnnotationType annotationLevel) {
        this.annotationLevel = annotationLevel;
        return this;
    }

    public Array<RefGene> getFullGeneOfChr(Chromosome chromosome) {
        if (fullLoad) {
            return chromosomeRefGeneHashMap.get(chromosome);
        }
        throw new UnsupportedOperationException("The reference parse type is not full load.");
    }


    public HashMap<Chromosome, Array<RefGeneName>> getBriefChromosomeRefGeneHashMap() {
        return briefChromosomeRefGeneHashMap;
    }


    public void outputConfig() throws IOException {
        if (briefChromosomeRefGeneHashMap != null && !briefChromosomeRefGeneHashMap.isEmpty()) {
            if (chromosomeResourceTreeMap != null && !chromosomeResourceTreeMap.isEmpty()) {
                chromosomeResourceTreeMap.clear();
            }
            if (chromosomeRefGeneHashMap != null && !chromosomeRefGeneHashMap.isEmpty()) {
                chromosomeRefGeneHashMap.clear();
            }
        } else {
            annotationLevel = GeneFeatureAnnotationType.BRIEF_LOAD;
            load();
            if (chromosomeResourceTreeMap != null && !chromosomeResourceTreeMap.isEmpty()) {
                chromosomeResourceTreeMap.clear();
            }
            if (chromosomeRefGeneHashMap != null && !chromosomeRefGeneHashMap.isEmpty()) {
                chromosomeRefGeneHashMap.clear();
            }
        }
    }

    public HashMap<Chromosome, Array<RefGene>> getChromosomeRefGeneHashMap() {
        return chromosomeRefGeneHashMap;
    }
}
