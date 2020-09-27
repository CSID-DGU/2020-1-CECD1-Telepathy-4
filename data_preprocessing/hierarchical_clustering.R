#Session에서 work space 변경 후 실행하기

x<-read.csv('continue_rate_var_correct.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x)<-c("name", "mon-tue", "tue-wed", "wed-thu", "thu-fri", "fri-sat", "sat-sun", "sum", "means", "mm", "var")
#x
str(x)

x$name<-NULL # name column 없애기
#x$'mon-tue'<-NULL
#x$'tue-wed'<-NULL
#x$'wed-thu'<-NULL
#x$'thu-fri'<-NULL
#x$'fri-sat'<-NULL
#x$'sat-sun'<-NULL

x$means<-NULL
#x$sum<-NULL
x$mm<-NULL
#x$var<-NULL
#x

# 거리 행렬 구하기
rate_scaled<-scale(x) # 거리 기반이므로 표준화 필요
rate_scaled
#di<-dist(rate_scaled)
#as.matrix(di)[1:221,]
# 거리 행렬 모델 적용(계층적 군집화)
#fit1<-hclust(d = dist(rate_scaled), method="average")
# method의 종류보다는 어떤 의도 하에 분류할 것인지에 따라 설명 변수를 선택하는 것이 중요
#plot(fit1, hang=-1, cex=1.0)

fit2<-hclust(d = dist(rate_scaled), method="complete")
plot(fit2, hang=-1, cex=1.0)

#install.packages("NbClust")
library(NbClust)
nc<-NbClust(rate_scaled, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")

clusters1<-cutree(fit1, k=8)
table(clusters1)
clusters1

clusters2<-cutree(fit2, k=7)
table(clusters2)
clusters2
#average 12  or complete 12
rect.hclust(fit, k=8)

clusters<-cutree(fit, k=12)
table(clusters)
clusters