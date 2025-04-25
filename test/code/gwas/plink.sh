# filter
./test/resource/gwas/plink2 --bfile ./test/resource/gwas/output/ \
--geno 0.2 \
--mind 0.8 \
--hwe 1e-6 \
--maf 0.05 \
--make-bed \
--out ./test/resource/gwas/output/geno_0.2_mind_0.8_maf_0.05
# assocation
./test/resource/gwas/plink2 --bfile ./test/resource/gwas/output/geno_0.2_mind_0.8_maf_0.05 \
-adjust \
--glm \
allow-no-covars \
--out ./test/resource/gwas/output/geno_0.2_maf_0.05_res
