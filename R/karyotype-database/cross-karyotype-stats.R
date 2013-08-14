## See CancerCytogenetics karyotype-stats for tests

rm(list=ls())

norm.ks<-function(t)
  {
  ks = ks.test(t, pnorm, mean(t), sd(t))
  return(ks)
  }

pois.ks<-function(t)
  {
  ks = ks.test(t, ppois, mean(t))
  return(ks)
  }
  
probs.in.ranges<-function(ranges, counts, decimals=5, total_counts)
  {
  rframe = as.data.frame(matrix( rep(0, length(ranges)), ncol=1, nrow=length(ranges), dimnames=list(sapply( ranges, function(s) paste(s[1], s[2], sep="-") ), c("count")) ))
  for (n in 1:length(ranges))
    {
    x = unlist(ranges[n])
    rframe[ paste(x[1], x[2], sep="-"), 1] =  sum(counts[ which( as.numeric(names(counts)) >= x[1] & as.numeric(names(counts)) <= x[2]  ) ])
    }
  rframe$prob = round(rframe[,'count']/total_counts, decimals)
  rframe$prob = round(rframe[,'prob']/sum(rframe[,'prob']), decimals)
  
  if (sum(rframe$prob) < 1)
    rframe$prob[rframe$prob == min(rframe$prob)] = signif(rframe$prob[rframe$prob == min(rframe$prob)] + (1-sum(rframe$prob)), decimals)
  if (sum(rframe$prob) > 1)
    rframe$prob[rframe$prob == min(rframe$prob)] = signif(rframe$prob[rframe$prob == min(rframe$prob)] - (sum(rframe$prob)-1), decimals)
  
  rframe = rframe[order(-rframe$prob),]
  return(rframe)
  }


setwd("~/workspace/IGCSA/R/karyotype-database")
source("lib/adj_scores.R")

datadir = "~/Data/sky-cgh/output"
setwd(datadir)
outdir = "~/Analysis/Database/cancer"

abrs = read.table("current/abr_per_kt.txt", header=T, sep="\t", row.names=1)
total_karyotypes = nrow(abrs)

bpkt = read.table("current/bp_per_kt.txt", sep="\t")
colnames(bpkt) = c('bps','kt.count') 
# Don't much care about those that have no breakpoints
bpkt = bpkt[bpkt[,'bps'] > 0, ]
bpkt = bpkt[order(bpkt[,1]),]

plot((bpkt$kt.count), type='o', col='blue', xlab="Number of bps", ylab="(Karyotype Count)")

# use simple probabilitiy as none of the tests showed anything
bpkt$prob = round( bpkt[,2]/total_karyotypes, 5)
bpkt = bpkt[ order(bpkt$prob), ]

# group them for more continuous probabilities
## bp per karyotype is not used nor useful at the moment
bp_tbl = bpkt[,'kt.count']
names(bp_tbl) = bpkt[,'bps']
bp_probs = probs.in.ranges( list(c(1,5), c(6,10), c(11,20), c(21,100)), bp_tbl, 5, total_karyotypes )
bp_probs$type = "breakpoint"

## Aberrations 
# same with aberrations
abr_probs = probs.in.ranges(list(c(0,2), c(3,7), c(8,14), c(15,20), c(21,55)), table(abrs[,'aberrations.count']), 5, total_karyotypes)
abr_probs$type = "aberration"

## Aneuploidy per karyotype
pdy_probs = probs.in.ranges(list(c(0,3), c(4,10), c(11,20), c(21,35)), table(abrs[,'aneuploidy.count']), 5, total_karyotypes)
pdy_probs$type = "aneuploidy" 

## Number of chromosomes involved in breaks
chr_probs = probs.in.ranges(list( c(0,2), c(3,6), c(7,10), c(11,21) ), table(abrs['chrs.count']), 5, total_karyotypes)
chr_probs$type = "chromosome"

filename = paste(outdir, "karyotype-probs.txt", sep="/")
write("## General karyotype probabilities given for counts in ranges", file=filename, app=F)
write.table(t(c("range", "prob", "type")), sep="\t", row.name=F, col.name=F, quote=F, app=T, file=filename)
for (pf in list(pdy_probs, chr_probs, abr_probs))
  {
  pf = format(pf, scientific=F, trim=T)
  write.table(cbind(rownames(pf), pf[,c('prob', 'type')]), row.name=F,col.names=F, quote=F, sep="\t", app=T, file=filename)
  }

