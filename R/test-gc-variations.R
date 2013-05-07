#
# Analysis script.
# Compares sequential bins based on either GC content or just chunks of the genome (total bps/10) using t-test to identify best method to break down
# variations for use in genome generation.
#

rm(list=ls())

plot.var.cor<-function(tests, plot=F)
  {
  tests = t(tests)
  
  cors = matrix(nrow=2, ncol=ncol(tests))
  rownames(cors) = c('rho', 'p.value')
  colnames(cors) = colnames(tests)
  cors = as.data.frame(cors)
  
  if (plot) par(mfrow=c(3,3))
  for (var in colnames(tests))
    {
    ct = cor.test(1:nrow(tests), tests[,var], m="s", na.action=na.omit)
    cors['rho', var] = round(ct$estimate, 3)
    cors['p.value', var] = round(ct$p.value, 3)
    if (plot)
      {
      plot(1:nrow(tests),tests[,var], type='l', col='blue', ylab=var,
           main=paste('rho:', round(ct$estimate,3), 'pvalue:', round(ct$p.value,3) ))
      }
    }
  return(cors)
  } 

# Run t.test over each variation in chunks of the *known* genome (all unknowns were filtered out) broken up by bins based on GC content.
test.gc.bins<-function(cg, binsize)
  {
  tests = data.frame()
  last_var = which(colnames(cg) == "GC")-1
  
  size = round(max(cg[,'GC'])/10)
  for(i in 0:9)
    {
    max=i*size; min=max-size; 
    if (min < 0) next
    
    lowRows = cg[,'GC'] >= min & cg[,'GC'] < max
    highRows = cg[,'GC'] >= max & cg[,'GC'] < max+size
    
    rowA = cg[lowRows,]
    rowB = cg[highRows,]
    
    #print(paste(nrow(rowA), nrow(rowB), sep=":"))
    
    for (var in colnames(cg[,1:last_var]))
      {
      tt = t.test(rowA[,var], rowB[,var])
      tests[var, paste(i, i+1, sep="-")] = round(tt$statistic, 3)
      }
    }
  return(tests)
  }

# Run t.test over each variation in chunks of the *known* genome (all unknowns were filtered out) broken up by number of bins.  Entirely regardless
# of the GC content
test.bp.bins<-function(cg, binsize)
  {
  tests = data.frame()
  size = round(nrow(cg)/binsize)
  
  last_var = which(colnames(cg) == "GC")-1
  
  for(i in 0:(binsize-1))
    {
    max=i*size; min=max-size; nextStep=max+size
    if (min < 0) next
    
    if (max >= nrow(cg))  break
    if (nextStep > nrow(cg)) nextStep = nrow(cg)
  
    rowA = cg[min:max,]
    rowB = cg[max:nextStep,]
    
    for (var in colnames(cg[,1:last_var]))
      {
      tt = t.test(rowA[,var], rowB[,var])
      tests[var, paste(i, i+1, sep="-")] = round(tt$statistic, 3)
      }
    }
  return(tests)
  }


setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

ens_dir = "~/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = "~/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

out_dir = "~/Analysis/Normal"

gc_rho = list()
bp_rho = list()
col=T; app=F

for (file in var_files)
  {
  chr = sub(".txt", "", file)
  if (chr == 'chrX' | chr == 'chrY') next
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars; gd = data$gc
  cg = cbind(vd, gd)
  
  bp.test = test.bp.bins(cg, 10) 
  bp.cor = plot.var.cor(bp.test,T)
  bp_rho[[chr]] = bp.cor['rho',]
  
  gc.test = test.gc.bins(cg, 10) 
  
  chr_dir = paste(out_dir, chr, sep="/")
#  png(filename=paste(chr_dir, "gc-bins.png", sep="/"), bg="white", height=900, width=900)
  gc.cor = plot.var.cor(gc.test,T)
#  dev.off()
  gc_rho[[chr]] = gc.cor['rho',]
  
  #if (!exists("all_cg")) all_cg = cg else all_cg = rbind(all_cg,cg)
  #print(nrow(all_cg))

  rm(data,vd,gd,cg)
  col=F; app=T
  }

# sort by GC high -> low
all_cg = all_cg[order(-all_cg[,'GC']),]
all_cg[1:10,] # just double check

gc.tests = test.gc.bins(all_cg, 10)
bp.tests = test.bp.bins(all_cg, 10)

filename=paste(dir, "whole-genome-bins.txt", sep="/")
write("# Ordered by GC high->low, split into bins by total fragments (e.g. 2684800/10) #", file=filename)
write.table(bp.tests, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")

png(filename=paste(dir, "whole-genome-by-bp.png", sep="/"), bg="white", height=900, width=900)
bp.cor = plot.var.cor(bp.tests, plot=T)
dev.off()

write(" # Correlation values by bp bins", file=filename, app=T)
write.table(bp.cor, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")


write("  ", file=filename, app=T)
write("# Split by each 10% GC content low->high (e.g. 908/10) Bin size varies #", file=filename, app=T)
write.table(gc.tests, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")


png(filename=paste(dir, "whole-genome-by-gc.png", sep="/"), bg="white", height=900, width=900)
gc.cor = plot.var.cor(gc.tests, plot=T)
dev.off()

write(" # Correlation values by GC bins", file=filename, app=T)
write.table(gc.cor, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")


#filename = paste(out_dir, 'chr-gcbin-cor.txt', sep="/")
#if (!app) write("# Rho values for correlations from the 10% GC separations per chromosome ", file=filename)
#write.table(rho, file=filename, quote=F, sep="\t", col.names=col, app=T)

#filename = paste(out_dir, 'chr-bpbin-cor.txt', sep="/")
#if (!app) write("# Rho values for correlations from the GC ordered, 10% bp separations per chromosome ", file=filename)
#write.table(rho, file=filename, quote=F, sep="\t", col.names=NA, app=T)



rho = rho[order(rho[,'chr'])]
d[,'chr'] = c(1, 10:19, 2, 20:22, 3:9)
d = d[order(d$chr),]:

par(mfrow=c(3,3))
for (var in colnames(d[,1:7]))
  {
  plot(d[,var], col='blue', type='o', xlab='Chr', xaxt='n', ylab=var, pch=19)
  abline(h=gc.cor['rho',var], col='red', lwd=2)
  axis(1, at=1:nrow(d), labels=d$chr)
  }






