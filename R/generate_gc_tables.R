rm(list=ls())

setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

q = read.table(paste(gc_dir, 'gc-quintiles.txt', sep="/"), sep='\t', header=T)
q = as.vector(q)

tables_dir = paste(dir, "/VariationNormal", sep="")

highest_f = paste(tables_dir, 'highest-gc.txt', sep='/')
write(paste("## GC >", signif(q$high, 4), "GC <=", signif(q$highest, 4), "##"), file=highest_f)

high_f = paste(tables_dir, 'high-gc.txt', sep='/')
write(paste("## GC >", signif(q$mean, 4), "GC <=", signif(q$high, 4), "##"), file=high_f)

mid_f = paste(tables_dir, 'mid-gc.txt', sep='/')
write(paste("## GC >", signif(q$low, 4), "GC <=", signif(q$mean, 4), "##"), file=mid_f)

low_f = paste(tables_dir, 'low-gc.txt', sep='/')
write(paste("## GC >", signif(q$lowest, 4), "GC <=", signif(q$low, 4), "##"), file=low_f)

lowest_f = paste(tables_dir, 'lowest-gc.txt', sep='/')
write(paste("## GC <", signif(q$lowest, 4), "##"), file=lowest_f)

rm(all_gc)
col=T; rows=F
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  chrdir = paste(getwd(), chr, sep="/")
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")
  
  data = load.data(gc_f, var_f)
  vd = data$vars
  gd = data$gc

  all = cbind(vd,gd)
  
  
  highest = all[ all[,'GCRatio'] > q$high & all[,'GCRatio'] <= q$highest ,] 
  high = all[ all[,'GCRatio'] > q$mean & all[,'GCRatio'] <= q$high ,] 
  mid = all[ all[,'GCRatio'] > q$low & all[,'GCRatio'] <= q$mean ,] 
  low = all[ all[,'GCRatio'] > q$lowest & all[,'GCRatio'] <= q$low ,] 
  lowest = all[ all[,'GCRatio'] < q$lowest ,] 

  
  write.table(highest, quote=F, sep="\t", row.names=rows, append=T, col.names=col, file=highest_f)
  write.table(high,    quote=F, sep="\t", row.names=rows, append=T, col.names=col, file=high_f)
  write.table(mid,     quote=F, sep="\t", row.names=rows, append=T, col.names=col, file=mid_f)
  write.table(low,     quote=F, sep="\t", row.names=rows, append=T, col.names=col, file=low_f)
  write.table(lowest,  quote=F, sep="\t", row.names=rows, append=T, col.names=col, file=lowest_f)
  
  if (!exists('all_gc')) all_gc = gd$GCRatio else all_gc = unlist(append(all_gc, gd$GCRatio))
  col = F
  }

sd(all_gc)

quin = vector("numeric", length=5)
names(quin) = c('highest', 'high', 'mean', 'low', 'lowest')

quin['highest'] = max(all_gc)
quin['high'] = mean(all_gc) + 3*sd(all_gc)
quin['mean'] = mean(all_gc) + 2*sd(all_gc)
quin['low'] = mean(all_gc)
quin['lowest'] = mean(all_gc) - 2*sd(all_gc)

length(all_gc)

length( all_gc[ all_gc > high & all_gc <= highest ] )
length( all_gc[ all_gc > mean & all_gc <= high ] )
length( all_gc[ all_gc > low & all_gc <= mean ] )
length( all_gc[ all_gc > lowest & all_gc <= low ] )
length( all_gc[ all_gc < lowest ] ) 

#write.table(t(quin), quote=F, sep="\t", row.names=F, file=paste(gc_dir, 'gc-quintiles.txt', sep="/"))
              
              