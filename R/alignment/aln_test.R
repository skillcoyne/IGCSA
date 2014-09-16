rank_pp<-function(num)
{
  digits=0
  r=round(num,digits)
  while (r <= 0)
  {
    digits=digits+1
    r=round(num,digits)
  }
  return(r)
}

dir = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments"
gen = "HCC1954"

dir = paste(dir, gen, sep="/")
d = read.table(paste(paste(dir, gen, sep="/"), ".stats", sep=""), header=T)

#d = read.table("/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.6.4/HCC1954.6.4.stats", header=T)
#x = read.table("/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.6/HCC1954.6.stats", header=T)
d = d[ order(-d$ppairs), ]
range(d$ppairs*100)

# normal...
plot(sort(d$ppairs))
ks.test(d$ppairs, pnorm, mean(d$ppairs), sd(d$ppairs))

#d = d[d$ppairs > mean(d$ppairs),]

counts=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(d$scores), ","))), nrow=nrow(d), byrow=T))
colnames(counts) = c('leftb1', 'leftb2', 'bp', 'rightb1', 'rightb2')
rownames(counts) = d$chr

# So counts has a high correlation with properly paired reads...it has to so that's not a surprise
cor.test( rowSums(counts), d$ppairs )

# If I drop pairs with 0 counts in them. pairs with one or more 0 counts, these are centromere flanking regions
# This doesn't have to be run now that I'm not creating centromeres
#row_ind=unique(which(counts <= 0, arr.ind=T)[,'row'])
#counts=counts[ -row_ind, ]
#d=d[which(d$chr %in% rownames(counts)),] 
# doesn't alter the correlation
#cor.test( rowSums(counts), d$ppair )

## lengths
lengths=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(d$lengths), ","))), nrow=nrow(d), byrow=T))
colnames(lengths) = colnames(counts)
rownames(lengths) = rownames(counts)

# properly paired ratio and lengths do not correlate, which is also correct
cor.test(rowSums(lengths), d$ppairs)

# Counts correlates, though not highly, with the band length
cor.test(t(lengths),t(counts))

# Adjusted for the ratio of properly paired reads, correlation doesn't change - drops a bit with mixed bands
rp = sapply(d$ppairs*100, rank_pp)
adjcounts=t( (t(counts))*rp)
#cor.test(lengths,adjcounts)
#ks.test(adjcounts, pnorm, mean(adjcounts), sd(adjcounts)) # normal?

# Adjust the counts for length and it drops 
adjcounts=adjcounts/lengths
cor.test(t(lengths),t(adjcounts))

ordered_cnts = t(adjcounts[ order(-rowSums(adjcounts)),])

## So, looking just at the top bands by ppair
#top = d[ which(d$ppairs >= mean(d$ppairs)+sd(d$ppairs)), ]
#ordered_cnts = ordered_cnts[, which(colnames(ordered_cnts) %in% top$chr)]


left=ordered_cnts[c('leftb2', 'leftb1'),]
right=ordered_cnts[c('rightb1', 'rightb2'),]
lr_ratio=abs(log(colSums(left)/colSums(right)))

# Best match by ratio...not by ppairs though
#lr_ratio[which.min(abs(lr_ratio-1.0))]
#ordered_cnts[,lr_ratio[which.min(abs(lr_ratio-1.0))]]
#ordered_cnts = ordered_cnts[,names(lr_ratio[lr_ratio > 0.5 & lr_ratio < 1.5])]


d[d$chr %in% names(lr_ratio[lr_ratio > 0.5 & lr_ratio < 1.5]), ]

ppairs = d[,'ppairs']
names(ppairs) = d$chr

m = matrix(ncol=2,nrow=0)
for (name in names(ppairs))
  m = rbind(m, c(ppairs[name], lr_ratio[name]))
colnames(m) = c('ppairs','lr.ratio')
rownames(m) = names(ppairs)
plot(m)
abline(0.5, 0.025)
abline(1.5, 0.025)
lines(0.025, 3.5, type='h')

#top = m[m[,'lr.ratio'] > 0.5 & m[,'lr.ratio'] < 1.5 & m[,'ppairs'] > 0.025, ]
top = m[ m[,'ppairs'] >= 0.025, ]


ordered_cnts = ordered_cnts[, which(colnames(ordered_cnts) %in% rownames(top))   ]
#ordered_cnts = ordered_cnts[, grep('(^1(p|q).*-5|^5.*-1(p|q).*)',  colnames(ordered_cnts), value=T)]

colors=c('red','red','green','blue','blue')
of_bar=barplot(ordered_cnts, beside=TRUE, ylim=c(0,max(ordered_cnts)), 
        legend.text=rownames(ordered_cnts), 
        args.legend=list(x="topright", bty="n"), 
        col=colors, las=2, cex.names=0.8)

points(of_bar[3,],lr_ratio[rownames(top)], type='o', pch=19)
text(of_bar[3,], lr_ratio[rownames(top)], pos=3, labels=round(lr_ratio[rownames(top)], 2))

# Best match
lr_ratio[which.min(abs(lr_ratio[rownames(top)]-1.0))]


points(of_bar[3,],d$ppairs, type='o', pch=17, col='red')
text(of_bar[3,], d$ppairs, pos=1, labels=round(d$ppairs, 3)*100)


