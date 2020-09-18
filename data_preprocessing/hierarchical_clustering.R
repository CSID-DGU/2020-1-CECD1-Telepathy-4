#Session에서 work space 변경 후 실행하기

x<-read.csv('continue_two_days_rate_delete.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x)<-c("name", "mon-tue", "tue-wed", "wed-thu", "thu-fri", "fri-sat", "sat-sun")
x
str(x)

x$name<-NULL # name column 없애기
x

# 거리 행렬 구하기
rate_scaled<-scale(x) # 거리 기반이므로 표준화 필요
rate_scaled
#di<-dist(rate_scaled)
#as.matrix(di)[1:221,]
# 거리 행렬 모델 적용(계층적 군집화)
fit<-hclust(d = dist(rate_scaled), method="complete")
# method의 종류보다는 어떤 의도 하에 분류할 것인지에 따라 설명 변수를 선택하는 것이 중요
plot(fit, hang=-1, cex=1.0)

install.packages("NbClust")
library(NbClust)
nc<-NbClust(rate_scaled, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")

clusters<-cutree(fit, k=8)
table(clusters)
clusters
#average 12  or complete 12
rect.hclust(fit, k=8)

clusters<-cutree(fit, k=12)
table(clusters)
clusters