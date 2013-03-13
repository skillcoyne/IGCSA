rm(list=ls())

t.test.gc<-function(cg, cols, ranges)
  {
  if (ranges[1] > 0) ranges = rev(round(ranges))
  #print(ranges)
  tests = data.frame()
  for (i in 1:length(ranges))
    {
    if (i == length(ranges)) break
    min = ranges[i]; max = ranges[i+1]
    rows = cg$GC > min & cg$GC <= max
    
    for (var in colnames(cg[,1:cols]))
      {
      tt = t.test(cg[rows,var])
      print(tt)
      tests[i, paste(var,'t', sep='.')] = round(tt$statistic, 2)
      }
    tests[i, 'min.GC'] = min
    tests[i, 'max.GC'] = max
    tests[i, 'n.rows'] = nrow(cg[rows,])
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

filename = paste(dir, "gc.ttests.txt", sep="/")
if (file.exists(filename)) file.remove(filename)

var_files = c('chr1.txt')
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars
  gd = data$gc
  cg = cbind(vd, gd)
  rm(data,vd,gd)

  summary(cg[,'GC'])
  ranges1 = c(
    #max(cg[,'GC']),
    mean(cg[,'GC']) + 2*sd(cg[,'GC']),
    mean(cg[,'GC']) + sd(cg[,'GC']),
    mean(cg[,'GC']),
    mean(cg[,'GC']) - sd(cg[,'GC']),
    mean(cg[,'GC']) - 2*sd(cg[,'GC'])
    #0    
    )
  
  ranges2 = c(
    #max(cg[,'GC']),
    mean(cg[,'GC']) + 2*sd(cg[,'GC']),
    mean(cg[,'GC']),
    mean(cg[,'GC']) - 2*sd(cg[,'GC'])
    #0
    )
  
  ranges3 = c(
    #max(cg[,'GC']),
    mean(cg[,'GC']) + 2*sd(cg[,'GC']),
    mean(cg[,'GC']),
    mean(cg[,'GC']) - sd(cg[,'GC'])
    #0
  )

  ranges4 = c(
    #max(cg[,'GC']),
    mean(cg[,'GC']) + 3*sd(cg[,'GC']),
    mean(cg[,'GC']) + 2*sd(cg[,'GC']),
    mean(cg[,'GC'])
    #0
  )

  
  
  #ranges=rev(round(ranges1))
  
  ranges = round(ranges1)
  
  #print(ranges)
  tests = data.frame()
  for (i in 1:length(ranges))
    {
    #min = ranges[i]; max = ranges[i+1]
    #rows = cg$GC > min & cg$GC <= max
    if (i == length(ranges)) break
    highRows = cg[,'GC'] >= ranges[i]
    lowRows = cg[,'GC'] <= ranges[length(ranges)]

    var = 'deletion'
    
print( paste(ranges[i], ranges[length(ranges)], sep=":") )
  t1 = t.test( cg[highRows, var], cg[!(highRows+lowRows), var] ) 
    t2 = t.test( cg[highRows, var], cg[lowRows, var] ) 
    
    
    tests[i, 't1'] = t1$statistic
    tests[i, 't2'] = t2$statistic
    
    #for (var in colnames(cg[,1:7]))
    #  {
    #  tt = t.test(cg[rows,var], cg[!(rows), var])
    #  tests[i, paste(var,'t', sep='.')] = round(tt$statistic, 2)
    #  }
    #tests[i, 'min.GC'] = min
    #tests[i, 'max.GC'] = max
    #tests[i, 'range'] = ranges[i]
    #tests[i, 'n.rows'] = nrow(cg[rows,])
    }
  print(tests)
  
  
  varcols = 7
  if (chr == 'chrY') varcols = 5
  
  write(paste("###", chr, "###"), file=filename, app=T)
  
  tests = t.test.gc(cg, varcols, ranges1)
  write.table(tests, file=filename, app=T, col.name=T, row.name=F, quote=F)
  write(" ", file=filename, app=T)
  
  tests = t.test.gc(cg, varcols, ranges2)
  write.table(tests, file=filename, app=T, col.name=T, row.name=F, quote=F)
  write(" ", file=filename, app=T)
  
  tests = t.test.gc(cg, varcols, ranges3)
  write.table(tests, file=filename, app=T, col.name=T, row.name=F, quote=F)
  write(" ", file=filename, app=T)
  
  tests = t.test.gc(cg, varcols, ranges4)
  write.table(tests, file=filename, app=T, col.name=T, row.name=F, quote=F)
  write(" ", file=filename, app=T)
  
  # none in Y chromosome
  tests = data.frame()
  x = round(nrow(cg)/4)
  for (i in 1:4)
    {
    max = x*i; min = x*(i-1)
    print(paste(min, max, sep=":"))
    tt = t.test(cg[min:max,'tandem_repeat'])
    tests[i, 'tandem_repeat.t'] = round(tt$statistic, 2)
    tests[i, 'n.rows'] = x
    }
  
  write.table(tests, file=filename, app=T, col.name=T, row.name=F, quote=F)
  write(" ", file=filename, app=T)
  
  }
