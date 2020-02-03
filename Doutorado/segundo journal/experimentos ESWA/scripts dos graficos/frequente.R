pskpq = c(0.86, 0.8, 0.7, 0.64)
skpq = c(0.68, 0.59, 0.57, 0.56)
fuzzy = c(0.58, 0.56, 0.54, 0.53)
jw = c(0.47, 0.5, 0.5, 0.51)

g_range <- range(0.43, 0.9)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(pskpq, type="o", pch=1, lty=1, col="darkblue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

#Add horizontal grid  
abline(h = c(0.5, 0.6, 0.7, 0.8), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"))
axis(2, las=1, at=0:1)

box()

lines(skpq, type="o", pch=2, lty=2, col="darkred")
lines(fuzzy, type="o", pch=2, lty=3, col="darkgreen")
lines(jw, type="o", pch=2, lty=4, col="darkorange")

text(c(1,2,3,4), cex = 1.1, pskpq, labels = pskpq, col="blue", pos = 3,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, skpq, labels = skpq, col="red", pos = 3,offset = 0.6)
text(c(1,2,3), cex = 1.1, fuzzy[1-3], labels = fuzzy, col="darkgreen", pos = 4,offset = 0.6)
text(c(4), cex = 1.1, 0.53, labels = 0.53, col="darkgreen", pos = 2,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, jw, labels = jw, col="darkorange", pos = 1,offset = 0.6)

axis(side=2, at=c(0.5, 0.6, 0.7, 0.8))

legend(3.25, 0.88, legend=c("P-SKPQ", "SKPQ", "FUZZY", "JW"),
       col=c("blue", "red","darkgreen", "darkorange"), lty=1:4)

