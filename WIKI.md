# SDFA Project Wiki

## Overview
- [Introduction](#introduction)
- [Key Concepts](#key-concepts)
- [Architecture](#architecture)

## Getting Started
- [Installation](#installation)
    - [Prerequisites](#prerequisites)
    - [Installing SDFA](#installing-sdfa)
- [Building from Source](#building-from-source)

## Usage
- [Command-line Interface](#command-line-interface)
    - [VCF to SDF Conversion](#vcf-to-sdf-conversion)
    - [Merging](#merging)
    - [Annotation](#annotation)
    - [Numeric Gene Feature Annotation](#numeric-gene-feature-annotation)
- [API Reference](#api-reference)
    - [SDF Format](#sdf-format)
    - [Merge Module](#merge-module)
    - [Annotation Module](#annotation-module)
    - [NAGF Module](#ngf-module)

## Examples
- [Example 1: VCF to SDF Conversion](#example-1-vcf-to-sdf-conversion)
- [Example 2: Merging Population-scale SVs](#example-2-merging-population-scale-svs)
- [Example 3: Annotating SVs](#example-3-annotating-svs)
- [Example 4: Numeric Gene Feature Annotation](#example-4-numeric-gene-feature-annotation)

## How-to Guides
- [How to Contribute](#how-to-contribute)
- [How to Report Issues](#how-to-report-issues)

## Additional Resources
- [Performance Benchmarks](#performance-benchmarks)
- [FAQ](#faq)
- [Glossary](#glossary)

## Introduction
SDFA is aimed to provide a efficient, lightweight and flexible framework for population-scale SV analyses.<br>
First of all, it proposed a novel format(SDF, standard decomposed format) to compress and store the SV data 
considering to the complexity types of SV(like CSV called by SVision), various contig labels, indexable information and so on.
<br>
Then to analyse the SV data in SDF, SDFA designed over 10 types of results of VCF called by different callers(including CuteSV2, Sniffles2).

## Key Concepts
This section explains the key concepts and terminologies used in SDFA, such as Standardized Decomposition Format (SDF), Structural Variations (SVs), Numeric Annotation of Gene Feature (NAGF), and others.

## Architecture
This section describes the overall architecture of SDFA, its core components (SDF, Merge Module, Annotation Module, NAGF Module), and how they interact with each other.

## Installation
This section provides detailed instructions for installing SDFA on different platforms and environments.

### Prerequisites
Lists the prerequisite software and dependencies required to run SDFA.

### Installing SDFA
Step-by-step instructions for installing SDFA using different methods (e.g., pre-built binaries, package managers, etc.).

## Building from Source
Instructions for building SDFA from source code, including any specific build requirements or configurations.

## Command-line Interface
This section covers the command-line interface of SDFA and explains the available commands and options.

### VCF to SDF Conversion
Explains how to use the `vcf2sdf` command to convert VCF files to the SDF format.

### Merging
Describes how to use the `merge` command to merge individual-level SV files into a population-level file.

### Annotation
Covers the `annotate` command for annotating SV files with multiple annotation resources.

### Numeric Gene Feature Annotation
Explains the `ngf` command for annotating SVs with Numeric Annotation of Gene Feature (NAGF) information.

## API Reference
This section provides a comprehensive reference for the SDFA API, covering the different modules and their respective classes, methods, and parameters.

### SDF Format
Detailed documentation on the Standardized Decomposition Format (SDF) and its structure.

### Merge Module
Reference for the Merge Module, including its API and usage examples.

### Annotation Module
Reference for the Annotation Module, including its API and usage examples.

### NAGF Module
Reference for the Numeric Annotation of Gene Feature (NAGF) Module, including its API and usage examples.

## Examples
This section provides practical examples and use cases for different SDFA functionalities.

## How-to Guides
This section covers various how-to guides, such as how to contribute to the project, how to report issues, and other common tasks.

## Additional Resources
This section includes additional resources, such as performance benchmarks, frequently asked questions (FAQ), and a glossary of terms used in SDFA.

This Wiki structure aims to provide comprehensive documentation for the SDFA project, covering installation, usage, API reference, examples, and additional resources. The content outlined here is based on the information provided in the research paper, but it can be further expanded and detailed as the project progresses.