#!/bin/bash

folder_path="/Users/pwj/Desktop/SV/SVAnnotation/AnnotSV/output/1404VCF"

find "$folder_path" -type f -name "*.log" -exec sh -c '
    for file do
        last_line=$(tail -n 1 "$file")
        if [[ "$last_line" == *"Exit"* ]]; then
            echo "$file" >> /Users/pwj/Desktop/SV/SVAnnotation/AnnotSV/output/error_file.txt
        fi
    done
' sh {} +
