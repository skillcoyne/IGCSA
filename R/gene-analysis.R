rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")


dir = "~/Data"

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t", row.names='Chromosome')

gene_dir = paste(dir, "/VariationNormal/Genes", sep="")

gene_perc = vector("numeric", length(var_files))
gp_names = vector("character", length(var_files))

chrs = c(1:22)#, 'X', 'Y')
gr = as.data.frame(matrix(ncol=5, nrow=length(chrs)))
colnames(gr) = c('TF.Dist2', 'FG.Dist2', 'TF.Extreme', 'FG.Extreme', 'Known.Genes')
for (i in 1:length(chrs))
  {
  c = chrs[i]

  chr = paste('chr', c, sep="")
  
  # gene files
  df = paste(gene_dir, "/dist2/genes/", paste(chr, "-genes-dist2.txt", sep=""), sep="")
  ef = paste(gene_dir, "/extremes/genes/", paste(chr, "-genes-extremes.txt", sep=""), sep="")

  gr[i, 'Known.Genes'] = chr_info[c, 'Confirmed.proteins']
  
  # Dist2
  gene_d = read.table(df, header=T, sep="\t")
  genes = unique(unlist(strsplit(as.character(gene_d$Genes), ","))) # Since one gene may overlap multiple consecutive regions
  gr[i, 'TF.Dist2'] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
  gr[i, 'FG.Dist2'] = length(genes)

  # Extremes
  gene_d = read.table(ef, header=T, sep="\t")
  genes = unique(unlist(strsplit(as.character(gene_d$Genes), ","))) # Since one gene may overlap multiple consecutive regions
  gr[i, 'TF.Extreme'] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
  gr[i, 'FG.Extreme'] = length(genes)
  }
rownames(gr) = chrs
save(gr, file="Genes.Rdata")

# Proportion of genes in entire chromosome identified within the top of the hill region
# Number of fragments matters

# I *think* I should adjust the number of genes by the number of fragments
# And I should definitely adjust by the number of known genes

adjd2 = (gr$FG.Dist2/gr$TF.Dist2)/gr$Known.Genes
adjex = (gr$FG.Extreme/gr$TF.Extreme)/gr$Known.Genes

plot(adjd2, col='blue', xaxt='n', type='o', pch=19, xlab="Chromosomes", ylab="Adjusted genes")
lines(adjex, col='red',type='o', pch=19)
axis(1, at=1:nrow(gr), labels=rownames(gr))
legend('topleft', legend=c("Hill dist", "Extremes"), col=c('blue', 'red'), fill=c('blue', 'red'))

# erm...not sure I actually need to do any tests here.  All I wanted to see was how many genes overlapped the fragments within the
# given distribution of variations on a chromosome.  Problem is that I'm not sure how to really compare them.  I had fewer fragments in the extreme
# regions.  So that needs to be normalized somehow
# extremes are less normal, as expected
ks.test(adjd2, pnorm, mean(adjd2), sd(adjd2)) 
ks.test(adjex, pnorm, mean(adjex), sd(adjex))
