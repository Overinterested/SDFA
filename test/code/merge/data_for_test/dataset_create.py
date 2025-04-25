import os
import random
import shutil

random.seed(1)
root = "/Users/pwj/Desktop/data/dataset"
sample_size = [100, 200, 400, 800, 1404]
move_dir = []
move_root = "/Users/pwj/Desktop/SV/SVMerge/data_for_test"
for size in sample_size:
    move_dir.append(move_root + f"/{size}samples")
tmp = os.walk(root)
for root, _, files in tmp:
    pass
for i in range(len(sample_size)):
    curr_sample_size = sample_size[i]
    random_samples = random.sample(files, curr_sample_size)
    move_dir = move_root+f"/{curr_sample_size}_samples"
    os.makedirs(move_dir, exist_ok=True)
    for file in random_samples:
        raw_file = root+f"/{file}"
        shutil.copy(raw_file, move_dir)
