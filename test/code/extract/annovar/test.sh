#!/bin/bash

# 获取当前目录下所有文件的绝对路径
date
folder="/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf"
output="/Users/pwj/Desktop/SV/extract/annovar/output"
# 字符串数组
string_array=("cutesv2" "debreak" "delly" "NanoSV" "nanovar" "pbsv" "picky" "sniffles2" "svim" "svision")
# 循环拼接路径
for item in "${string_array[@]}"; do
   dir="$folder/$item"
   simple_files=($(ls -p $dir/*.vcf)) 
   start_time=$(gdate +%s.%N)
   # 遍历所有文件 
   mkdir "$output/$item"
   for vcf in "${simple_files[@]}"
   do 
      # 从文件路径中提取文件名
      base_file=$(basename $vcf)
      /Users/pwj/Desktop/SV/extract/annovar/convert2annovar.pl \
      -format vcf4 $vcf > "$output/$item/$base_file.avinput"
   done
   end_time=$(gdate +%s.%N)
   # 计算时间差
   elapsed_time_ns=$(echo "($end_time - $start_time) * 1000000000" | bc | cut -d '.' -f 1)
   elapsed_time_sec=$(echo "scale=9; $elapsed_time_ns / 1000000000" | bc)
   echo $item >> /Users/pwj/Desktop/SV/SVExtract/annovar/extract.log
   echo $elapsed_time_sec >> /Users/pwj/Desktop/SV/extract/annovar/extract.log
done
