rmse = c(0.56,	0.37,	0.43,	0.60)


g_range <- range(0.3, 0.65)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(rmse, xlab= "Profiles", ylab= "RMSE values", type="o", col="blue", ylim=g_range, pch=2,
     axes=FALSE, ann=TRUE)



abline(h = c(0.5, 0,55, 0.6, 0.65), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("ROOM", "VALUE", "LOCATION", "SERVICE"))
axis(2, las=1, at=0:1)

box()

lines(rmse, type="o", pch=22, lty=2, col="blue")


text(c(1,2,3,4), cex = 1.1, rmse, labels = a_ld, col="blue", pos = 3,offset = 0.6)


axis(side=2, at=c(0.6, 0,65, 0.7, 0.75))

#grid(3, 3, lwd = 2)

legend(0.9, 0.63, legend=c("RMSE", "SKPQ"),
       col=c("blue", "red"), lty=1:2, cex=1.2)

