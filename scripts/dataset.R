library(clValid)

data = read.table("coordinates.txt")

internAmbos=clValid(data, 2:30, clMethods=c("kmeans"), validation="internal")
op=par(no.readonly = TRUE)
par(mfrow=c(2,2),mar=c(4,4,3,1))
plot(internAmbos, legend=FALSE)
legend("right", clusterMethods(internAmbos), col=1:9, lty=1:9, pch=paste(1:9))
par(op)

#clvalid não foi capaz de lidar com a quantidade de POIs