# 统计文件夹下所有文件中不以#开头的总行数
string_array=("cutesv2" "debreak" "delly" "NanoSV" "nanovar" "pbsv" "picky" "sniffles2" "svim" "svision")

for item in "${string_array[@]}"; do
    folder_path="/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf/$item"
    start=$(python -c 'import time; print(time.time())')
    cp -r $folder_path "/Users/pwj/Desktop/SV/extract/structurevariationannotation/$item"
    end=$(python -c 'import time; print(time.time())')
    runtime=$(python -c "print($end - $start)")
    echo "$item costs $runtime seconds"
done