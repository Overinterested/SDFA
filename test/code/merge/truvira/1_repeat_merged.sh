cd /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv
for file in ./*.gz;do
	tabix -f -p vcf "$file"
done
# merge by bcftool
bcftools merge -m none /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/*.gz --force-samples | bgzip > /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/cutesv_merged.vcf.gz
tabix -p vcf /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/cutesv_merged.vcf.gz
# merge by truvari
truvari collapse -i /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/cutesv_merged.vcf.gz \
-o /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/1_repeat_cutesv_truvari_merged.vcf \
-c /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat/cutesv/1_repeat_cutesv_truvari_collapsed.vcf \
-r 1000