args = commandArgs(trailingOnly = TRUE)
#print(args)
if (length(args) < 3) stop("Usage: CpG-analysis.R <chromosome number format: chr10> <data directory> <working dir>")

print(args[1])

chr = args[1]
dir = args[2]
wd =  args[3]

source(paste(wd, "lib/gc_functions.R", sep="/"))

print(paste("Reading chr", chr))
print(paste("Directory:", dir))



ens_dir = paste(dir,"/VariationNormal/Frequencies/1000/Ensembl", sep="")
var_file = paste(ens_dir, list.files(path=ens_dir, pattern=paste(chr,"txt", sep=".")), sep="/")

gc_dir = paste(dir, "/VariationNormal/GC/1000", sep="")
gc_file = paste(gc_dir, list.files(path=gc_dir, pattern=paste(chr,"-gc.txt", sep="")), sep="/")

cpg_dir = paste(dir, "HDMFPred", sep="/")
cpg_file = paste(cpg_dir, list.files(path=cpg_dir, pattern=paste(chr,"txt", sep=".")), sep="/")

chrdir = paste(dir, "VariationNormal", chr, sep="/")
if (!file.exists(chrdir)) dir.create(chrdir)
 
# Variation & gc files
  
data = load.data(gc_file, var_file)
gc = data$gc
var_data = cbind(data$vars, gc)
  
# Taking up a lot of memory so gotta try to clean up
rm(gc)
rm(data)
  
cpgd = load.cpg(cpg_file)
 
print(paste("Total fragments", nrow(var_data)))
varnorm = paste(dir, "VariationNormal", sep="/")
nam=T; app=F
for (i in 1:nrow(var_data))
  {
  fragE = as.numeric( rownames(var_data[i,]) )
  fragS = fragE-1000
  islands = cpgd[ cpgd$RangeS >= fragS & cpgd$RangeE <= fragE,  ]
  if (nrow(islands) > 0) 
    { 
    var_data[i, 'Pred.CpGI'] = nrow(islands)
    var_data[i, 'Med.Methy.Pred'] = median(islands$Meth.Prob)
    app=T
    }
  write.table(var_data[i,], file=paste(chrdir, "/", chr, "-varCpG.txt", sep=""), sep="\t", quote=F, append=app, col.names=nam)
  nam=F
  }
  
