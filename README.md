# 2020-1-CECD1-Telepathy-4
컴퓨터공학종합설계1 01분반 텔레파시

## 프로젝트 개요
구글 피트니스를 이용한 개인 건강 분석 서비스 개발

## 프로젝트 주제 및 설명
구글 피트니스를 이용한 개인 활동 패턴 분석 및 맞춤형 콘텐츠 제공 안드로이드 애플리케이션 개발

## 팀원
- 팀장: [고노윤](https://github.com/nohyoonko)
- 팀원: [이주영](https://github.com/1122jinny)
- 팀원: [오유민](https://github.com/ym-o)
- 팀원: [유현지](https://github.com/LUV2KUIT)

## 프로젝트 특징
![특징](https://user-images.githubusercontent.com/55729131/95025816-871cc400-06c7-11eb-83a9-8a1656866047.JPG)

## 프로젝트 주요 내용
### 안드로이드
- Open API : Google Fit API 사용
- 'Google Fitness'와 연동
- 일/주/월/년 통계 및 나와의 경쟁
### 데이터 분석 & 활동 패턴 분류
- 걸음 수 데이터를 바탕으로 다양한 활동 패턴 정의
- 계층적 클러스터링 사용
#### 6가지 활동 패턴
1. 밸런스형 : 평균 일주일 간의 걸음 수 차이가 적은 경우
2. 주말 감소형 : 주중과 비교하여 주말 걸음 수가 감소하는 경우
3. 특정 요일 감소형 : 다른 요일에 비해 유독 크게 감소하는 요일이 있는 경우
4. 짧은 증감주기형 : 짧은 주기로 증가와 감소가 반복하여 불규칙적인 경우
5. 특정 요일 증가형 : 다른 요일에 비해 유독 크게 증가하는 요일이 있는 경우
6. 주말 증가형 : 주중과 비교하여 주말 걸음 수가 증가하는 경우
- 참고 문헌 : 김병준, 김유정, 이중식 (2018). 웨어러블 디바이스 데이터를 통해 분석한 요일별 활동량 패턴 유형에 대한 탐색적 연구. 한국HCI학
회 학술대회, 460-464
### 맞춤형 콘텐츠
- 사용자의 활동 패턴을 바탕으로 한 맞춤형 콘텐츠(뉴스, 동영상, 제품 등) 제공
