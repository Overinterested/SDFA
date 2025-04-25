# 统计文件夹下所有文件中不以#开头的总行数
string_array=("cutesv2" "debreak" "delly" "NanoSV" "nanovar" "pbsv" "picky" "sniffles2" "svim" "svision")
target_path=/Users/pwj/Desktop/SV/extract/truvari/output
for item in "${string_array[@]}"; do
    folder_path="/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf/$item"
    mkdir "$target_path/$item"
    cd $folder_path
    start=$(python -c 'import time; print(time.time())')
    error_count=0
    for file in *; do
    	file_name=$(basename "$file")
    	trap 'echo "Error occurred for $file"; error_count=$((error_count+1))' ERR
    	truvari vcf2df -i -f "$file" "$target_path/$item/$file_name" 2>&1 >> /Users/pwj/Desktop/SV/extract/truvari/extract.log
    done
    end=$(python -c 'import time; print(time.time())')
    runtime=$(python -c "print($end - $start)")
	echo "Parsing $item costs $runtime seconds with $error_count errors" 2>&1 >> /Users/pwj/Desktop/SV/extract/truvari/extract.log
done