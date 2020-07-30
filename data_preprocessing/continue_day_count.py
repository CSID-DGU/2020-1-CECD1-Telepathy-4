#!/usr/bin/env python
# coding: utf-8

# In[98]:


import csv
import pandas as pd

csv_file = pd.read_csv('output_efil_day_sample.csv')

# csv_file
# 여기까지 불러오는 거


# In[99]:


i = 2
j = 0
arr = {}

for d in csv_file['date']:
    list_temp = d.split('-')
    list_temp.insert(3, csv_file['person_id'][j])
    list_temp.insert(4, csv_file['steps'][j])
    arr[i] = list_temp
    i = i + 1
    j = j + 1
    
# 여기까지 배열에 날짜만 뽑아서 저장하는 거


# In[100]:


count = 0 # 며칠 연속인지
n = 0 # 사람 수

for i in range(2, len(arr) - 1):
    month = int(arr[i][1]) # 달
    nextmonth = int(arr[i + 1][1]) # 다음달

    day = int(arr[i][2]) # 일
    nextday = int(arr[i + 1][2]) # 다음날
    
    user = arr[i][3] # 사용자 id
    nextuser = arr[i + 1][3] # 다음 사용자 id

    #if user == nextuser:
    if day + 1 != nextday: # 하루 단위로 안 이어지면
        if month == 1 or month == 3 or month == 5 or month == 7 or month == 8 or month == 10 or month == 12: # 31일
            if (day == 31) and (nextday == 1) and (month + 1 == nextmonth): # 말일이고 다음 달이랑 이어지는 데이터면
                count = count + 1
            elif (day == 31) and (nextday == 1) and month == 12 and nextmonth == 1: # 12월이고 1월이랑 이어지는 말일 데이터면
                count = count + 1
            else: # 말일이긴 한데 다음 달이랑 안 이어지면
                if count >= 28:
                    n = n + 1
                    #print(i, count)
                count = 0
        elif month == 4 or month == 6 or month == 9 or month == 11: # 30일
            if (day == 30) and (nextday == 1) and (month + 1 == nextmonth): # 말일이고 다음 달이랑 이어지는 데이터면
                count = count + 1
            else:
                if count >= 28:
                    n = n + 1
                    #print(i, count)
                count = 0
        else: # 2월일 때
            if (day == 28) and (nextday == 1) and (month + 1 == nextmonth): # 말일이고 다음 달이랑 이어지는 데이터면
                count = count + 1
            else:
                if count >= 28:
                    n = n + 1
                    #print(i, count)
                count = 0
    else: # 하루 단위로 이어지면
        count = count + 1
        
print(n)

