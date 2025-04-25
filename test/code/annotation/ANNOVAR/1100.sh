find "/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_all_vcf" -type f -name "*.vcf" | while read -r vcf_file; do
    # 获取文件名和扩展名
    filename=$(basename "$vcf_file")
    /Users/pwj/Desktop/SV/annotation/ANNOVAR/annovar/convert2annovar.pl -format vcf4 "$vcf_file" >  "/Users/pwj/Desktop/SV/annotation/ANNOVAR/output/$filename.avinput"
    /Users/pwj/Desktop/SV/annotation/ANNOVAR/annovar/annotate_variation.pl -out "/Users/pwj/Desktop/SV/annotation/ANNOVAR/output/$filename.annot" \
    -build hg19 --geneanno "/Users/pwj/Desktop/SV/annotation/ANNOVAR/output/$filename.avinput" /Users/pwj/Desktop/SV/annotation/ANNOVAR/annovar/humandb/
done