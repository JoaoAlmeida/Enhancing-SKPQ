# f_ld = c(0.95, 0.89,	0.86,	0.85)
# f_spkq = c(0.95,	0.95,	0.95,	0.95)
# 
# g_range <- range(0.84, 1)
# 
# #define a area onde será plotado o gráfico, em polegadas (inchs)
# par(pin=c(5.5,3)) 
# 
# plot(f_ld, type="o", col="blue", ylim=g_range, 
#      axes=FALSE, ann=FALSE)
# 
# #Add horizontal grid  
# abline(h = c(0.85, 0.9, 0.95), lty = 2, col = "grey")
# #Add vertical grid
# abline(v = 0:4,  lty = 2, col = "grey")
# 
# axis(1, at=1:4, lab=c("1", "2", "3", "4"))
# axis(2, las=1, at=0:1)
# 
# box()
# 
# lines(f_ld, type="o", pch=22, lty=2, col="blue")
# lines(f_spkq, type="o", pch=22, lty=2, col="red")
# 
# text(c(1,2,3,4), cex = 1.1, f_ld, labels = f_ld, col="blue", pos = 1,offset = 0.6)
# text(c(1,2,3,4), cex = 1.1, f_spkq, labels = f_spkq, col="red", pos = 3,offset = 0.6)
# 
# axis(side=2, at=c(0.85, 0.9, 0.95))
# 
# #grid(3, 3, lwd = 2)
# 
# legend(3.05, 0.93, legend=c("SKPQ-LD", "SKPQ"),
#        col=c("blue", "red"), lty=1:2, cex=1.2)
#----- Range -----

r_ld = c(0.41,	0.52,	0.53,	0.45, 0.48)
r_spkq = c(0.54,	0.62,	0.73,	0.73, 0.82)

g_range <- range(0.4, 0.85)

#define a area onde será plotado o gráfico, em polegadas (inchs)
par(pin=c(5.5,3)) 

plot(r_ld, type="o", col="blue", ylim=g_range, 
     axes=FALSE, ann=FALSE)

#Add horizontal grid  
abline(h = c(0.4, 0.5, 0.6, 0.7, 0.8), lty = 2, col = "grey")
#Add vertical grid
abline(v = 0:4,  lty = 2, col = "grey")

axis(1, at=1:5, lab=c("200", "400", "600", "800", "1000"))
axis(2, las=1, at=0:1)

box()

lines(r_ld, type="o", pch=22, lty=2, col="blue")
lines(r_spkq, type="o", pch=22, lty=2, col="red")

text(c(1,2,3,4,5), cex = 1.1, r_ld, labels = r_ld, col="blue", pos = 3,offset = 0.6)
text(c(1,2,3,4,5), cex = 1.1, r_spkq, labels = r_spkq, col="red", pos = 1,offset = 0.6)

axis(side=2, at=c(0.4, 0.5, 0.6, 0.7, 0.8))

#grid(3, 3, lwd = 2)

legend(1, 0.8, legend=c("RQ-LD", "RQ"),
       col=c("blue", "red"), lty=1:2, cex=1.2)
