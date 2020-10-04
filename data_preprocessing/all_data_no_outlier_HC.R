#Session에서 work space 변경 후 실행하기
#no outlier : 218 data
x<-read.csv('all_data_no_outlier_HR_new.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x)

x$name<-NULL # name column 없애기
#월-화 / 화-수 / 수-목 / 목-금 / 금-토 / 토-일 증감률
#x$days_sat  #주중-토요일 증감률
#x$days_sun  #주중-일요일 증감률
#x$days_ends  #주중-주말 증감률
#x$var  #분산
x

rate_scaled<-scale(x) # 표준화
#rate_scaled

#표준화O
fit1<-hclust(d = dist(rate_scaled), method="complete") #거리기반 계층적 클러스터링
plot(fit1, hang=-1, cex=1.0)

#표준화X
fit2<-hclust(d = dist(x), method="complete") #거리기반 계층적 클러스터링
plot(fit2, hang=-1, cex=1.0)

library(NbClust)
nc<-NbClust(rate_scaled, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")
nc<-NbClust(x, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")

#표준화O
clusters1<-cutree(fit1, k=12)
table(clusters1)
clusters1

#표준화X
clusters2<-cutree(fit2, k=12)
table(clusters2)
clusters2


setwd('C:/Users/oym91/OneDrive/Desktop/graph_all')
write.csv(clusters1, file="no_outlier_scaled_12.csv", row.names = FALSE)

setwd('C:/Users/oym91/OneDrive/Desktop/graph_all')
write.csv(clusters2, file="no_outlier_no_scaled_12.csv", row.names = FALSE)