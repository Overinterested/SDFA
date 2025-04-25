. /Users/pwj/Desktop/SV/annotation/vcfanno/vcfanno/docs/examples/gff_sv/script/1100VCF.sh &
pid=$!
psrecord $pid --log 1100_annot_annovcf.txt --include-children --interval 2
wait $pid