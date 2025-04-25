# SV level NAGF
java --jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sv-level

# sample level NAGF
java -jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level

# additional config with only outputting noncoding gene feature
java -jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level
--filter-noncoding

# additional config with only outputting coding gene feature
java -jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level
--filter-coding

# additional config with only outputting gene feature with large than coverage-cutoff 
java -jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level
--coverage-cutoff 8,-1,-1,-1,-1

# additional config with only outputting gene feature with large than coverage-cutoff 
java -jar ./SDFA.jar nagf \
-dir ./test/resource/46samples \
-f ./resource/hg38_refGene.ccf \
-o ./test/resource/nagf/output \
--sample-level
--af-cutoff 0.95