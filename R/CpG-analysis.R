rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")


dir = "/Volumes/Data/Data"

ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir,pattern=".txt")  

cpg_dir = paste(dir, "HDMFPred", sep="/")
cpg_files = list.files(path=gc_dir,pattern=".txt")  

var_files=c("chr10.txt")
for (i in 1:length(var_files))
  {
  file = var_files[i]
  chr = sub(".txt", "", file)
  chrdir = paste(getwd(), chr, sep="/")
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  cpg_f = paste(cpg_dir, file, sep="/")
  var_f = paste(ens_dir, file, sep="/")
  
  data = load.data(gc_f, var_f)
  var_data = cbind(data$vars, data$gc)
  cpgd = load.cpg(cpg_f)
  
  for (fragE in rownames(var_data))
    {
    fragE = as.numeric(fragE)
    fragS = fragE-1000
    print(paste(fragS, fragE, sep="-"))
    islands = cpgd[ cpgd$RangeS >= fragS & cpgd$RangeE <= fragE,  ]
    if (nrow(islands) > 0) 
      { 
      print(islands)
      var_data[fragE, 'Pred.CpGI'] = nrow(islands)
      var_data[fragE, 'Med.Methy.Pred'] = median(islands$Meth.Prob)
      }
    }
  
  chrdir = paste(dir, "VariationNormal", chr, sep="/")
  if (!file.exists(chrdir)) dir.create(chrdir)
  save(var_data, file=paste(chrdir, chr, ".RData", sep=""))
  }