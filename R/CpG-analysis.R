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


var_files = c("chr1.txt")
# Variation & gc files
c = '1'
#for (c in c(1:22))
#  {
chr = paste('chr', c, sep="")
chrdir = paste(dir, "VariationNormal", chr, sep="/")
cpg_file = paste(chrdir, "/", chr, "-varCpG.txt", sep="")
  
# forgot to write out column headers apparently
d = read.table(cpg_file, header=F, sep="\t", row.names=1)
colnames(d) = c("SNV", "deletion", "indel", "insertion",  "sequence_alteration", "substitution",  "tandem_repeat",      
                  "GC", "Unk", "BPs",  "UnkRatio",  "GCRatio", "Pred.CpGI",  "Med.Methy.Pred" )
  
nrow(d)

# There's a big difference between the high/low methylation predicted values for SNV
# am currently rerunning the cpg integration because I did not integrated all the methylation values
# as there are predictions in regions that do not have cpg
low = d[d[,14] < mean(d[,14]),]
cor.test(low$SNV, low$Med.Methy.Pred, methods="pearson")
cor.test(low$Med.Methy.Pred, low$GCRatio, methods="pearson")
  

high = d[d[,14] > mean(d[,14]),]
cor.test(high$SNV, high$Med.Methy.Pred, methods="pearson")
cor.test(high$Med.Methy.Pred, high$GCRatio, methods="pearson")

plot(table(high$SNV), col='blue')
lines(table(low$SNV), col='red')
legend('topright', legend=c("high methylation", 'low methylation'), col=c('blue', 'red'), fill=c('blue', 'red'))


#  }