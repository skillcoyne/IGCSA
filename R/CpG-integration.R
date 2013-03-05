args = commandArgs(trailingOnly = TRUE)
#print(args)
if (length(args) < 3) stop("Usage: CpG-analysis.R <chromosome number format: chr10> <data directory> <working dir>")

print(args[1])

#chr = 'chr1'
#dir = "~/Data"
#wd = "~/workspace/IGCSA/R"

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
  
cpgd = load.cpg(cpg_file, cpgI.only=F)
 
high = cpgd[cpgd$Meth.Prob >= 0.5, ]
low = cpgd[cpgd$Meth.Prob < 0.5, ]

table(low[,1])
table(high[,1])

print(paste("Total fragments", nrow(var_data)))
varnorm = paste(dir, "VariationNormal", sep="/")

var_data$Pred.CpGI = NA
var_data$CpGI.Meth = NA
var_data$NonCpGI.Meth = NA

nam=T; app=F
for (i in 1:nrow(var_data))
  {
  fragE = as.numeric( rownames(var_data[i,]) )
  fragS = fragE-1000
  islands = cpgd[ cpgd$RangeS >= fragS & cpgd$RangeE <= fragE,  ]
  if (nrow(islands) > 0 ) 
    { 
    # 1 -> CpG Island, 0 is nonCpG
    var_data[i, 'Pred.CpGI'] = nrow(islands[islands$CpG > 0,]) 
    var_data[i, 'CpGI.Meth'] = mean(islands[islands$CpG == 1, 'Meth.Prob'])
    var_data[i, 'NonCpGI.Meth'] = mean(islands[islands$CpG < 1, 'Meth.Prob'])
    }
  write.table(var_data[i,], file=paste(chrdir, "/", chr, "-varCpG.txt", sep=""), sep="\t", quote=F, append=app, col.names=nam)
  #write.table(var_data[i,], sep="\t", quote=F, append=app, col.names=nam)
  nam=F; app=T
  }
  
