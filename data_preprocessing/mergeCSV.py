#!/usr/bin/env python
# coding: utf-8

# In[9]:


import csv 
import glob 

path = 'C:/Users/saepi/Documents/University/grade4/semester1/CapstonDesign1/example/output_continue/' #CSV 파일이 존재하는 경로 
merge_path = 'C:/Users/saepi/Documents/University/grade4/semester1/CapstonDesign1/example/total.csv' #최종 Merge file 
file_list = glob.glob(path + '*') #1. merge 대상 파일을 확인 

with open(merge_path, 'w') as f: #2-1.merge할 파일을 열고 
    for file in file_list: 
        with open(file ,'r') as f2: 
            while True: 
                line = f2.readline() #2.merge 대상 파일의 row 1줄을 읽어서 
                
                if not line: #row가 없으면 해당 csv 파일 읽기 끝 
                    break 
                    
                f.write(line) #3.읽은 row 1줄을 merge할 파일에 쓴다. 
                
        file_name = file.split('\\')[-1] 
        print(file.split('\\')[-1] + ' write complete...') 
        
print('>>> All file merge complete...')


# In[ ]:




