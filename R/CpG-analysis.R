rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")

plot.meth<-function(df, columns, title, leg)
  {
  colors = c(rainbow(length(columns)-1), 'grey')
  
  plot(df[,columns[1]], type='o', pch=19, col=colors[1], xaxt='n', ylab='p.value', xlab='chr')
  lines(df[,columns[2]], type='o', pch=19, col=colors[2])
  lines(cpdf[,columns[3]], type='l', col=colors[3], lwd=2)
  axis(1, at=1:nrow(df), labels=rownames(df))
  title(main=title)
  legend('topleft', legend=leg, col=colors, fill=colors, bty='n', cex=0.5)
  }

## NOTE: Have to redo this with the correct data.  I processed the cpg files badly the first time

dir = "/Volumes/Data/Data"
#dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
#gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

cpg_dir = paste(dir, "/VariationNormal/CpG/1000", sep="")
cpg_files = list.files(path=cpg_dir, pattern="*.txt")


cpdf = data.frame(cbind(chr=c(1:22), CpGI.Mean=0) , row.names='chr' )
nondf = data.frame(cbind(chr=c(1:22), NonCpGI.Mean=0) , row.names='chr' )
for (file in cpg_files)
  {
  chr = sub('-varCpG.txt', "", file)
  chr = sub('chr', "", chr)
  print(file)
  cpg_file = paste(cpg_dir, file, sep="/")
  d = read.table(cpg_file, header=T, sep="\t")
#  if (!exists('all_cpg')) all_cpg = d  else all_cpg = rbind(all_cpg, d)
#  print(nrow(all_cpg))
  
  # Non-cpgI sites have a much lower mean
  cpdf[chr, 'CpGI.Mean'] = mean(d$CpGI.Meth, na.rm=T)
  nondf[chr, 'NonCpGI.Mean'] = mean(d$NonCpGI.Meth, na.rm=T)
  
  ## All methylated sites in CpGI
  
  # There's a big difference between the high/low methylation predicted values for SNV
  # am currently rerunning the cpg integration because I did not integrated all the methylation values
  # as there are predictions in regions that do not have cpg
  low = d[ !is.na(d$CpGI.Meth) & (d$CpGI.Meth < mean(d$CpGI.Meth, na.rm=T)), ]
  ctl = cor.test(low$SNV, low$CpGI.Meth, methods="pearson")
  cpdf[chr, 'Low.SNV-Meth.Pval'] = ctl$p.value
  
  ctl = cor.test(low$CpGI.Meth, low$GCRatio, methods="pearson")
  cpdf[chr, 'Low.Meth-GC.Pval'] = ctl$p.value
  rm(ctl, low)
  
  high = d[ !is.na(d$CpGI.Meth) & (d$CpGI.Meth > mean(d$CpGI.Meth, na.rm=T)), ]
  cth = cor.test(high$SNV, high$CpGI.Meth, methods="pearson")
  cpdf[chr, 'High.SNV-Meth.Pval'] = cth$p.value
  
  cth = cor.test(high$CpGI.Meth, high$GCRatio, methods="pearson")
  cpdf[chr, 'High.Meth-GC.Pval'] = cth$p.value
  rm(cth, high)
  
  ## All methylated sites in nonCpGI
  
  # The non cpgI sites that are methylated are very different from the CpGI sites. 
  # There is a difference again between high and low methylation, but not nearly as big as the difference
  # in CpGI vs non
  low = d[ !is.na(d$NonCpGI.Meth) & (d$NonCpGI.Meth < mean(d$NonCpGI.Meth, na.rm=T)), ]
  ctl = cor.test(low$SNV, low$NonCpGI.Meth, methods="pearson")
  nondf[chr, 'Low.SNV-Meth.Pval'] = ctl$p.value
  
  ctl = cor.test(low$NonCpGI.Meth, low$GCRatio, methods="pearson")
  nondf[chr, 'Low.Meth-GC.Pval'] = ctl$p.value
  rm(ctl, low)
  
  high = d[ !is.na(d$NonCpGI.Meth) & (d$NonCpGI.Meth > mean(d$NonCpGI.Meth, na.rm=T)), ]
  cth = cor.test(high$SNV, high$NonCpGI.Meth, methods="pearson")
  nondf[chr, 'High.SNV-Meth.Pval'] = cth$p.value
  
  cth = cor.test(high$NonCpGI.Meth, high$GCRatio, methods="pearson")
  nondf[chr, 'High.Meth-GC.Pval'] = cth$p.value
  rm(cth, high)
  
  rm(d)
  }

#par(mfrow=c(2,1))
#cpdf = cpdf[ order(cpdf$CpGI.Mean) ,]
#plot.meth(cpdf, c(2,4,1), "SNV to Methylation in CpGI", c('< Mean', '> Mean', 'Mean'))
#plot.meth(cpdf, c(3,5,1), "GC to Methylation in CpGI", c('< Mean', '> Mean', 'Mean'))


#par(mfrow=c(2,1))
#nondf = nondf[ order(nondf$NonCpGI.Mean) ,]
#plot.meth(nondf, c(2,4,1), "SNV to Methylation in NonCpGI", c('< Mean', '> Mean', 'Mean'))
#plot.meth(nondf, c(3,5,1), "GC to Methylation in NonCpGI", c('< Mean', '> Mean', 'Mean'))
#dev.off()

# In the regions with CpG islands there appears to be a connection between correlations of low methylation 
# values with SNV occurance and low pvalue correlations between methylation & GC
cpdf[ cpdf[,2] > cpdf[,4] ,]

# Looking at the  pvaalues showing a correlation between high methylation predictions and SNV occurance
# the GC correlations are all mucch higher
cpdf[ cpdf[,2] < cpdf[,4] ,]

# This does not appear to hold true when looking at the non-CpG island methylation
nondf[ nondf[,2] > nondf[,4] ,]
nondf[ nondf[,2] < nondf[,4] ,]

write.table(cpdf, file="cpg-summary.txt", quote=F, sep="\t")
write.table(nondf, file="noncpg-summary.txt", quote=F, sep="\t")
