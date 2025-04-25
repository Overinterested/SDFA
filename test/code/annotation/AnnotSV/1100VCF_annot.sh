#!/bin/bash

# 获取当前目录下所有文件的绝对路径
folder="/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_all_vcf"
output="/Users/pwj/Desktop/SV/annotation/AnnotSV/output/1100_annotation_result"
simple_files=($(ls -p $folder/*.vcf)) 

# 遍历所有文件
for vcf in "${simple_files[@]}"
do 
   # 从文件路径中提取文件名
   base_file=$(basename $vcf)
   /Users/pwj/Desktop/SV/annotation/AnnotSV/AnnotSV/bin/AnnotSV \
         -SVinputFile $vcf \
         -REselect2 0 \
         -REselect1 0 \
         -rankFiltering NA \
         -outputFile "$output/$base_file.tsv" > "$output/$base_file.log"
   rm -rf /Users/pwj/Desktop/SV/SVAnnotTest/AnnotSV/output/1404VCF/*.header.tsv
done