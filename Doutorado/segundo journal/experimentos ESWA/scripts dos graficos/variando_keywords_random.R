a_ld = c(0.76, 0.74, 0.87,	0.84)
a_spkq = c(0.72,	0.62,	0.79,	0.73)

g_range <- range(0.6, 0.88)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(a_ld, type="o", col="blue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

#Add horizontal grid  
abline(h = c(0.6, 0.7, 0.8, 0.9), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("1", "2", "3", "4"))
axis(2, las=1, at=0:1)

box()

lines(a_ld, type="o", pch=22, lty=2, col="blue")
lines(a_spkq, type="o", pch=22, lty=2, col="red")

text(c(1,2,3,4), cex = 1.1, a_ld, labels = a_ld, col="blue", pos = 1,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, a_spkq, labels = a_spkq, col="red", pos = 1,offset = 0.6)

axis(side=2, at=c(0.6, 0.7, 0.8, 0.9))

#grid(3, 3, lwd = 2)

legend(3.05, 0.7, legend=c("SKPQ-LD", "SKPQ"),
       col=c("blue", "red"), lty=1:2, cex=1.2)
#----- Range -----

#r_ld = c(0.41,	0.39,	0.39,	0.39)
#r_spkq = c(0.29,	0.32,	0.34,	0.36)

g_range <- range(0.26, 0.42)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(r_ld, type="o", col="blue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

#Add horizontal grid  
abline(h = c(0.3, 0.35, 0.4, 0.45), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"))
axis(2, las=1, at=0:1)

box()

lines(r_ld, type="o", pch=22, lty=2, col="blue")
lines(r_spkq, type="o", pch=22, lty=2, col="red")

text(c(1,2,3,4), cex = 1.1, r_ld, labels = r_ld, col="blue", pos = 3,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, r_spkq, labels = r_spkq, col="red", pos = 1,offset = 0.6)

axis(side=2, at=c(0.3, 0.35, 0.4, 0.45))

#grid(3, 3, lwd = 2)

legend(3.2, 0.3, legend=c("RQ-LD", "RQ"),
       col=c("blue", "red"), lty=1:2, cex=1.2)