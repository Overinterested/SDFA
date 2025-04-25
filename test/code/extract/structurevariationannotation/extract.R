library(StructuralVariantAnnotation)
root <- "/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf"
vcfType <-
  c(
    "cutesv2",
    "debreak",
    "delly",
    "nanosv",
    "nanovar",
    "pbsv",
    "sniffles2",
    "svim",
    "svision"
  )
error_file <- 0
for (type in vcfType) {
  print(paste("Start extracting ", type, sep = ""), sep = "\n")
  start <- Sys.time()
  type_dir <- paste(root, type, sep = "/")
  files <- list.files(type_dir)
  error_file <- 0
  for (file in files) {
    file <- paste(type_dir, file, sep = "/")
    tryCatch({
      vcf <- VariantAnnotation::readVcf(file)
      ranges <- breakpointRanges(vcf)
    }, error = function(e) {
      print(file)
      error_file <- error_file + 1
    })
  }
  end <- Sys.time()
  time_diff <- end-start
  cat(type," costs",time_diff[1],"  and missed", error_file, "files \n")
}

