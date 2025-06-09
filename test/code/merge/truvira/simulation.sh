cd /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/vcf_gz
#for file in ./*.bgz;do
#	tabix -f -p vcf "$file"
#done
# merge by bcftool
bcftools merge -m none /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/vcf_gz/*.bgz --force-samples | bgzip > /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_merged.vcf.bgz
tabix -p vcf /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_merged.vcf.bgz
# merge by truvari
truvari collapse -i /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_merged.vcf.bgz \
-o /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_truvari_merged.vcf \
-c /Users/wenjiepeng/Desktop/SDFA_4.0/simulation/truvari/cutesv_truvari_collapsed.vcf \
-r 1000