one="/Users/pwj/Desktop/SV/SVMerge/data_for_test/100_samples"
two="/Users/pwj/Desktop/SV/SVMerge/data_for_test/200_samples"
four="/Users/pwj/Desktop/SV/SVMerge/data_for_test/400_samples"
eight="/Users/pwj/Desktop/SV/SVMerge/data_for_test/800_samples"
all_files="/Users/pwj/Desktop/SV/SVMerge/data_for_test/1260_samples"
output_dir="/Users/pwj/Desktop/SV/SVMerge/data_for_test"

find "$one" -type f -not -name '.*' -exec echo {} >> "$output_dir/100_samples_files" \;
find "$two" -type f -not -name '.*' -exec echo {} >> "$output_dir/200_samples_files" \;
find "$four" -type f -not -name '.*' -exec echo {} >> "$output_dir/400_samples_files" \;
find "$eight" -type f -not -name '.*' -exec echo {} >> "$output_dir/800_samples_files" \;
find "$all_files" -type f -not -name '.*' -exec echo {} >> "$output_dir/1260_samples_files" \;


cd /Users/pwj/Desktop/data/46samples/gz/92_samples
for file in ./*.gz; do
    tabix -p vcf "$file"
done

cd /Users/pwj/Desktop/data/46samples/gz/184_samples
for file in ./*.gz; do
    tabix -p vcf "$file"
done

cd /Users/pwj/Desktop/data/46samples/gz/368_samples
for file in ./*.gz; do
    tabix -p vcf "$file"
done

cd /Users/pwj/Desktop/data/46samples/gz/736_samples
for file in ./*.gz; do
    tabix -p vcf "$file"
done



find //Users/pwj/Desktop/data/46samples/gz/46_samples -type f -name "*.gz" -exec ls -d {} \; \
> /Users/pwj/Desktop/data/46samples/gz/46_curated_sniffles

find //Users/pwj/Desktop/data/46samples/gz/92_samples -type f -name "*.gz" -exec ls -d {} \; \
> /Users/pwj/Desktop/data/46samples/gz/92_curated_sniffles

find //Users/pwj/Desktop/data/46samples/gz/184_samples -type f -name "*.gz" -exec ls -d {} \; \
> /Users/pwj/Desktop/data/46samples/gz/184_curated_sniffles

find //Users/pwj/Desktop/data/46samples/gz/368_samples -type f -name "*.gz" -exec ls -d {} \; \
> /Users/pwj/Desktop/data/46samples/gz/368_curated_sniffles

find //Users/pwj/Desktop/data/46samples/gz/736_samples -type f -name "*.gz" -exec ls -d {} \; \
> /Users/pwj/Desktop/data/46samples/gz/736_curated_sniffles


/Users/pwj/Desktop/SV/SVAnnotation/vcfanno/vcfanno/docs/examples/gff_sv



find "/Users/pwj/Desktop/data/dataset" -type f -name "*.vcf" | while read -r vcf_file; do
    # 获取文件名和扩展名
    filename=$(basename "$vcf_file")
    /Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/annovar/convert2annovar.pl -format vcf4 \
    "$filename" >  "/Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/output/$filename.avinput"
    /Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/annovar/annotate_variation.pl -out "/Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/output/$filename.annot" \
    -build hg19 "/Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/output/$filename.avinput" /Users/pwj/Desktop/SV/SVAnnotation/ANNOVAR/annovar/humandb/
done











