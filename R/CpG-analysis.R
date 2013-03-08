rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")

## NOTE: Have to redo this with the correct data.  I processed the cpg files badly the first time

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
#gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

cpg_dir = paste(dir, "/VariationNormal/CpG/1000", sep="")
cpg_files = list.files(path=cpg_dir, pattern="*.txt")

# Variation & gc files
rm(all_cpg)
for (file in cpg_files)
  {
  chr = sub('-varCpG.txt', "", file)
  print(file)
  cpg_file = paste(cpg_dir, file, sep="/")
  d = read.table(cpg_file, header=T, sep="\t")
  print(colnames(d))
#  if (!exists('all_cpg')) all_cpg = d  else all_cpg = rbind(all_cpg, d)
#  print(nrow(all_cpg))
#  rm(d)
  }

cpg_file = paste(cpg_dir, "/", chr, "-varCpG.txt", sep="")
  
# forgot to write out column headers apparently
d = read.table(cpg_file, header=T, sep="\t")
#colnames(d) = c("SNV", "deletion", "indel", "insertion",  "sequence_alteration", "substitution",  "tandem_repeat", "GC", "Unk", "BPs",  "UnkRatio",  "GCRatio", "Pred.CpGI",  "Med.Methy.Pred" )

# Non-cpgI sites have a much lower mean
mean(d$CpGI.Meth, na.rm=T)
mean(d$NonCpGI.Meth, na.rm=T)

## All methylated sites in CpGI

# There's a big difference between the high/low methylation predicted values for SNV
# am currently rerunning the cpg integration because I did not integrated all the methylation values
# as there are predictions in regions that do not have cpg
low = d[ !is.na(d$CpGI.Meth) & (d$CpGI.Meth < mean(d$CpGI.Meth, na.rm=T)), ]
cor.test(low$SNV, low$CpGI.Meth, methods="pearson")
cor.test(low$CpGI.Meth, low$GCRatio, methods="pearson")

high = d[ !is.na(d$CpGI.Meth) & (d$CpGI.Meth > mean(d$CpGI.Meth, na.rm=T)), ]
cor.test(high$SNV, high$CpGI.Meth, methods="pearson")
cor.test(high$CpGI.Meth, high$GCRatio, methods="pearson")


## All methylated sites in nonCpGI

# The non cpgI sites that are methylated are very different from the CpGI sites. 
# There is a difference again between high and low methylation, but not nearly as big as the difference
# in CpGI vs non
low = d[ !is.na(d$NonCpGI.Meth) & (d$NonCpGI.Meth < mean(d$NonCpGI.Meth, na.rm=T)), ]
cor.test(low$SNV, low$NonCpGI.Meth, methods="pearson")
cor.test(low$NonCpGI.Meth, low$GCRatio, methods="pearson")

high = d[ !is.na(d$NonCpGI.Meth) & (d$NonCpGI.Meth > mean(d$NonCpGI.Meth, na.rm=T)), ]
cor.test(high$SNV, high$NonCpGI.Meth, methods="pearson")
cor.test(high$NonCpGI.Meth, high$GCRatio, methods="pearson")



#  }