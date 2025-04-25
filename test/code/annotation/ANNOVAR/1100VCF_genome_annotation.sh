sh /Users/pwj/Desktop/SV/annotation/ANNOVAR/1100.sh &
pid=$!
psrecord $pid --log //Users/pwj/Desktop/SV/annotation/ANNOVAR/1100_annot_monitor.txt --include-children --interval 2
wait $pid