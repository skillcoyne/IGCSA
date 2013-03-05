source("lib/gc_functions.R")


dir = "~/Data"
ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

#cpg_dir = paste(dir, "HDMFPred", sep="/")
#cpg_file = paste(cpg_dir, list.files(path=cpg_dir, pattern=paste(chr,"txt", sep=".")), sep="/")

pvalues = matrix(ncol=14, nrow=length(var_files))
colnames(pvalues) = c('SNV', 'SNV.log', 'deletion', 'deletion.log', 'indel', 'indel.log', 'insertion', 'insertion.log', 'sequence_alteration', 
                      'sequence_alteration.log', 'substitution', 'substitution.log', 'tandem_repeat', 'tandem_repeat.log')
rownames(pvalues) = c(1:22, 'X', 'Y')

# Variation & gc files
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  chrnum = sub("chr", "", chr)
  print(chr)
  var_file = paste(ens_dir, file, sep="/")
  gc_file = paste(gc_dir, "/", chr, "-gc.txt", sep="")
  data = load.data(gc_file, var_file)
  #gc = data$gc
  var_d = data$vars

  # A few variations appear poisson in the plots, but all of them tend to have some extreme values
  # A few of them are poisson if the counts are logged due to the extremes (deletion, indel), the rest
  # aren't.
  for (var in colnames(var_d[,2:ncol(var_d)]))
    {
    test = wilcox.test(var_d[[var]], rpois(1000, 0.5))
    pvalues[chrnum, var] = signif(test$p.value, 4)
    test = wilcox.test(log(var_d[[var]]), rpois(1000, 0.5)) 
    pvalues[chrnum, paste(var, 'log', sep=".")] = signif(test$p.value, 4)
    }

  # SNPs are a special case.  They appear to have a bimodal distribution with a median around 15 for all chromosomes
  # Each chromosome is different though.  21 is poisson but not if logged, 1
  test = wilcox.test(var_d$SNV, rpois(1000, median(var_d$SNV)))
  pvalues[chrnum, 'SNV'] = signif(test$p.value, 4)
  test = wilcox.test(log(var_d$SNV), rpois(1000, median(log(var_d$SNV))))
  pvalues[chrnum, 'SNV.log'] = signif(test$p.value, 4)
  }

write.table(pvalues, file=paste(dir, "/VariationNormal/wilcox-tests.txt", sep=""), sep="\t", quote=F)



