import os
import time
import vcfpy
import warnings
warnings.filterwarnings("ignore")
folder_path = "/Users/pwj/Desktop/SV/data/pmglab/lz_hg38_catagory_vcf/"
output_path = "/Users/pwj/Desktop/SV/extract/vcfpy/output/"
vcfType = ["cutesv2", "debreak", "delly", "NanoSV", "nanovar", "pbsv",
           "picky", "sniffles2", "svim", "svision"]
for type_item in vcfType:
    count = 0
    dir = folder_path + type_item
    start = time.time()
    for root, dirs, files in os.walk(dir):
        for file in files:
            file_path = os.path.join(root, file)
            if not file_path.endswith("vcf"):
                pass
            # Open file, this will read in the header
            try:
                reader = vcfpy.Reader.from_path(file_path)
            except Exception:
                print(file_path)
                pass
            output_file = f"{output_path}{type_item}/{file}"
            # 递归创建父目录
            os.makedirs(os.path.dirname(output_file), exist_ok=True)
            # 打开文件并写入内容
            with open(output_file, 'w') as file:
                pass
            writer = vcfpy.Writer.from_path(output_file, reader.header)
            try:
                for record in reader:
                    writer.write_record(record)
                    pass
            except Exception:
                count = count+1
                pass
    end = time.time()
    print(type_item, f" cost {end - start} seconds and miss ", count)
