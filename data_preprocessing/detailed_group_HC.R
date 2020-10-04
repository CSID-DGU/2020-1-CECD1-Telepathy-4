#Session에서 work space 변경 후 실행하기
#no outlier : 218 data - scaled 12의 세부 그룹 clustering
x<-read.csv('final_best_grouping_no_outlier.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(x) # 요일별 걸음수와 group 번호
y<-read.csv('final_no_outlier_forHC.csv', header = TRUE, stringsAsFactors = FALSE, na.strings = "")
names(y) # HC에 사용되는 변수 10개

# 번호별로 다시 clustering : 큰 그룹만(1, 2)
# 1
name<-c()
mon2tue<-c()
tue2wed<-c()
wed2thu<-c()
thu2fri<-c()
fri2sat<-c()
sat2sun<-c()
days2sat<-c()
days2sun<-c()
days2ends<-c()
varr<-c()

for(i in 1:length(x$scaled12)){
  if(x$scaled12[i]== 1){
    name<-c(name, y$name[i])
    mon2tue<-c(mon2tue, y$mon_tue[i])
    tue2wed<-c(tue2wed, y$tue_wed[i])
    wed2thu<-c(wed2thu, y$wed_thu[i])
    thu2fri<-c(thu2fri, y$thu_fri[i])
    fri2sat<-c(fri2sat, y$fri_sat[i])
    sat2sun<-c(sat2sun, y$sat_sun[i])
    days2sat<-c(days2sat, y$days_sat[i])
    days2sun<-c(days2sun, y$days_sun[i])
    days2ends<-c(days2ends, y$days_ends[i])
    varr<-c(varr, y$var[i])
  }
}
group1<-data.frame(name, mon2tue, tue2wed, wed2thu, thu2fri, fri2sat, sat2sun, days2sat, days2sun, days2ends, varr)
group1 # 번호 1로 분류된 data

# 2
name<-c()
mon2tue<-c()
tue2wed<-c()
wed2thu<-c()
thu2fri<-c()
fri2sat<-c()
sat2sun<-c()
days2sat<-c()
days2sun<-c()
days2ends<-c()
varr<-c()

for(i in 1:length(x$scaled12)){
  if(x$scaled12[i]== 2){
    name<-c(name, y$name[i])
    mon2tue<-c(mon2tue, y$mon_tue[i])
    tue2wed<-c(tue2wed, y$tue_wed[i])
    wed2thu<-c(wed2thu, y$wed_thu[i])
    thu2fri<-c(thu2fri, y$thu_fri[i])
    fri2sat<-c(fri2sat, y$fri_sat[i])
    sat2sun<-c(sat2sun, y$sat_sun[i])
    days2sat<-c(days2sat, y$days_sat[i])
    days2sun<-c(days2sun, y$days_sun[i])
    days2ends<-c(days2ends, y$days_ends[i])
    varr<-c(varr, y$var[i])
  }
}
group2<-data.frame(name, mon2tue, tue2wed, wed2thu, thu2fri, fri2sat, sat2sun, days2sat, days2sun, days2ends, varr)
group2 # 번호 2로 분류된 data

# group1
name1<-group1$name
group1$name<-NULL
rate_scaled1<-scale(group1) # 표준화
# group1 - 표준화O
fit1_group1<-hclust(d = dist(rate_scaled1), method="complete") #거리기반 계층적 클러스터링
plot(fit1_group1, hang=-1, cex=1.0)
# group1 - 표준화X
fit2_group1<-hclust(d = dist(group1), method="complete") #거리기반 계층적 클러스터링
plot(fit2_group1, hang=-1, cex=1.0)

# group2
name2<-group2$name
group2$name<-NULL
rate_scaled2<-scale(group2) # 표준화
# group2 - 표준화O
fit1_group2<-hclust(d = dist(rate_scaled2), method="complete") #거리기반 계층적 클러스터링
plot(fit1_group2, hang=-1, cex=1.0)
# group2 - 표준화X
fit2_group2<-hclust(d = dist(group2), method="complete") #거리기반 계층적 클러스터링
plot(fit2_group2, hang=-1, cex=1.0)


library(NbClust)
nc<-NbClust(rate_scaled1, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")
nc<-NbClust(group1, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")
nc<-NbClust(rate_scaled2, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")
nc<-NbClust(group2, distance = "euclidean",min.nc = 2,max.nc = 15,method = "complete")


# group1 - 표준화O
scaled_group1<-cutree(fit1_group1, k=2)
table(scaled_group1)
scaled_group1
# group1 - 표준화X
noscaled_group1<-cutree(fit2_group1, k=2)
table(noscaled_group1)
noscaled_group1

# group2 - 표준화O
scaled_group2<-cutree(fit1_group2, k=2)
table(scaled_group2)
scaled_group2
# group2 - 표준화X
noscaled_group2<-cutree(fit2_group2, k=4)
table(noscaled_group2)
noscaled_group2

group1_1<-data.frame(name1, scaled_group1)
setwd('C:/Users/oym91/OneDrive/Desktop/graph_all/1004/img_219_scaled_12/1')
#write.csv(name1, file="group1_scaled_2_HC.csv", row.names = FALSE)
write.csv(group1_1, file="group1_scaled_2_HC.csv", row.names = FALSE)

group2_1<-data.frame(name2, scaled_group2)
setwd('C:/Users/oym91/OneDrive/Desktop/graph_all/1004/img_219_scaled_12/2')
write.csv(group2_1, file="group2_scaled_2_HC.csv", row.names = FALSE)
group2_2<-data.frame(name2, noscaled_group2)
setwd('C:/Users/oym91/OneDrive/Desktop/graph_all/1004/img_219_scaled_12/2')
write.csv(group2_2, file="group2_no_scaled_4_HC.csv", row.names = FALSE)