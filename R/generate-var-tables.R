rm(list=ls())

setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

#var_files = c('chr1.txt')
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  if (chr == 'chrX' | chr == 'chrY') next
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  var_data = data$vars
  #gd = data$gc
  #cg = cbind(vd, gd)
  rm(data)

  if (!exists("all_cg")) all_cg = var_data else all_cg = rbind(all_cg, var_data)
  print(nrow(all_cg))
  }


window = round(nrow(all_cg)*.1)
for (i in 0:9)
  {
  min = window*i; max = min+window 
  print( paste(min, max, sep=" : ") )
  if (max == window*10) break
  
  current = all_cg[min:max, ] # whole genome by sequence
  print( nrow(current) )
  
  filename=paste("~/Analysis",  paste('var-', i+1, '.txt', sep=""), sep="/" )
  write.table(current, file=filename, sep="\t", row.names=F, quote=F)
  }




