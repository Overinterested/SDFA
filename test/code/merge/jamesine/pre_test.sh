# single sample
time jasmine threads=1 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data \
out_file=output/one_sample_merged_1threads.vcf > merge_1_sample_1_threads.log 2>&1
echo "**********************"
time jasmine threads=4 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data \
out_file=output/one_sample_merged_4threads.vcf > merge_1_sample_4_threads.log 2>&1
echo "**********************"

# double samples
time jasmine threads=1 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data_duplicate \
out_file=output/2_sample_merged_1threads.vcf > merge_2_sample_1_threads.log 2>&1
echo "**********************"
time jasmine threads=4 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data_duplicate \
out_file=output/2_sample_merged_4threads.vcf > merge_2_sample_4_threads.log 2>&1

# 4 times samples
time jasmine threads=1 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data_4times \
out_file=output/4_sample_merged_1threads.vcf > merge_4_sample_1_threads.log 2>&1
echo "**********************"
time jasmine threads=4 \
file_list=/Users/pwj/Desktop/SV/SVMerge/curated_data_4times \
out_file=output/4_sample_merged_4threads.vcf > merge_4_sample_4_threads.log 2>&1
