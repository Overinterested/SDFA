sh /Users/pwj/Desktop/SV/SVMerge/survivor_done/population_test/736_samples.sh &
pid=$!
psrecord $pid --log 736_samples_survivor.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/736_samples_1_threads.sh &
pid=$!
psrecord $pid --log 736_samples_jasmine_1_thread.txt --include-children --interval 2
wait $pid

sh /Users/pwj/Desktop/SV/SVMerge/jamesine_done/population/736_samples_4_threads.sh &
pid=$!
psrecord $pid --log 736_samples_jasmine_4_thread.txt --include-children --interval 2
wait $pid