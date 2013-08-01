rm(list=ls())

source("lib/selection.R")

# --------------------------------------- #

dir = "~/Analysis/Database/cancer"
setwd(dir)

all_bp_d = read.table("all-bp-prob.txt", header=T, sep="\t")
chr_d = read.table("chr_instability_prob.txt", header=F, sep="\t")
colnames(chr_d) = c('chr','probs')

centromeres = read.table("centromeres-probs.txt", header=T, sep="\t")
centromeres = centromeres[order(centromeres[,'bp.prob']),]
centromeres = set.probs(centromeres[,'bp.prob'],centromeres)

cent_bp = select.bp(centromeres[,c('chr','band','bp.prob','p')], s=sample(1:3,1))$bp
cent_bp = cent_bp[which(cent_bp$count > 0), c('chr','band')]
print(cent_bp)

consensus_counts = vector("numeric", 20)
for (i in 1:length(consensus_counts))
{
## -- Test 1, select breakpoints directly from the previously calculated probability -- ##
direct.bp = select.bp(all_bp_d[,c('chr','band','bp.prob')], s=1000) # number of bps per karyotype ranges from 0 - 70, average = 1
direct.bp

## -- Select chromosomes based on previously calculated probability then select breakpoints from within that chromosome, again probability based -- ##
chr.bp = select.chr(chr_d, all_bp_d[,c('chr','band','per.chr.prob')], s=sample(1:12,1))
chr.bp

s = merge(chr.bp$bp, direct.bp$bp, by=c('chr','band'))
top = s[ s$count.x >= mean(s$count.x)+1.5*sd(s$count.x) & s$count.y >= mean(s$count.y)+1.5*sd(s$count.y) ,]
top

consensus_counts[i] = nrow(chr.bp$bp)
}

summary(consensus_counts)

if (exists("bands")) bands = rbind(bands, top)
else bands = top

rm(top, direct.bp, chr.bp, s)
  } 

#counted = unique(bands[,c('chr','band')])
#counted$count = 0
#for (r in 1:nrow(bands))
# {
#  b = which(counted[,'chr'] == bands[r,'chr'] & counted[,'band'] == bands[r,'band'] )
#  counted[ b, 'count'] = counted[b,'count'] + 1
#  }
#counted = counted[order(-counted$count),]

#write.table(counted, row.name=F, sep="\t", quote=F)

#plot(counted$count, type='h', xaxt='n')
#axis(1,at=counted$count, labels=paste(counted$chr, counted$band, sep=""))

#c2 = read.table("consensus-1.txt", header=T)
#plot(c2$count, type='h', xaxt='n')
#axis(1,at=c2$count, labels=paste(c2$chr, c2$band, sep=""))

