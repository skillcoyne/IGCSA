rm(list=ls())

t.test.gc<-function(cg, comb, r, cols)
  {
  tests = data.frame()
  for (i in 1:(length(comb)/2))
    {
    curr = comb[,i]
    
    nameHigh = names(r[r == curr[1]])
    nameLow = names(r[r == curr[2]] )
    
    highRow = cg[,'GC'] >= curr[1]
    lowRow =  cg[,'GC'] >= curr[2]
    
    for (var in colnames(cg[,1:cols]))
      {    
      tt = t.test(cg[highRow, var], cg[lowRow, var]  )
      tests[var, paste(nameHigh, nameLow, sep=",")] = round(tt$statistic, 3)
      }
    }
  return(tests)
  }
  
  


setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

highFileName = paste(dir, "gc-high-ttests.txt", sep="/")
if (file.exists(highFileName)) file.remove(highFileName)

lowFileName = paste(dir, "gc-low-ttests.txt", sep="/")
if (file.exists(lowFileName)) file.remove(lowFileName)

col=T
#var_files = c('chr1.txt')
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  if (chr == 'chrX' | chr == 'chrY') next
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars
  gd = data$gc
  cg = cbind(vd, gd)
  rm(data,vd,gd)

  if (!exists("all_cg")) all_cg = cg else all_cg = rbind(all_cg,cg)
  print(nrow(all_cg))
  
  summary(cg[,'GC'])
  
  rnames = c("max", "+3sd", "+2sd", "+1.5sd", "+1sd", "mean", "-1sd", "-1.5sd", "-2sd")
  ranges = vector("numeric", length=length(rnames))
  names(ranges) = rnames
  ranges[1] = max(cg[,'GC'])
  ranges[2] = mean(cg[,'GC']) + 3*sd(cg[,'GC'])
  ranges[3] = mean(cg[,'GC']) + 2*sd(cg[,'GC'])
  ranges[4] = mean(cg[,'GC']) + 1.5*sd(cg[,'GC'])
  ranges[5] = mean(cg[,'GC']) + sd(cg[,'GC'])
  ranges[6] = mean(cg[,'GC'])
  ranges[7] = mean(cg[,'GC']) - sd(cg[,'GC'])
  ranges[8] = mean(cg[,'GC']) - 1.5*sd(cg[,'GC'])
  ranges[9] = mean(cg[,'GC']) - 2*sd(cg[,'GC'])
  ranges=round(ranges)
  
  high = combn(ranges[2:6], 2)
  low = combn(ranges[6:9], 2)
  
  varcols = 7
  if (chr == 'chrY') varcols = 5
  
  highTests = t.test.gc(cg, high, ranges, varcols)
  lowTests = t.test.gc(cg, low, ranges, varcols)
  
  #window = round((max(cg[,'GC'])*.1)+0.5)
  #for(i in 0:9)
  #  {
  #  min = window*i; max = window*(i+1)
  #  rowA = cg[min:max,'SNV']
  #  rowB = cg[max:(max+window), 'SNV']
  #  if (max >= max(cg[,'GC'])) break
  #  tt = t.test(rowA, rowB)
  #  tt$data.name = paste()
  #  print(tt)
  #  }
  
  
  write(paste("###", chr, "###"), file=highFileName, app=T)
  write.table(highTests, file=highFileName, app=T, col.name=col, row.name=T, quote=T, sep="\t")
  write(" ", file=highFileName, app=T)
  
  write(paste("###", chr, "###"), file=lowFileName, app=T)
  write.table(lowTests, file=lowFileName, app=T, col.name=col, row.name=T, quote=T, sep="\t")
  write(" ", file=lowFileName, app=T)
  
  col=F
  }

rnames = c("max", "+3sd", "+2sd", "+1.5sd", "+1sd", "mean", "-1sd", "-1.5sd", "-2sd")
ranges = vector("numeric", length=length(rnames))
names(ranges) = rnames
ranges[1] = max(cg[,'GC'])
ranges[2] = mean(cg[,'GC']) + 3*sd(cg[,'GC'])
ranges[3] = mean(cg[,'GC']) + 2*sd(cg[,'GC'])
ranges[4] = mean(cg[,'GC']) + 1.5*sd(cg[,'GC'])
ranges[5] = mean(cg[,'GC']) + sd(cg[,'GC'])
ranges[6] = mean(cg[,'GC'])
ranges[7] = mean(cg[,'GC']) - sd(cg[,'GC'])
ranges[8] = mean(cg[,'GC']) - 1.5*sd(cg[,'GC'])
ranges[9] = mean(cg[,'GC']) - 2*sd(cg[,'GC'])
ranges=round(ranges)

highTests = t.test.gc(cg, high, ranges, 7)
lowTests = t.test.gc(cg, low, ranges, 7)

allFileName = paste(dir, "gc-ttests-WholeGenome.txt", sep="/")
write(paste("###", "All HIGH", "###"), file=allFileName, app=F)
write.table(highTests, file=allFileName, app=T, col.name=T, row.name=T, quote=T, sep="\t")
write("  ", file=allFileName, app=T)

write(paste("###", "All LOW", "###"), file=allFileName, app=T)
write.table(lowTests, file=allFileName, app=T, col.name=T, row.name=T, quote=T, sep="\t")

tests = data.frame()
window = round(max(all_cg$GC)*.1)
#window = round(nrow(all_cg)*.1)
for (i in 0:9)
  {
  min = window*i; max = min+window 
  print( paste(min, max, sep=" : ") )
  if (max == window*10) break

  rowA = all_cg[,'GC'] >= min & all_cg[,'GC'] < max
  rowB = all_cg[,'GC'] >= max & all_cg[,'GC'] < (max+window)
  
  for (var in colnames(all_cg[,1:7]))
    {
    #rowA = all_cg[min:max, var] # whole genome by sequence
    #rowB = all_cg[max:(max+window), var]
    #tt = t.test(rowA, rowB)
    
    tt = t.test(all_cg[rowA, var], all_cg[rowB,var])
    
    tests[var, paste(min,max,sep=":")] = round(tt$statistic, 3)
    }
  }

write.table(tests, col.name=T, row.name=T, quote=F, sep="\t")




