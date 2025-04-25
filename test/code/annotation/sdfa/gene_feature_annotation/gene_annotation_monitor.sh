. /Users/pwj/Desktop/SV/annotation/sdfa/gene_feature_annotation/1_thread_gene_feature_annotation.sh &
pid=$!
psrecord $pid --log 1100_annot_annovcf.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/annotation/sdfa/gene_feature_annotation/1_thread/output/*.sdf
rm /Users/pwj/Desktop/SV/annotation/sdfa/gene_feature_annotation/1_thread/output/*.sdfa