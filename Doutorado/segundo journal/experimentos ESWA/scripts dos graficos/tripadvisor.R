spkq_location = c(0.691,	0.666,	0.515,	0.472)
spkq_clean = c(1,	1,	1,	1)
spkq_rooms = c(0.978,	0.984,	0.987,	0.989)

g_range <- range(0.39, 1.1)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(spkq_location, type="o", col="blue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

v = c(0.4, 0.6, 1)

#Add horizontal grid  
abline(h = v, lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:5,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"))
axis(2, las=1, at=0:1)

box()

lines(spkq_location, type="o", pch=22, lty=2, col="blue")
lines(spkq_clean, type="o", pch=22, lty=2, col="red")
lines(spkq_rooms, type="o", pch=22, lty=2, col="black")

text(c(1.05,2,3,3.95), cex = 1.1, spkq_location, labels = spkq_location, col="blue", pos = 1,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, spkq_clean, labels = spkq_clean, col="red", pos = 3,offset = 0.6)
text(c(1.05,2,3,3.95), cex = 1.1, spkq_rooms, labels = spkq_rooms, col="black", pos = 1,offset = 0.5)

axis(side=2, at=c(0.4,0.6))

legend(2.5, 0.89, legend=c(dQuote("great location"), dQuote("clean place"), dQuote("cozy rooms")),
       col=c("blue", "red", "black"), lty=1:3, cex=1.2)

