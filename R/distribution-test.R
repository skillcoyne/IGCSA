# 
# Analysis script.
# The distribution of variations appeared to be poisson or bimodal in most chromosomes (X,Y excepted). To assess if this was something I should concern myself
# with modeling I applied a Mann-Whitney against the distributions of the number of variations of a given type per fragments of 1kb in size.
#
rm(list=ls())
source("~/workspace/IGCSA/R/lib/gc_functions.R")


dir = "~/Data"
ens_dir = "~/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = "~/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

#cpg_dir = paste(dir, "HDMFPred", sep="/")
#cpg_file = paste(cpg_dir, list.files(path=cpg_dir, pattern=paste(chr,"txt", sep=".")), sep="/")

out_dir = "~/Analysis/Normal"

result = tryCatch({
  load("~/Analysis/Database/normal/vars.Rdata")
}, warning = function(w) {
  warning(paste(gettext(w), "Run the variation-table script in /database_normal first.", sep=""))
  exit(-1)
}, error = function(e) {
  print("Missing list of variations.  Run the variation-table script in /database_normal first.")
  exit(-1)
})

pvalues = matrix(ncol=length(variations)*2, nrow=length(var_files))
columns = vector(mode="character", length=length(variations)*2)
i = 1
for (var in variations)
  {
  print(var)
  columns[i] = var
  columns[i+1] = paste(var, ".log", sep="")
  i=i+2
  }
colnames(pvalues) = columns  
rownames(pvalues) = c(1:22, 'X','Y')

# Variation & gc files
for (file in var_files)
  {
  chr = sub(".txt", "", file)
  #if (chr == 'chrX' || chr == 'chrY') next
  print(chr)
  var_file = paste(ens_dir, file, sep="/")
  gc_file = paste(gc_dir, "/", chr, "-gc.txt", sep="")
  data = load.data(gc_file, var_file)
  #gc = data$gc
  var_d = data$vars

  # A few variations appear poisson in the plots, but all of them tend to have some extreme values
  # A few of them are poisson if the counts are logged due to the extremes (deletion, indel), the rest
  # aren't.
  chrnum = as.numeric(sub("chr", "", chr))
  for (var in colnames(var_d[1:ncol(var_d)]))
    {
    test = wilcox.test(var_d[[var]], rpois(1000, 0.5))
    pvalues[chrnum, var] = test$p.value
    test = wilcox.test(log(var_d[[var]]), rpois(1000, 0.5)) 
    pvalues[chrnum, paste(var, 'log', sep=".")] = test$p.value
    }
  }

write.table(pvalues, file=paste(out_dir, "wilcox-tests.txt", sep="/"), sep="\t", quote=F, col.names=NA)

pvalues = as.data.frame(pvalues)
#plot(pvalues[,'deletion.log'], type='l', xaxt='n')
#axis(1, at=1:nrow(pvalues), labels=rownames(pvalues))



