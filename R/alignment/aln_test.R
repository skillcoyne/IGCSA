
d = read.table("/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.6/HCC1954.6.stats", header=T)
d = d[ order(-d$ppairs), ]
range(d$ppairs*100)

counts=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(d$scores), ","))), nrow=nrow(d), byrow=T))
colnames(counts) = c('leftb1', 'leftb2', 'bp', 'rightb1', 'rightb2')
rownames(counts) = d$chr

# So counts has a high correlation with properly paired reads...that's what I expected
cor.test( rowSums(counts), d$ppairs )
# just checking - random
cor.test(sample(min(counts):max(counts), nrow(d), replace=T), d$ppairs)


# If I drop pairs with 0 counts in them. pairs with one or more 0 counts, interestingly these are centromeres
row_ind=unique(which(counts <= 0, arr.ind=T)[,'row'])
counts=counts[ -row_ind, ]

d=d[which(d$chr %in% rownames(counts)),] 

# doesn't alter the correlation
cor.test( rowSums(counts), d$ppair )

## lengths
lengths=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(d$lengths), ","))), nrow=nrow(d), byrow=T))
colnames(lengths) = colnames(counts)
rownames(lengths) = rownames(counts)

# reads and lengths do correlate as well though not as highly as counts do
cor.test(rowSums(lengths), d$ppairs)

counts=t(counts)
lengths=t(lengths)

# as expected the number of matches correlates with the length of the band
cor.test(lengths,counts)

# Adjusted for the ratio of properly paired reads, correlation doesn't change
adjcounts=t( (t(counts))*d$ppairs)
cor.test(lengths,adjcounts)
ks.test(adjcounts, pnorm, mean(adjcounts), sd(adjcounts)) # normal?

# Adjust for length and it drops 
adjcounts=adjcounts/lengths
cor.test(lengths,adjcounts)


ordered_cnts = adjcounts[ order(-rowSums(adjcounts)),]

## So, looking just at the top bands by ppair
top=d[ which(d$ppairs >= mean(d$ppairs)), ]
ordered_cnts = ordered_cnts[, which(colnames(ordered_cnts) %in% top$chr)]

#par(mfrow=c(2,1))

of_bar=barplot(ordered_cnts, beside=TRUE, ylim=c(0,0.030), 
        legend.text=rownames(ordered_cnts), 
        args.legend=list(x="topright", bty="n"), 
        col=c(32,33,'green',26,30), las=2, cex.names=0.8)


left=ordered_cnts[c('leftb2', 'leftb1'),]
right=ordered_cnts[c('rightb1', 'rightb2'),]
lr_ratio=abs(colSums(left)/colSums(right))

points(of_bar[3,],lr_ratio/1000, type='o', pch=19)
text(of_bar[3,], lr_ratio/1000, pos=3, labels=round(lr_ratio, 2))

#points(of_bar[3,],d$ppairs, type='o', pch=17, col='red')
#text(of_bar[3,], d$ppairs, pos=1, labels=round(d$ppairs, 3)*100)

# These don't correlate at all...which is good as the lr_ratio is already adjusted for ppairs
# cor.test(lr_ratio, d$ppairs)
