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
var_files=c("chr6.txt")
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
  
  gdvd = cbind(vd, gd)
  gdvd = gdvd[gdvd$GCRatio <= 0.3,]  # low GC seems to correlate with high SNV
  cor.test(gdvd$SNV, gdvd$GCRatio, methods="pearson")
  #bins1 = which(vd$SNV == 0 )
  #r1 = gd$GCRatio[bins1]
  #bins2 = which(vd$SNV == 19 )
  #r2 = gd$GCRatio[bins2]
  #t.test(r1, r2)  
  
  #par(mfrow=c(1,2))
  #hist(r1, main="1kb = 0")
  #hist(r2, main="1kb = 19")
  
  ## TODO: Look at CpG islands do means/bins t.tests, this *could* be GC content but isn't really significant
  
  #ct = corrGC(gd, vd, var="SNV", var.counts=c(16, 15), method="pearson")
  #if (!is.na(ct)) pvalues[chr, 'p.value'] = format.pval(ct$p.value, digits=3)
  
  # Random pvalue test
  #rand = sample( gd$GCRatio, 5000 )
  #t.test(rand[1:2500], rand[2501:5000])
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

