#max = c(235,146,33,38,18)
#avg = c(39.84,14.45,12.10,7.45,5.84)
#min = c(20,1,1,2,0)

max = c(146,33,38,18)
avg = c(14.45,12.10,7.45,5.84)
min = c(1,1,2,0)

bars = data.frame(max,avg,min)

barplot(t(as.matrix(bars)), ylab = "# of pages", main="Statistics of collected data",
        col=c("darkgreen","darkblue", "darkred"),
        names.arg = c("Negative - VI", "Negative - I", "Positive - VR", "Positive - R"),
        legend.text=FALSE,
        beside=TRUE)

legend(15, 100, inset=.1, bty = "n",
       c("Max","Avg","Min"), 
       fill=c("darkgreen", "darkblue", "darkred"), horiz=FALSE, cex=0.8)

