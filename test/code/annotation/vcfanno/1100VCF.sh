find "/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_all_vcf" -type f -name "*.vcf" | while read -r vcf_file; do
    # 获取文件名和扩展名
    filename=$(basename "$vcf_file")
    /Users/pwj/Desktop/SV/annotation/vcfanno/vcfanno/docs/examples/gff_sv/vcfanno --ends -lua gff_gene_name.lua gff_sv.conf $vcf_file \
    > "/Users/pwj/Desktop/SV/annotation/vcfanno/vcfanno/docs/examples/gff_sv/output/$filename.annot"
done