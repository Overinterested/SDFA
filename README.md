# SDFA: An Efficient Framework for Structural Variation Analysis



<div align="center"> 
    <img src="assets/logo.png" alt="SDFA Logo" width="800" height="200"> 
</div>

SDFA (SDF-based Analyzer) is a novel computational framework designed for accurate and rapid parsing, storage, and analysis of structural variation (SV) data within large population samples. It introduces a novel Standardized Decomposition Format (SDF) for efficient SV representation and provides modules for merging, annotation and numeric annotation of gene feature(NAGF).

<p align="center">
    <a href="#key-features">
        <img src="./assets/xingxing.png" width=20 alt="*">Key Features
    </a>
    |
    <a href="#getting-start">
        <img src="./assets/xingxing.png" width=20 alt="*">Getting Start
    </a>
    |
    <a href="#usage">
        <img src="./assets/xingxing.png" width=20 alt="*">Usage
    </a>
    |
    <a href="#documentation">
        <img src="./assets/xingxing.png" width=20 alt="*">Documentation
    </a>
    |
    <a href="#license">
        <img src="./assets/xingxing.png" width=20 alt="*">License
    </a>
</p>

## Key Features

<img src="./assets/github_overview.png" alt="SDFA Framework"></img>

- **Standardized Decomposition Format(SDF)**: A novel format for standardizing, compressing, and storing various types of SVs arranged in genomic coordinates with indexes.
- **Sample-wide SV Merge**: A sample-wide merging procedure for analyzing population samples, ensuring a robust and efficient merging procedure.
- **Indexed sliding-window Annotation**: A light and fast annotation algorithm for annotating SVs with multiple resources, without repeatedly scanning the resource data.
- **Numeric Annotation of Gene Feature (NAGF)**: A numeric and efficient assessment of the impact of SVs on genes, addressing the complexity and lengthiness of current gene feature annotations.
- **SV-based GWAS Pipeline**: A SV-based GWAS analysis for large-scale population was constructed combined with Plink.

## Getting Start

### Prerequisites

- Java 8 or later
- Plink (using SV-based GWAS)

### Installation

1. Clone the repository:

```bash
git clone https://github.com/Overinterested/SDFA.git
```

2. Navigate to the project directory:

```bash
cd SDFA
# the latest .jar is SDFA.jar in current dir
java -jar SDFA.jar -h  
```

## Usage

The SDFA command-line tool supports various functionalities, including conversion from VCF to SDF, sample merge, SV annotation, and numeric annotation of gene feature(NAGF) for SVs. Here are some examples:

- <a href="#vcf2sdf">VCF2SDF Convertor</a>
- <a href="#gui">SDF GUI</a>
- <a href="#merge">Sample Merge</a>
- <a href="#annotation">Function Annotation</a>
- <a href="#nagf">NAGF for SV</a>
- <a href="#sv_gwas">SV-based GWAS</a>

<a name="vcf2sdf"></a>
**VCF to SDF Conversion**

Use SDFA to build an SDF archive for the sample file `input.vcf` and output to the folder `output_dir`. The instructions to complete the task are as follows:

```bash
# convert single vcf file to sdf file
java -jar ./SDFA.jar vcf2sdf \
-f ./test/resource/build/HG002_HiFi_aligned_GRCh38_winnowmap.sniffles.vcf \
-o ./test/resource/build/output
# convert compressed vcf file to sdf file: bgz, gz
java -jar ./SDFA.jar vcf2sdf \
-f ./test/resource/build/HG002_HiFi_aligned_GRCh38_winnowmap.sniffles.vcf.bgz \
-o ./test/resource/build/output

java -jar ./SDFA.jar vcf2sdf \
-f ./test/resource/build/HG002_HiFi_aligned_GRCh38_winnowmap.sniffles.vcf.gz \
-o ./test/resource/build/output

```

<a name="gui"></a>**SDF GUi**

Use SDFA to present the graphical interface for the sample SDF file

```shell
java -jar ./SDFA.jar gui \
-f ./test/resource/build/HG002_HiFi_aligned_GRCh38_winnowmap.sniffles.vcf.sdf
```

<a name="merge"></a>**Sample Merge**

Use SDFA to merge all files in the folder `input_dir`, including raw VCF files and other compressed files based on VCF like gz, bgz and sdf.

```bash
java -jar ./SDFA.jar merge \
-dir ./test/resource/merge \
-o ./test/resource/merge/output
```

<a name="annotation"></a>**SV Annotation**

Use SDFA to annotate all SVs in the folder `input_dir`, configured by annotation config file `annotation.config`.

```bash
java -jar SDFA.jar annotate -dir input_dir -o output_dir --annot-config annotation.config
```

<a name="ngf"></a>**NAGF For SV**

Use numeric annotation of gene feature to annotate the all files in the folder `input_dir`.

```bash
java -jar ./SDFA.jar nagf \
-dir ./test/resource/nagf \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level
```
<a name="sv_gwas"></a>**SV-based GWAS**

Here, we consider several split VCF files, which can be combined to form a complete population VCF file for a large-scale sample (similar to the organization of SV files in the UK Biobank). Therefore, we need to complete the following steps:

- <a name="#concat">SDF Concat</a>: Concat mutiple vcf files into one file in coordinate order.
- <a name="#extract">Samples Extraction</a>: Extract partial samples from the concat vcf file using `ped` file
- <a name="#sdf2plink">SDF2Plink</a>: Convert the sdf files to plink format files

- <a name="#plink">Plink For GWAS</a>: Use plink to conduct the GWAS for SV

<a name="concat"></a>**SDF Concat**

Concat multiple SDF files into one SDF file.

```bash
java -jar SDFA.jar concat -dir sdf_dir -o output_dir -threads 4
```

<a name="extract"></a>**Partial subjects**

Extract samples in ped file and convert to SDF file.

```bash
java -jar SDFA.jar extract -f input.sdf --extract-subject ped.file --ped-file -o output_dir
```

<a name="sdf2plink"></a>**SDF2Plink**

Convert SDF to Plink format, producing three files: `.fam`, `.bed` and `.bim`.

```bash
java -jar SDFA.jar sdf2plink -f sdf_file_path -o output_dir
```
<a name="plink"></a>**Plink for GWAS**

After conversion from SDF to Plink, the plink tool can be used to conduct the GWAS studies.

The detailed usage can be accessed and searched in homepage of plink, https://www.cog-genomics.org/plink/2.0/.

## Documentation

For more parameter information on available options and configurations, refer to the command-line help:

```bash
java -jar SDFA.jar [function] --help
```

**Note**: Detailed documentation, including installation instructions, usage examples, and API reference, can be found in the [project wiki](https://github.com/Overinterested/SDFA/wiki).

## License

This project is licensed under the MIT License.

