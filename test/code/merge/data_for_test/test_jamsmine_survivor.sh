sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/46_samples.sh &
pid=$!
psrecord $pid --log 46_samples_survivor.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/92_samples.sh &
pid=$!
psrecord $pid --log 92_samples_survivor.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/184_samples.sh &
pid=$!
psrecord $pid --log 184_samples_survivor.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/368_samples.sh &
pid=$!
psrecord $pid --log 368_samples_survivor.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/736_samples.sh &
pid=$!
psrecord $pid --log 736_samples_survivor.txt --include-children --interval 2
wait $pid
# jasmine 
sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/46_samples_1_threads.sh &
pid=$!
psrecord $pid --log 46_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/46_samples_4_threads.sh &
pid=$!
psrecord $pid --log 46_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/92_samples_1_threads.sh &
pid=$!
psrecord $pid --log 92_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/92_samples_4_threads.sh &
pid=$!
psrecord $pid --log 92_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/184_samples_1_threads.sh &
pid=$!
psrecord $pid --log 184_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/184_samples_4_threads.sh &
pid=$!
psrecord $pid --log 184_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/368_samples_1_threads.sh &
pid=$!
psrecord $pid --log 368_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/368_samples_4_threads.sh &
pid=$!
psrecord $pid --log 368_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid


sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/736_samples_1_threads.sh &
pid=$!
psrecord $pid --log 736_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/736_samples_4_threads.sh &
pid=$!
psrecord $pid --log 736_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid
