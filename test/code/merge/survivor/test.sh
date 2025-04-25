time /Users/pwj/Desktop/SV/SVMerge/survivor/SURVIVOR/Debug/SURVIVOR \
merge /Users/pwj/Desktop/SV/SVMerge/curated_data 1000 1 1 0 0 30 one_sample_vcf.vcf 2>&1 >> one_sample.log

echo "**********************"

time /Users/pwj/Desktop/SV/SVMerge/survivor/SURVIVOR/Debug/SURVIVOR \
merge /Users/pwj/Desktop/SV/SVMerge/curated_data_duplicate 1000 1 1 0 0 30 two_sample_vcf.vcf 2>&1 >> two_samples.log

echo "**********************"

time /Users/pwj/Desktop/SV/SVMerge/survivor/SURVIVOR/Debug/SURVIVOR \
merge /Users/pwj/Desktop/SV/SVMerge/curated_data_4times 1000 1 1 0 0 30 four_sample_vcf.vcf 2>&1 >> four_samples.log
