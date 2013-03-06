rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")


#args = commandArgs(trailingOnly = TRUE)
#ens_dir = args[1]
#gc_dir = args[2]

data_dir = "~/Data/VariationNormal"
setwd(data_dir)

ens_dir = paste(data_dir, "Frequencies/1000/Ensembl", sep="/")
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = paste(data_dir, "GC/1000", sep="/")
gc_files = list.files(path=gc_dir,pattern=".txt")  


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
  
  extreme_frags = all[all$SNV < 8,]
  med_frags = all[all$SNV == median(all$SNV), ]
  
  write.table(extreme_frags, file=paste(chrdir, "dist-extremes.txt", sep="/") , quote=F, sep="\t")
  #write.table(med_frags, file=paste(chrdir, "dist2.txt", sep="/") , quote=F, sep="\t")
  }





