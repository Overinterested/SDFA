sh /Users/pwj/Desktop/SV/merge/sdfa/multi/1_repeat.sh &
pid=$!
psrecord $pid --log 1_repeat_4_threads_merged.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/merge/sdfa/multi/1_repeat/*.sdf

sh /Users/pwj/Desktop/SV/merge/sdfa/multi/2_repeat.sh &
pid=$!
psrecord $pid --log 2_repeat_4_threads_merged.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/merge/sdfa/multi/2_repeat/*.sdf

sh /Users/pwj/Desktop/SV/merge/sdfa/multi/4_repeat.sh &
pid=$!
psrecord $pid --log 4_repeat_4_threads_merged.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/merge/sdfa/multi/4_repeat/*.sdf

sh /Users/pwj/Desktop/SV/merge/sdfa/multi/8_repeat.sh &
pid=$!
psrecord $pid --log 8_repeat_4_threads_merged.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/merge/sdfa/multi/8_repeat/*.sdf

sh /Users/pwj/Desktop/SV/merge/sdfa/multi/16_repeat.sh &
pid=$!
psrecord $pid --log 16_repeat_4_threads_merged.txt --include-children --interval 2
wait $pid
rm /Users/pwj/Desktop/SV/merge/sdfa/multi/16_repeat/*.sdf