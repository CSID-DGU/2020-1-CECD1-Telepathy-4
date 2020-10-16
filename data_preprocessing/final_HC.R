#Session에서 work space 변경 후 실행하기
x<-read.csv('all_data_forHC.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
#x<-read.csv('real_data_forHC.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x) # column 이름 확인
x<-x[c(order(-x$v_rate)),] # 분산 bool형 오름차순 정렬
x

ver<-data.frame(x$v_rate, x$count, x$max_ends, x$min_ends, x$total_rate)

#표준화X
fit_v<-hclust(d = dist(ver), method="complete") #거리기반 계층적 클러스터링
plot(fit_v, hang=-1, cex=1.0)

gg<-cutree(fit_v, k=6) # k = 6
table(gg) # 각 group으로 분류된 데이터의 갯수 출력
gg # grouping된 결과
group<-data.frame(gg) # 데이터 프레임 형식으로 전환
group # 데이터 프레임 형식의 grouping된 결과

tmp<-data.frame(x$name, ver, group) # 이름과 변수, 나뉜 그룹으로 데이터 프레임 생성
tmp<-tmp[c(order(tmp$gg)),] # group으로 오름차순 정렬
tmp

# 그래프 별로 각기 다른 폴더에 생성하기 위한 csv 생성
result<-data.frame(x$name, group, x$mon, x$tue, x$wed, x$thu, x$fri, x$sat, x$sun)
result<-result[c(order(result$gg)),] # group으로 오름차순 정렬
result

setwd('C:/Users/oym91/OneDrive/Desktop/graph_all/1006')
write.csv(result, file="all_classification.csv", row.names = FALSE) # csv로 저장
#write.csv(result, file="real_classification2.csv", row.names = FALSE)
