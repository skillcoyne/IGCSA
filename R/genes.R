# Script filters the fragments found in high/low/mean variation (SNV) counts to see if these matched with any particular 
# genes.  The gene_analysis.R script compares them.

rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")


#args = commandArgs(trailingOnly = TRUE)
#ens_dir = args[1]
#gc_dir = args[2]
#outdir = args[3]
#low/median/high
frags = "high"

data_dir = "~/Data/VariationNormal"
outdir = paste(data_dir, "Genes", frags, "fragments", sep="/")
if (!file.exists(outdir)) dir.create(outdir, recursive=T)

setwd(data_dir)

ens_dir = paste(data_dir, "Frequencies/1000/Ensembl", sep="/")
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = paste(data_dir, "GC/1000", sep="/")
gc_files = list.files(path=gc_dir,pattern=".txt")  

file="chr10.txt"
for(file in var_files)
  {
  chr = sub(".txt", "", file)
  chrdir = paste(getwd(), chr, sep="/")
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")
  
  data = load.data(gc_f, var_f)
  vd = data$vars; gd = data$gc
  all = cbind(vd, gd)
  
  if (frags == 'high')
    {
    fragments = all[all$SNV > mean(all$SNV) + sd(all$SNV),]
    }
  else if (frags == 'mean')
    {
    fragments = all[all$SNV == median(all$SNV), ]
    }
  else if (frags == 'low')
    {
    fragments = all[all$SNV < 8,]
    }
  print(paste(outdir, paste(chr, "txt", sep="."), sep="/"))
  write.table(fragments, file=paste(outdir, paste(chr, "txt", sep="."), sep="/"), quote=F, sep="\t")
  }





