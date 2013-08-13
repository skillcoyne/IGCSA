## Generates the chromosome instability scores that were previously determined to be the starting point for karyotype generation ##
## all tests were done in the CancerCytogenetics scripts for bp-analysis and bp-by-band ##
rm(list=ls())

setwd("~/workspace/IGCSA/R/karyotype-database")
source("lib/adj_scores.R")

datadir = "~/Data/sky-cgh/output"
setwd(datadir)

# Load files
chrinfo = read.table("../genomic_info/chromosome_gene_info_2012.txt", header=T, sep="\t", row.name=1)  
bandinfo = read.table("../genomic_info/band_genes.txt", header=T, sep="\t")

bp = read.table("current/noleuk-breakpoints.txt", sep="\t", header=T)
leuk = FALSE

if (leuk)
  {
  bp2 = read.table("current/leuk-breakpoints.txt", sep="\t", header=T)
  cols = c('chr','band','start','end', 'total.karyotypes')
  merged = merge(bp[,cols], bp2[,cols], by=cols[1:4])
  
  merged$total.karyotypes = merged[,'total.karyotypes.x'] + merged[,'total.karyotypes.y']
  merged$total.karyotypes.x = NULL
  merged$total.karyotypes.y = NULL
  bp = merged
  }
bp = bp[-which(bp[,'total.karyotypes'] <= 5),]

c = c(1:22, 'X', 'Y')
## ---- Chromosome Instability ---- ##
chromosome_counts = vector("numeric",length(c))
names(chromosome_counts) = c
for(i in names(chromosome_counts))
  chromosome_counts[i] = sum(bp[bp$chr == i,'total.karyotypes'])

# Obviously correlates to length
cor.test(chromosome_counts,chrinfo[c,"Base.pairs"])

#-- so we adjust the length in a non-linear manner (with a bit of mucking arround found ^.7 gave nearly no correlation) --#
adjusted_scores=chromosome_counts/(chrinfo[c,"Base.pairs"]^0.9)
ks.test(adjusted_scores,pnorm,mean(adjusted_scores),sd(adjusted_scores))
## -- Probabilities are probably the most useful -- ##
chr_probability_list = pnorm(adjusted_scores,mean(adjusted_scores),sd(adjusted_scores))



## Breakpoint per chromosome probability ##
break_info = merge(bp, bandinfo[,c(1,2,5)], by.x=c('chr', 'band'), by.y=c('chr','band'))
break_info$bp.length = break_info$end - break_info$start

bp_adj_scores = norm.scores(break_info, "total.karyotypes")
probability_list = pnorm(bp_adj_scores[,'scores'], mean(bp_adj_scores[,'scores']), sd(bp_adj_scores[,'scores']))
bp_adj_scores[,'bp.prob'] = signif(adjust.to.one(probability_list/sum(probability_list), 5), 5)  ## not using this but keeping it in case I change my mind later
break_info = merge(break_info, bp_adj_scores[,c('chr','band','bp.prob')], by=c('chr','band'))

all_scores = bp.norm.score(break_info, "total.karyotypes")
# scores per breakpoint within each chromosome
for (i in 1:length(all_scores))
  {
  chr = names(all_scores[i])
  pln = pnorm(all_scores[[chr]], mean(all_scores[[chr]]), sd(all_scores[[chr]]))
  pf = as.data.frame( signif(adjust.to.one(pln/sum(pln), 5), 5) )  # prob adjusted to 0-1
  pf$band = row.names(pf)
  
  per_chr = merge(break_info[break_info$chr == chr,], pf, by=c('band'))
  names(per_chr)[length(per_chr)] = 'per.chr.prob'
  if (exists("temp_bk"))  temp_bk = rbind(temp_bk, per_chr) 
  else  temp_bk = per_chr 
  }
break_info = temp_bk
rm(temp_bk)

## Output data files ##
outdir = "~/Analysis/Database/cancer"

filename = paste(outdir, "chr_instability_prob.txt", sep="/")
write("# Normal distribution, probability score per chromosome. Each score is independent of the other chromosomes", file=filename, app=F)
write.table( signif(adjust.to.one(chr_probability_list/sum(chr_probability_list), 5), 5), quote=F, col.names=F, sep="\t", app=T, file=filename)

filename = paste(outdir, "all-bp-prob.txt", sep="/")
write("# Probabilities for each breakpoint across all breakpoints and across breakpoints within each chromosome", file=filename, app=F)
write.table(break_info[,c('chr','band','bp.prob', 'per.chr.prob')], quote=F, row.name=F, sep="\t", app=T, file=filename)





