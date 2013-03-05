rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")

## NOTE: Have to redo this with the correct data.  I processed the cpg files badly the first time

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

gene_dir = paste(dir, "VariationNormal", sep="/")
gene_files = list.files(path=gene_dir, pattern="genes-dist2.txt", recursive=T)  

gene_perc = vector("numeric", length(var_files))
gp_names = vector("character", length(var_files))

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t", row.names='Chromosome')

gene_ratio = matrix(ncol=3, nrow=length(var_files))
colnames(gene_ratio) = c('Total.Fragments', 'Fragment.Genes', 'Known.Genes')
gp_names = vector("character", length(var_files))
#var_files=c("chr3.txt")
for (i in 1:length(var_files))
  {
  var_f = var_files[i]
  
  chr = sub(".txt", "", var_f)
  chrnum = sub('chr', "", chr)
  chrdir = paste(dir, "VariationNormal", chr, sep="/")
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, var_f, sep="/")
  gene_f = paste(chrdir, "genes-dist2.txt", sep="/")
  #data = load.data(gc_f, var_f)
  #var_d = data$vars
  #gc_d = data$gc
  gp_names[i] = chr
  
  gene_d = read.table(gene_f, header=T, sep="\t")
  genes = unique(gene_d$Genes) # Since one gene may overlap multiple consecutive regions

  gene_ratio[i, 'Total.Fragments'] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
  gene_ratio[i, 'Fragment.Genes'] = length(genes)
  gene_ratio[i, 'Known.Genes'] = chr_info[chrnum, 'Confirmed.proteins']
  }
rownames(gene_ratio) = gp_names

#gene_perc = sort(gene_perc)

# Proportion of genes in entire chromosome identified within the top of the hill region
gene_perc = sort( signif(gene_ratio[,2]/gene_ratio[,3], 3)*100 )

bp = barplot(gene_perc, names.arg = names(gene_perc), col='blue', ylim=c(0,100))
#axis(1, at=1:length(gene_perc), labels=names(gene_perc))
text(bp, gene_perc, labels=paste(gene_perc, '%', sep=""), pos=3 )
title(main="Genes in dist peak", ylab="Percentage of all genes")


