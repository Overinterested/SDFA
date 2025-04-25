sh /Users/pwj/Desktop/SV/merge/truvira/data/multi/1_repeat_merged.sh &
pid=$!
psrecord $pid --log 1_repeat_merged.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/merge/truvira/data/multi/2_repeat_merged.sh &
pid=$!
psrecord $pid --log 2_repeat_merged.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/merge/truvira/data/multi/4_repeat_merged.sh &
pid=$!
psrecord $pid --log 4_repeat_merged.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/merge/truvira/data/multi/8_repeat_merged.sh &
pid=$!
psrecord $pid --log 8_repeat_merged.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/merge/truvira/data/multi/16_repeat_merged.sh &
pid=$!
psrecord $pid --log 16_repeat_merged.txt --include-children --interval 2
wait $pid
