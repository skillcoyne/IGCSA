rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")


## NOTE: Have to redo this with the correct data.  I processed the cpg files badly the first time

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

#gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
#gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

gene_dir = paste(dir, "VariationNormal", sep="/")
extremes = "genes-extremes.txt"
dist2 = "genes-dist2.txt"

gene_perc = vector("numeric", length(var_files))
gp_names = vector("character", length(var_files))

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t", row.names='Chromosome')

gr = matrix(ncol=5, nrow=length(var_files))
colnames(gr) = c('TF.Dist2', 'FG.Dist2', 'TF.Extreme', 'FG.Extreme', 'Known.Genes')

gp_names = vector("character", length(var_files))

#var_files=c("chr3.txt")
for (i in 1:length(var_files))
  {
  var_f = var_files[i]
  
  chr = sub(".txt", "", var_f)
  chrnum = sub('chr', "", chr)
  chrdir = paste(dir, "VariationNormal", chr, sep="/")
  
  # gene files
  df = paste(chrdir, dist2, sep="/")
  ef = paste(chrdir, extremes, sep="/")

  gp_names[i] = chr
  gr[i, 'Known.Genes'] = chr_info[chrnum, 'Confirmed.proteins']
  
  # Dist2
  gene_d = read.table(df, header=T, sep="\t")
  genes = unique(gene_d$Genes) # Since one gene may overlap multiple consecutive regions
  gr[i, 'TF.Dist2'] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
  gr[i, 'FG.Dist2'] = length(genes)

  # Extremes
  gene_d = read.table(ef, header=T, sep="\t")
  genes = unique(gene_d$Genes) # Since one gene may overlap multiple consecutive regions
  gr[i, 'TF.Extreme'] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
  gr[i, 'FG.Extreme'] = length(genes)
  }
rownames(gr) = gp_names

#gene_perc = sort(gene_perc)

# Proportion of genes in entire chromosome identified within the top of the hill region
# Number of fragments matters
cor.test(gr[,2]/gr[,1], 

gr[,4]/gr[,3]


sort( signif(gr[,2]/gr[,5], 3)*100 )
sort( signif(gr[,4]/gr[,5], 3)*100 )




#bp = barplot(gene_perc, names.arg = names(gene_perc), col='blue', ylim=c(0,100))

#text(bp, gene_perc, labels=paste(gene_perc, '%', sep=""), pos=3 )
#title(main="Genes in dist peak", ylab="Percentage of all genes")

#plot(gr[,2], type='h', col='red', xaxt='n')
#lines(gr[,4], type='h', col='blue')
#axis(1, at=1:nrow(gr), labels=rownames(gr))