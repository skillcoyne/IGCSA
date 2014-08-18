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

get_counts<-function(df)
  {
  cts=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(df$scores), ","))), nrow=nrow(df), byrow=T))
  colnames(cts) = c('leftb1', 'leftb2', 'bp', 'rightb1', 'rightb2')
  rownames(cts) = df$chr

  return(cts)
  }

get_lengths<-function(df)
  {
  lgt=as.data.frame(matrix(as.numeric(unlist(strsplit( as.character(df$lengths), ","))), nrow=nrow(df), byrow=T))
  colnames(lgt) = c('leftb1', 'leftb2', 'bp', 'rightb1', 'rightb2')
  rownames(lgt) = df$chr
  return(lgt)
  }

band_ratio<-function(df, ajc)
  {
  if (rownames(ajc)[1] != "leftb1") ajc = t(ajc)
  
  left=ajc[c('leftb2', 'leftb1'),]
  
  #left=ajc[c('leftb2'),]
  right=ajc[c('rightb1', 'rightb2'),]
  lr_ratio=abs(log( abs(colSums(left)/colSums(right)) ))
  #lr_ratio=abs(log( abs((left)/colSums(right)) ))
  
  ppairs=df[match(names(lr_ratio), df$chr), 'ppairs']

  lp=as.data.frame(cbind(lr_ratio, ppairs))
  lp$rank=sapply(lp$ppairs*100, rank_pp)
  lp=lp[order(-lp$rank,lp$lr_ratio),]
  
  ranks=names(table(lp$rank))
  
  plot(lp$lr_ratio)
  text(lp$lr_ratio, labels=rownames(lp), pos=3)
  colors=rainbow(length(ranks))
  for (i in 1:length(ranks))
    {
    high = lp[which(lp$rank == ranks[i]), ]
    points(which(lp$lr_ratio %in% high$lr_ratio), high$lr_ratio, type='p', col=colors[i], pch=16)
    }
  legend("topleft", ranks, col=colors, pch=16, title="Rank")
  
  return(lp)
  }


plot_counts<-function(adjusted_counts, lp, cutoff)
  {
  if (rownames(adjusted_counts)[1] == 'leftb1') adjusted_counts = t(adjusted_counts)
  
  rank = lp[lp$rank >= cutoff,]
  ordered_counts = adjusted_counts[ order(-rowSums(adjusted_counts)),]
  
  #rank = rank[rownames(ordered_counts),]
  ordered_counts = t(ordered_counts[ rownames(rank),])
  
  colors=rainbow(nrow(ordered_counts))
  of_bar=barplot(ordered_counts, beside=TRUE, ylim=c(0,round(max(ordered_counts))), 
                 legend.text=rownames(ordered_counts), 
                 args.legend=list(x="topright", bty="n"), 
                 col=colors, las=2, cex.names=0.8)
  title(ylab="Length adjusted counts")
  
  points(of_bar[3,],mean(c(0,max(ordered_counts)))*rank$'rank'/10, type='o', pch=19, col='blue')
  text(of_bar[3,], mean(c(0,max(ordered_counts)))*rank$'rank'/10, pos=3, labels=rank$'rank')

  ratio_labels=lp[ rownames(t(ordered_counts)), 'lr_ratio']
  points(of_bar[3,],ratio_labels, type='o', pch=16, col='red')
  #text(of_bar[3,], ratio_labels$lr_ratio, pos=3, labels=ratio_labels$'lr_ratio')

  
  points(of_bar[3,which(ratio_labels == min(ratio_labels))], ratio_labels[which(ratio_labels == min(ratio_labels))], type='p',pch=16, cex=2, col="red")
  
  text(of_bar[3,which(ratio_labels == min(ratio_labels))], ratio_labels[which(ratio_labels == min(ratio_labels))], 
       pos=3, labels=round(ratio_labels[which(ratio_labels == min(ratio_labels))],2))
  
  
  #points(which(colnames(ordered_counts) == '1q21-2q33'), .01, type='p', col='red', pch=19)
  }

dir = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments"
gen = "PatientOne-aln"

dir = paste(dir, gen, sep="/")
data = read.table(paste(paste(dir, gen, sep="/"), ".stats", sep=""), header=T)

data = data[ order(-data$ppairs), ]
summary(data$ppairs)
sd(data$ppairs)

## what if I drop low pps (so far correlations start to disappear)
#min=max(data$ppairs)-sd(data$ppairs)*3
#d=data[ which(data$ppairs >= min  ),]
#nrow(d)

all.counts = get_counts(data)
all.lengths = get_lengths(data)

# Counts and properly paired ratio correlates, as it should
cor.test(rowSums(all.counts), data$ppairs )

# Counts and band length correlates somewhat which is also reasonable
cor.test(t(all.lengths),t(all.counts))

# Adjust for length and it disappears
adjcounts = t(all.counts/all.lengths)
cor.test(t(all.lengths),adjcounts)
# Still correlates to the pp ratio though -- This should be ok though, I want to find bands that match best and part of that will be finding
# the most matches
cor.test(rowSums(t(adjcounts)), data$ppairs)

lp=band_ratio(data, t(adjcounts))

plot_counts(adjcounts, lp, 2)
#points(which(colnames(ordered_counts) == '1q21-2q33'), .01, type='p', col='red', pch=19)

adjcounts=t(adjcounts)
bps=merge(adjcounts, lp, by="row.names")
rownames(bps) = bps[,1]
bps=bps[,c('bp', 'rank')]

bps$adj=bps$bp*bps$rank
bps=bps=bps[order(-bps$adj),]

bps=bps[ bps$rank >= max(bps$rank), ]
lps=lp[ rownames(bps), ]

lps=lps[order(lps$lr_ratio),]
bps=bps[rownames(lps),]


of_bar=barplot(bps$bp, beside=TRUE, ylim=c(0,max(bps$bp)), 
               col='blue', las=2, cex.names=0.8)
axis(1, of_bar, labels=rownames(bps),  las=2)
points(of_bar, lps$lr_ratio/2, type='o', col='red', pch=19)
text(of_bar, lps$lr_ratio/2, labels=round(lps$lr_ratio, 1), pos=3)


# --------------------------------------------------------------------------- #

## Check the ones from known chromosome pairs in the HCC1954

cb = (strsplit(as.character(data$chr), "-"))
chrs = as.data.frame(matrix(data=NA, nrow=0, ncol=2))
colnames(chrs) = c('chrA', 'chrB')
i = 1
for (c in cb)
  {
  chrs[i,] =  sub("(q|p)[0-9]+", "", c)  
  i=i+1
  }

data = cbind(data, chrs)

paired_chr=table(data$chrA, data$chrB)
pairs=which(paired_chr > 0, arr.ind=T)
pairs=cbind(colnames(paired_chr)[pairs[,'col']], rownames(pairs))

par(mfrow=c(2,2))
for (i in 1:nrow(pairs))
  {
  chrA=pairs[i,1]
  chrB=pairs[i,2]
  dchr=data[which(data$chrA %in% c(chrA, chrB) & data$chrB %in% c(chrA,chrB)),]

  nrow(dchr)
  if (nrow(dchr) > 2)
    {
    counts=get_counts(dchr)
    lengths=get_lengths(dchr)
    lp=band_ratio(dchr, counts/lengths)
    plot_counts(counts/lengths, lp, 0)
    }
  }




