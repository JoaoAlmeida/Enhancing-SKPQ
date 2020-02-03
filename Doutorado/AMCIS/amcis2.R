#max = c(235,146,33,38,18)
#avg = c(39.84,14.45,12.10,7.45,5.84)
#min = c(20,1,1,2,0)

max = c(146, 33, 38, 18)
avg = c(14.45, 12.10, 7.45, 5.84)
min = c(1, 1, 2, 0)

g_range <- range(-8, 146)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(max, ylab= "# of pages", type="o", col="darkgreen", ylim=g_range, pch=2,
     axes=FALSE, ann=TRUE)



abline(h = c(1, 25, 50, 75, 100, 125, 150), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("Negative - VI", "Negative - I", "Positive - VR", "Positive - R"))
axis(2, at=c(1, 25, 50, 75, 100, 125, 150), lab=c(1, 25, 50, 75, 100, 125, 150))

box()

lines(avg, type="o", pch=13, lty=2, col="darkblue")
lines(min, type="o", pch=20, lty=3, col="darkred")

#pos =1 ou 3
text(c(1), cex = 1.1, 146, labels = 146, col="darkgreen", pos = 1,offset = 0.3)
text(c(2,3,4), cex = 1.1, c(33, 38, 18), labels = c(33, 38, 18), col="darkgreen", pos = 3,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, avg, labels = avg, col="darkblue", pos = 3,offset = 0.3)
text(c(1,2,3,4), cex = 1.1, min, labels = min, col="darkred", pos = 1,offset = 0.3)

axis(side=2, at=c(1, 25, 50, 100, 150))

#grid(3, 3, lwd = 2)

legend(3.2, 125, legend=c("Max", "Avg", "Min"),
       col=c("darkgreen", "darkblue", "darkred"), lty=1:3, pch=c(2,13,20), cex=1.2)

