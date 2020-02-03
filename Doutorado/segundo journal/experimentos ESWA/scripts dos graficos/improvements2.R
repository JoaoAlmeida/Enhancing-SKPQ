a_ld = c(0.76,	0.76,	0.74,	0.74)
a_spkq = c(0.72,	0.65,	0.63,	0.61)

f_ld = c(0.94,	0.92,	0.90,	0.90)
f_spkq = c(0.95,	0.92,	0.91,	0.90)

imp_a = (a_ld - a_spkq )/ (a_spkq) * 100
imp_f = (f_ld - f_spkq )/ (f_spkq) * 100

bars = data.frame(imp_f)

barplot(t(as.matrix(bars)), main="Relative NDCG improvements", 
        col=c("darkblue","red"),
        # args.legend = list(x ='top', bty='o'), ylim=my.range,
        names.arg = c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"),
        # legend = c("SKPQ-LD with random keywords", "SKPQ-LD with the most frequent keywords"), 
        beside=TRUE)

#x <-1:10
#y <-11:20
#plot(bars,type="n", xaxt="n", yaxt="n")
#my.legend.size <-legend("top",c("SKPQ-LD with random keywords", "SKPQ-LD with the most frequent keywords"), plot = FALSE)

#my.range <- range(imp_a)
#my.range[2] <- 1.04*(my.range[2]+my.legend.size$rect$h)

#----- Range -----

rf_ld = c(0.41,	0.39,	0.39,	0.39)
rf_spkq = c(0.29,	0.32,	0.34,	0.3)

ra_ld = c(0.39,	0.33,	0.35,	0.36)
ra_spkq = c(0.34,	0.37,	0.38,	0.39)

imp_ra = (ra_ld - ra_spkq )/ (ra_spkq) * 100
imp_rf = (rf_ld - rf_spkq )/ (rf_spkq) * 100

bars2 = data.frame(imp_ra, imp_rf, imp_a, imp_f)

xx = barplot(t(as.matrix(bars2)), 
             #main="Relative NDCG improvements", 
             col=c("darkblue", "darkred", "darkgreen", "yellow"),
             # args.legend = list(x ='top', bty='o'), ylim=my.range,
             names.arg = c("NDCG@5", "NDCG@10", "NDCG@15", "NDCG@20"),
             # legend = c("SKPQ-LD with random keywords", "SKPQ-LD with the most frequent keywords"), 
             beside=TRUE)

#text(x = xx, y = round(t(as.matrix(bars2)),2), label = round(t(as.matrix(bars2)),2), offset = 0.4, pos = 1, cex = 0.8, col = "black")

legend(3.3, 42, inset=.1, bty = "n",
       c("RQ@R","RQ@F","SKPQ@R","SkPQ@F"), 
       fill=c("darkblue", "darkred", "darkgreen", "yellow"), horiz=FALSE, cex=0.8)

