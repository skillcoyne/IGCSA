rm(list=ls())

source("lib/gc_functions.R")

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")


pvalues = as.data.frame(matrix(nrow=0, ncol=3))
colnames(pvalues) = c('p.value', 'sampled.p.value', 'gc')
# Does correlation tests against the two tops of the variation distribution (0 ~15)
# on each chromosome
var_files=c("chr1.txt")
for (i in 1:length(var_files))
  {
  file = var_files[i]
  chr = sub(".txt", "", file)
  chrdir = paste(getwd(), chr, sep="/")
    
  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")
  
  data = load.data(gc_f, var_f)
  vd = data$vars
  gd = data$gc
  
  # So, there's a difference between high & low gc content.  Low GC seems to correlate appears to be 
  # correlated with high high frequency of SNVs in that hill distribution
  # Extreme values still cause problems
  gdvd = cbind(vd, gd)
  
  snvCutoff = 1
  snvCutoffUpper = 29
  snvBump = gdvd[gdvd$SNV > snvCutoff & gdvd$SNV < snvCutoffUpper ,]
  
  q = quantile(gdvd$GCRatio)

  low = gdvd[gdvd$GCRatio <= 0.3,]  
  cor.test(low$SNV, low$GCRatio, methods="pearson")
  range(low$SNV)
  median(low$SNV)
  table(low$SNV)
  
  # High GC *seems* to be more frequent in the fragments with large numbers of SNVs  
  high = gdvd[gdvd$GCRatio >= 0.6,]
  cor.test(high$SNV, high$GCRatio, methods="pearson")
  range(high$SNV)
  median(high$SNV)
  table(high$SNV)
  
  
  # Random pvalue test
  rand1 = sample( gdvd$GCRatio, 5000 )
  rand2 = sample( gdvd$SNV, 5000 )
  t.test(rand1[1:2500], rand1[2501:5000])
  t.test(rand1[1:2500], rand2[1:2500])
  #rt = cor.test(quantile(rand[1:2500]), quantile(rand[2501:5000]), method="pearson" )
  #rt = cor.test(rand[1:2500], rand[2501:5000], method="pearson" )
  #pvalues[chr, 'sampled.p.value'] = format.pval(rt$p.value, digits=3)
  #pvalues[chr, 'gc'] = signif(mean(gd$GCRatio), 3)
  
  }
#pvalues = pvalues[ order(pvalues$'p.value'), ]
#plot(pvalues$sampled.p.value, type='o', col='red', ann=F, xaxt='n', pch=19)
#lines(pvalues$p.value, type='o', col='blue', pch=19)
#lines(pvalues$gc, type='o', col='green', lty=4, lwd=2)
#axis(1, at=(1:nrow(pvalues)), lab=rownames(pvalues)   )
#title(main="GC Ratio pvalues", sub="p.value on bins with count 0, 15", ylab='p value')
#legend("topleft", legend=names(pvalues), fill=c('blue', 'red', 'green') )
#write.table(pvalues, quote=F, sep="\t")




sd(gdvd$GCRatio)

highest = max(gdvd$GCRatio)
high = mean(gdvd$GCRatio) + 2*sd(gdvd$GCRatio)
mean = mean(gdvd$GCRatio)
low = mean(gdvd$GCRatio) - 2*sd(gdvd$GCRatio)


sum(gdvd$GCRatio <= low)
sum(gdvd$GCRatio > low & gdvd$GCRatio <= mean)
sum(gdvd$GCRatio > mean & gdvd$GCRatio <= high)
sum(gdvd$GCRatio > high & gdvd$GCRatio <= highest)


chunk = gdvd[gdvd$GCRatio <= low,]
chunk = gdvd[gdvd$GCRatio > low & gdvd$GCRatio <= mean,]
chunk = gdvd[gdvd$GCRatio > mean & gdvd$GCRatio <= high,]
chunk = gdvd[gdvd$GCRatio > high & gdvd$GCRatio <= highest,]
nrow(chunk)
cor.test(chunk[,6], chunk$GCRatio, m="p")

colnames(chunk[,1:7])



