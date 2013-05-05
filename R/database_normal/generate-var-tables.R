# POST ANALYSIS SCRIPT
# This script generates the database table that provides counts of each variation per 1kb fragment of the genome.


rm(list=ls())

setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = "~/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = "~/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

outdir = "~/Analysis/Database/normal"
if (!file.exists(outdir)) dir.create(outdir, recursive=T)

if (!file.exists(paste(outdir, "variation.txt", sep="/"))) 
  {
  print("Run the variation-probabilities.R script first to generate the variations table.")
  exit(-1)
  }
variations = read.table(paste(outdir, "variation.txt", sep="/"), header=F, row.names=1)


gc_bins = data.frame()
chr_table_names = vector("character")
app=F; cols=T
#file = 'chr10.txt'
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  chrnum = sub("chr", "", chr)
  if (chr == 'chrX' | chr == 'chrY') next
  print(chr)
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars; gd = data$gc
  cg = cbind(vd, gd)

  last_var = which( colnames(cg) == 'GC' ) - 1
  
  binsize=10; chr_vars = c(1:last_var)
  #if (length(chr_table_names) <=0) chr_table_names = c()
  
  size = round(max(cg[,'GC'])/binsize)
  bins = data.frame(chr=rep(chrnum, binsize), bin_id=0, min=0, max=0, total_fragments=0)  
  chr_rows = data.frame()
  for(i in 1:binsize)
    {
    max=i*size; min=max-size; 
    
    rows = cg[ which(cg[,'GC'] >= min & cg[,'GC'] < max) , chr_vars]
    bins[i, 'bin_id'] = i; bins[i, 'total_fragments'] = nrow(rows)
    bins[i, 'min'] = min; bins[i, 'max'] = max
    rows[,'bin_id'] = i; rows[, 'chr'] = chrnum
    # reorder the rows for output: chr, bin_id, [variations]
    #rows = rows[, c(length(rows), length(rows)-1,chr_vars) ] 
    for (var in colnames(rows[,chr_vars]))
      {
      var_id = which(variations == var)
      chr_rows = rbind(chr_rows, cbind(rows[,c(length(rows), length(rows)-1)], rep(var_id, nrow(rows)), rows[, var] ) )
      }
    }
  colnames(chr_rows) = c('chr', 'bin_id', 'variation_id', 'count')
  gc_bins = rbind(gc_bins, bins)

  filename = paste(chr, "txt", sep=".")
  
  #rownames(chr_rows) = c(1:nrow(chr_rows)) # reset rownames in order to use them as table indecies in the output
  write.table(chr_rows, file=paste(outdir, 'variation_per_bin.txt', sep="/"), sep="\t", row.names=F, quote=F, col.names=cols, append=app)
  
  rm(data,vd,gd,cg)
  app=T; cols=F
  }
#colnames(gc_bins) = c('id', 'chr', 'bin_id', 'min', 'max', 'total_fragments')
write.table(gc_bins, file=paste(outdir, "gc_bins.txt", sep="/"), quote=F, row.name=T, col.names=NA, sep="\t")

