rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")


dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")


var_files = c("chr1.txt")
# Variation & gc files
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  chrnum = sub("chr", "", chr)
  chrdir = paste(dir, "VariationNormal", chr, sep="/")
  
#  var_file = paste(ens_dir, file, sep="/")
#  gc_file = paste(gc_dir, "/", chr, "-gc.txt", sep="")
  cpg_file = paste(chrdir, "/", chr, "-varCpG.txt", sep="")
 
  all = load.combined.cpg(cpg_file)
  
  
#  data = load.data(gc_file, var_file)
#  gd = data$gc
#  var_d = data$vars
  

  }