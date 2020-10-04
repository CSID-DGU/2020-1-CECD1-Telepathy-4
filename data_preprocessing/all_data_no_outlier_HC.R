#Session에서 work space 변경 후 실행하기

x<-read.csv('all_data_no_outlier_HR_new.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x)

x$name<-NULL # name column 없애기
x

# 거리 행렬 구하기
rate_scaled<-scale(x) # 표준화
#rate_scaled

fit1<-hclust(d = dist(rate_scaled), method="complete") # 표준화한거
plot(fit1, hang=-1, cex=1.0)

fit2<-hclust(d = dist(x), method="complete") # 표준화 안 한거
plot(fit2, hang=-1, cex=1.0)

library(NbClust)
nc<-NbClust(rate_scaled, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")
nc<-NbClust(x, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")

clusters1<-cutree(fit1, k=12) # 표준화 한거 : 개별로
table(clusters1)
clusters1

clusters2<-cutree(fit2, k=12) # 표준화 안한거 : 이게 더 좋음 : 왜인지는 나도 몰라
table(clusters2)
clusters2

setwd('C:/Users/oym91/OneDrive/Desktop/graph_all')
write.csv(clusters1, file="no_outlier_scaled_12.csv", row.names = FALSE)
# no_scaled.csv : days_ends 제거 후 k = 12
# no_scaled_2.csv : days_ends 포함(모든 변수 포함) 후 k = 10
# no_scaled_3.csv : days_ends 포함(모든 변수 포함) 후 k = 12
# no_scaled_4.csv : days_ends 포함(모든 변수 포함) 후 k = 11