sh /Users/pwj/Desktop/SV/annotation/AnnotSV/output/1100VCF_annot.sh &
pid=$!
psrecord $pid --log /Users/pwj/Desktop/SV/annotation/AnnotSV/output/annotation_monitor.txt --include-children --interval 10
wait $pid