pskpq = c(0.81,	0.78,	0.68,	0.61)
skpq = c(0.43,	0.44,	0.42,	0.40)
fuzzy = c(0.59, 0.57, 0.56, 0.54)
jw = c(0.66, 0.58, 0.55, 0.54)

g_range <- range(0.35, 0.86)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(pskpq, type="o", pch=1, lty=1, col="darkblue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

#Add horizontal grid  
abline(h = c(0.4, 0.5, 0.6, 0.7, 0.8), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:4, lab=c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"))
axis(2, las=1, at=0:1)

box()

#lines(pskpq, tpye="o", pch=1, lty=1, col="darkblue")
lines(skpq, type="o", pch=2, lty=2, col="darkred")
lines(fuzzy, type="o", pch=2, lty=3, col="darkgreen")
lines(jw, type="o", pch=2, lty=4, col="darkorange")

text(c(1,2,3,4), cex = 1.1, pskpq, labels = pskpq, col="blue", pos = 3,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, skpq, labels = skpq, col="red", pos = 1,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, fuzzy, labels = fuzzy, col="darkgreen", pos = 1,offset = 0.6)
text(c(1,2,3,4), cex = 1.1, jw, labels = jw, col="darkorange", pos = 3,offset = 0.6)

axis(side=2, at=c(0.4, 0.5, 0.6, 0.7, 0.8))

#grid(3, 3, lwd = 2)

legend(3.25, 0.88, legend=c("P-SKPQ", "SKPQ", "FUZZY", "JW"),
       col=c("blue", "red","darkgreen", "darkorange"), lty=1:4)

