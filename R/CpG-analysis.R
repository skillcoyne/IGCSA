rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")

binomial.var<-function(cpg, var)
  {
  binomdf = data.frame()
  
  N = nrow(cpg)  # Total fragments
  R = nrow(cpg[cpg$CpGI.Meth > 0 & !is.na(cpg$CpGI.Meth),]) # Total fragments with methylation 
  
  if (N <= 0 | R <= 0) stop("There are no fragments or no fragments with methylation in the given data.") 
  
  # those with the variation
  variation = cpg[cpg[[var]] > 0,] 
  n = nrow(variation) 
  
  if (n <= 0) stop(paste("No", var, " variation found in the given data.") )
  
  # fragments with variation that show methylation, needs to use a cutoff most likely
  q = quantile(cpg$CpGI.Meth, na.rm=T) # could be good for cutoffs
  
  # each quantile of the CpGI predicted methylation
  for (i in 1:length(q)-1) # 100% doesn't matter for anything
    {
    meth = cpg[cpg[[var]] > 0 & (cpg$CpGI.Meth > q[i] & !is.na(cpg$CpGI.Meth)) ,]
    r = nrow(meth)   
    # prob of the variation being methylated?
    p = r/(N-n)
    # probability
    binomdf[ var, names(q[i]) ] = signif( pbinom(r, size=N, prob=p), 4 )
    test = binom.test(r,N,p)
    binomdf[var, 'Exact.Binomial'] = test$p.value
    }
  
  return(binomdf)  
  }

dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
#gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

cpg_dir = paste(dir, "/VariationNormal/CpG/1000", sep="")
cpg_files = list.files(path=cpg_dir, pattern="*.txt")


cpdf = data.frame(cbind(chr=c(1:22)) , row.names='chr' )
nondf = data.frame(cbind(chr=c(1:22)) , row.names='chr' )
app = F
for (file in cpg_files)
  {
  chr = sub('-varCpG.txt', "", file)
  chr = sub('chr', "", chr)
  print(file)
  cpg_file = paste(cpg_dir, file, sep="/")
  d = read.table(cpg_file, header=T, sep="\t")
  d$Pred.CpGI[ which(is.na(d$CpGI.Meth)) ] = NA  
  
#  if (!exists('all_cpg')) all_cpg = d  else all_cpg = rbind(all_cpg, d)
#  print(nrow(all_cpg))
  
  # Non-cpgI sites have a much lower mean
  #cpdf[chr, 'CpGI.Mean'] = mean(d$CpGI.Meth, na.rm=T)
  #nondf[chr, 'NonCpGI.Mean'] = mean(d$NonCpGI.Meth, na.rm=T)
  
  ## Binomial to find liklihood of a specific variation being methylated
  vardf = data.frame()
  for (n in colnames(d[1:7]))
    {
    vardf = rbind(vardf, binomial.var(d, n) )
    }
  
  
  break
  #write(paste("*** Chromosome", chr, "***"), file="cpg-binomial-summary.txt", append=app)
  #write.table(t(vardf), file="cpg-binomial-summary.txt", append=T, quote=F, sep="\t")
  
  
  app = T
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
#cpdf[ cpdf[,2] > cpdf[,4] ,]

# Looking at the  pvaalues showing a correlation between high methylation predictions and SNV occurance
# the GC correlations are all mucch higher
#cpdf[ cpdf[,2] < cpdf[,4] ,]

# This does not appear to hold true when looking at the non-CpG island methylation
#nondf[ nondf[,2] > nondf[,4] ,]
#nondf[ nondf[,2] < nondf[,4] ,]

#write.table(cpdf, file="cpg-summary.txt", quote=F, sep="\t")
#write.table(nondf, file="noncpg-summary.txt", quote=F, sep="\t")
