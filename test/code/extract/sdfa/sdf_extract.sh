#!/bin/bash
folder="/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf"
output="/Users/pwj/Desktop/SV/extract/sdfa"

string_array=("cutesv2" "debreak" "delly" "NanoSV" "nanovar" "pbsv" "picky" "sniffles2" "svim" "svision")
# compress-mode-0
for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_0_1_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 0 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   else
   java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 0 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "0 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/1_threads_time.log
done

for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_0_4_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar --line-sort vcf2sdf  \
      -c 0 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   else
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 0 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "0 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/4_threads_time.log
done

# compress-mode-1
for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_1_1_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 1 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   else
      java -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf \
      -c 1 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "1 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/1_threads_time.log
done

for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_1_4_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar --line-sort vcf2sdf  \
      -c 1 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   else
   java -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf \
      -c 1 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "1 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/4_threads_time.log
done

# compress-mode-2
for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_2_1_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 2 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   else
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 2 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "2 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/1_threads_time.log
done

for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_2_4_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar --line-sort vcf2sdf  \
      -c 2 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   else
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 2 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "2 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/4_threads_time.log
done
# compress-mode-3
for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_3_1_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 3 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   else
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 3 \
      --threads 1 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "3 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/1_threads_time.log
done
for item in "${string_array[@]}"; do
   start=$(python -c 'import time; print(time.time())')
   data_dir="$folder/$item"
   output_dir="$output/mode_3_4_threads/$item"
   mkdir -p $output_dir
   if [ "$item" = "svim" ]; then
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar --line-sort vcf2sdf  \
      -c 3 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   else
      java  -Xms20g -Xmx20g -jar /Users/pwj/Desktop/SV/sdfa.jar vcf2sdf  \
      -c 3 \
      --threads 4 \
      -dir $data_dir \
      -o $output_dir
   fi
   end=$(python -c 'import time; print(time.time())')
   runtime=$(python -c "print($end - $start)")
   # 计算执行时间
   echo "3 mode : Parsing $item costs $runtime seconds" >> /Users/pwj/Desktop/SV/extract/sdfa/4_threads_time.log
done