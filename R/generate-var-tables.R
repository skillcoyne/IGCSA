rm(list=ls())

bin.gc<-function(cg, variations=c(1:7), binsize=10)
  {
  size = round(max(cg[,'GC'])/binsize)
  for(i in 0:(binsize-1))
    {
    max=i*size; min=max-size; 
    if (min < 0) next
  
    rows = cg[ which(cg[,'GC'] >= min & cg[,'GC'] < max) , variations]
  
    }
  }

setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

outdir = "~/Analysis/Database"
if (!file.exists(outdir)) dir.create(outdir, recursive=T)

bins = data.frame()
colnames(bins) = c(10,20,30,40,50,60,70,80,90,100)

#var_files = c('chr1.txt')
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  chrnum = sub("chr", "", chr)
  #if (chr == 'chrX' | chr == 'chrY') next
  print(chr)
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars; gd = data$gc
  cg = cbind(vd, gd)
  
  chrdir = paste(outdir,chr,sep="/")
  if (file.exists(chrdir)) unlink(chrdir, recursive=T) 
  dir.create(chrdir)
  
  binsize=10; variations = c(1:7)
  size = round(max(cg[,'GC'])/binsize)
  for(i in 1:binsize)
    {
    max=i*size; min=max-size; 

    bins[chrnum, as.character(i*10)] = paste(min,max,sep="-")    
    rows = cg[ which(cg[,'GC'] >= min & cg[,'GC'] < max) , variations]
  
    filename = paste(chr, as.character(i*10), "txt", sep=".")
    write.table(rows, file=paste(chrdir, filename, sep="/"), sep="\t", col.names=T, row.names=F, quote=F)
    }
  
  #if (!exists("all_cg")) all_cg = var_data else all_cg = rbind(all_cg, var_data)
  #print(nrow(all_cg))
  rm(data,vd,gd,cg)
  }

bins[,'chr'] = rownames(bins)
bins = bins[,c(11, 1:10)]
write.table(bins, file=paste(outdir, "GC-bins.props", sep="/"), quote=F, row.name=F, sep="\t")
