. /Users/pwj/Desktop/SV/annotation/sdfa/multi_resource_annotation/注释文件/resource/1100_annotation.sh &
pid=$!
psrecord $pid --log /Users/pwj/Desktop/SV/annotation/sdfa/multi_resource_annotation/注释文件/resource/annotation_monitor_log.txt --include-children --interval 2
wait $pid