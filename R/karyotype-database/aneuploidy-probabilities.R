## This sets up the basic probability of each chromosome either being deleted, or gained. ##
# All of the tests were done in CancerCytogenetics ploidy.R

rm(list=ls())


simple.prob<-function(d, total)
  {
  d = d[ order( d$chromosome, decreasing=T ), ]
  
  d$gain.prob = d[,'gain']/d[,'karyotypes']
  d$loss.prob = d[,'loss']/d[,'karyotypes']
  
  for (r in 1:nrow(d))
    d[r,c('gain.prob','loss.prob')] = adjust.to.one(d[r,c('gain.prob', 'loss.prob')])
  
  overall =  d$karyotypes/total
  d$prob = adjust.to.one(overall/sum(overall))
  
  return(d[ order(-d$prob), ])
  }

setwd("~/workspace/IGCSA/R/karyotype-database")
source("lib/adj_scores.R")

datadir = "~/Data/sky-cgh/output"
setwd(datadir)

total_karyotypes = 100240
## Ploidy changes ##
pdy = read.table("current/noleuk-ploidy.txt", header=T, sep="\t")
lpdy = read.table("current/leuk-ploidy.txt", header=T, sep="\t")

all = pdy
all$gain = all$gain + lpdy$gain
all$loss = all$loss + lpdy$loss
all$karyotypes = all$karyotypes + lpdy$karyotypes


chrinfo = read.table("../genomic_info/chromosome_gene_info_2012.txt", header=T, sep="\t")
chrinfo = chrinfo[ -which(chrinfo[,'Chromosome'] == 'mtDNA'), ]

ploidy_info = merge(all, chrinfo[,c(1,3,5)], by.x=c('chromosome'), by.y=c('Chromosome'))

all = simple.prob(all, total_karyotypes)

# non leukemia
pdy = simple.prob(pdy)
# leukemia
lpdy = simple.prob(lpdy)

plot(all[,'prob'], xaxt='n')
axis(1, at=1:nrow(all), label=all$chromosome)

setwd("~/Analysis/Database/cancer")
write.table( all[,c('chromosome','prob', 'gain.prob', 'loss.prob')], quote=F, sep="\t", row.name=F, file="aneuploidy-probs.txt" )



