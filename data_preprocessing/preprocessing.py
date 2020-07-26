import json
import csv

class Person:  # 한 사람에 대한 정보 
    def __init__(self, id, step, start, end, vender):
        self.id = id  # id = recordkey
        self.step = step  # 걸음 수
        self.start = start  # 시작 시간
        self.end = end  # 끝나는 시작
        self.vender = vender  # 디바이스
    def __repr__(self):
        return repr((self.id, self.step, self.start, self.end, self.vender))

class DayPerson:
    def __init__(self, id, steps, date, device):
        self.id = id
        self.steps = steps
        self.date = date
        self.device = device
    def __repr__(self):
        return repr((self.id, self.steps, self.date, self.device))

with open("activity.json")as json_file:  # with를 이용해 json 파일 열기, 타겟 json 파일의 경로 지정
    json_object = json.load(json_file)  # json 파일 로드
    json_array = json_object.get("info")  # info 객체 가져오기

    csvfile = open('output_samsung_minutes.csv', 'w', encoding='utf=8', newline='')  # csv 파일 '쓰기'로 열기
    wr = csv.writer(csvfile)

    person_list = []  # 빈 리스트
    scount = 1
    for list in json_array:
        stype = list.get("source").get("type")
        if stype == 1101:  #product 존재하는 경우만 (efilcare 제외)
            #not_efilcare = list.get("source").get("product")
            isSamsung = list.get("source").get("product").get("name")
            if isSamsung == "S-Health":  #Samsung(S-Health)이면
            #if isSamsung == "healthkit":  #apple(healthkit)이면
            #if isSamsung == "efil tracker S1":  #lifesemantics(efil tracker S1)이면
                scount += 1
                person_list.append(Person(list.get("recordkey"),list.get("steps"),list.get("period").get("from").get("$date"),list.get("period").get("to").get("$date"),list.get("source").get("product").get("vender")))
        # 데이터 가져와 하나의 클래스 생성, 해당 클래스를 리스트에 추가
    print(scount)  # 총 데이터 수 출력

    person_list.sort(key=lambda person: (person.id, person.start))  # person_list 정렬, id 오름차순 순서로

    person_list.append(Person(0, 0, 0, 0, 0))  # day csv에서 가장 마지막 row가 작성되지 않는 문제를 해결하기 위해 추가
    
    wr.writerow(['person_id', 'start_time', 'end_time', 'steps', 'device'])  # 해당 데이터에 대한 필드 이름
    count = 1  # 인원 수 카운트
    for i in range(0, len(person_list)):
        if i != len(person_list)-1:  # 마지막 데이터(사람)가 아니면
            if person_list[i].id != person_list[i+1].id:  # 다른 id(사람)면
                count += 1  # 인원 수 증가
            else:  # 동일한 id(사람)면
                count += 0  # 인원 수 증가X
        wr.writerow([person_list[i].id, person_list[i].start, person_list[i].end, person_list[i].step, person_list[i].vender])    
        # csv 파일에 쓰기

    csvfile.close()  # csv 파일 닫기
    print(count)  # 총 인원 수 출력
    

    # 하루로 합치기
    csvfile2 = open('output_samsung_day.csv', 'w', encoding='utf=8', newline='')  # csv 파일 '쓰기'로 열기
    wr2 = csv.writer(csvfile2)

    n = 1  # 인원 수 카운트
    dayPerson_list = []
    daySteps = 0

    for i in range(0, len(person_list)-1):
        #daySteps += person_list[i].step
        if person_list[i].id == person_list[i+1].id and person_list[i].start[:10] == person_list[i+1].start[:10]:
            # 같은 사람, 같은 날짜이면
            daySteps += person_list[i].step  # 같은 날짜의 다른 시간 걸음 수 더함

        elif person_list[i].id == person_list[i+1].id and person_list[i].start[:10] != person_list[i+1].start[:10]:
            # 같은 사람, 다른 날짜이면
            daySteps += person_list[i].step
            dayPerson_list.append(DayPerson(person_list[i].id, daySteps, person_list[i].start[:10], person_list[i].vender))
            # 두 날 중 전 날의 걸음 수와 함께 클래스로 만들어서 리스트에 추가
            daySteps = 0  # 총 하루의 걸음 수 초기화 -> 첫 걸음 수로
        else:  # 다른 사람이면
            n += 1
            daySteps += person_list[i].step
            dayPerson_list.append(DayPerson(person_list[i].id, daySteps, person_list[i].start[:10], person_list[i].vender))
            # 두 날 중 전 날의 걸음 수와 함께 클래스로 만들어서 리스트에 추가
            daySteps = 0  # 총 하루의 걸음 수 초기화 -> 첫 걸음 수로
    
    wr2.writerow(['person_id', 'date', 'steps', 'device'])  # 해당 데이터에 대한 필드 이름
    for i in range(0, len(dayPerson_list)):
        #print(dayPerson_list[i])
        wr2.writerow([dayPerson_list[i].id, dayPerson_list[i].date, dayPerson_list[i].steps, dayPerson_list[i].device])
        #print(person_list[i].start[:10])
        #10까지가 날짜

    csvfile2.close()
    print(n)
