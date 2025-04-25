#!/bin/bash

# 获取当前目录下所有文件的绝对路径
folder="/Users/pwj/Desktop/data/dataset"
output="/Users/pwj/Desktop/SV/SVAnnotTest/AnnotSV/output/1404VCF"
cd $folder
simple_files=$(ls -p | grep -v /)

# 遍历所有文件 
for file in "${simple_files[@]}"
do 
   # 从文件路径中提取文件名
   filename="$folder/$file"
   /Users/pwj/Desktop/SV/SVAnnotTest/AnnotSV/AnnotSV/bin/AnnotSV \
         -SVinputFile "$filename" \
         -REselect2 0 \
         -REselect1 0 \
         -rankFiltering NA \
         -outputFile "$output/$file.annot"
done