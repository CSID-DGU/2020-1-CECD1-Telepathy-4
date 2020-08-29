#!/usr/bin/env python
# coding: utf-8

# In[72]:


import csv
import pandas as pd
from datetime import date

csv_file = pd.read_csv('total.csv')
# csv 파일 읽기

i = 0
j = 0
arr = {}

for d in csv_file['date_num']:
    if len(d) == 10: # yyyy-mm-dd 형식의 날짜만 있는 한 사람의 하루 걸음수 데이터면
        list_temp = d.split('-')
        list_temp.insert(3, csv_file['person_id'][j])
        list_temp.insert(4, csv_file['steps'][j])
        arr[i] = list_temp
        i = i+1
        j = j+1
    else: # yyyymmdd~yyyymmdd 형식의 한 사람의 기간이면
        j = j+1


# In[82]:


csv_out_file = open('average_4weeks.csv', 'w', encoding='utf-8', newline='')
wr = csv.writer(csv_out_file)

wr.writerow(['person_id', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'])

count = 0
oneweek_list = [0, 0, 0, 0, 0, 0, 0]  # [0]~[6] : MON ~ SUN

for x in range(0, len(arr)-1):
    year = int(arr[x][0]) # 년
    month = int(arr[x][1]) # 월
    day = int(arr[x][2]) # 일
    user_id = arr[x][3] # 식별번호
    wday = date(year, month, day).weekday()  # 요일을 int값으로 반환
        
    if arr[x][3] == arr[x+1][3]:  # 같은 사람일 때
        if count < 28: # 연속된 날 수가 28일이 아직 안 되면
            count = count + 1  # 28일까지 카운트
            oneweek_list[wday] = oneweek_list[wday] + int(arr[x][4])  # 각 요일의 걸음수 누적
            print(user_id, " | ", year, " | ", month, " | ", day, " | ", wday, " | ", "count: ", count)

            if count == 28:  # 28일치를 했으면
                average_list = [0, 0, 0, 0, 0, 0, 0] # 4주치 걸음수 데이터의 평균을 담을 배열을 선언하고
                for y in range(0, 7):
                    average_list[y] = oneweek_list[y] / 4 # 4주치 걸음수 데이터를 각 요일별로 평균을 저장
                wr.writerow([arr[x][3], average_list[0], average_list[1], average_list[2], average_list[3], average_list[4], average_list[5], average_list[6]])
                # csv 파일에 씀
                
        else: # 같은 사람이긴 한데 한 달치 데이터를 다 뽑았으면
            continue # 아무 동작도 안 하고 그냥 넘어감
            
    else: # 다른 사람이면
        print("------------------------------- else -------------------------------")
        year = int(arr[x+1][0]) # 년
        month = int(arr[x+1][1]) # 월
        day = int(arr[x+1][2]) # 일
        user_id = arr[x+1][3] # 식별번호
        wday = date(year, month, day).weekday()  # 요일을 int값으로 반환
        
        count = 0  # 초기화
        
        print(user_id, " | ", year, " | ", month, " | ", day, " | ", wday, " | ", "count: ", count)
        print("--------------------------------------------------------------------")
        
        for n in range(0, 7): 
            oneweek_list[n] = 0  # 요일 누적 걸음수 배열 초기화
            
csv_out_file.close()

print("success")


# In[ ]:




