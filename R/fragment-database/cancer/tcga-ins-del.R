


var.length<-function(df)
  {
  var_size = vector(mode="integer", length=max(df))
  names(var_size) = c(1:max(df))
  for (i in 1:max(df))
    {
    var_size[i] = length(df[df[,1] == i, 1]) + length(df[df[,2] == i, 2])
    }
  return(var_size)
  }


dir = "/Volumes/Spark/Data/TCGA"  
files = list.files(path=dir, pattern="chr*")    

outdir = "/Volumes/Spark/Analysis/Database/cancer"

for (file in files)
  {
  print(file)
  d = read.table(paste(dir, file, sep="/"), header=T, sep="\t")

  for (v in c('INS', 'DEL'))
    {
    var = d[ d[,'VarType'] == v, ]

    var$SizeA1 = nchar( as.vector(var[,'TumorAllele1']) )  
    var$SizeA2 = nchar( as.vector(var[,'TumorAllele2']) ) 
  
    if (v == 'INS')
      {
      if (!exists("insSize")) insSize = var[, c('SizeA1', 'SizeA2') ]
      else insSize = rbind(insSize, var[, c('SizeA1', 'SizeA2') ])
      }
    else
      {
      if (!exists("delSize")) delSize = var[, c('SizeA1', 'SizeA2') ]
      else delSize = rbind(delSize, var[, c('SizeA1', 'SizeA2') ])
      }
    }
  rm(d)
  }


## These aren't hugely different from the normal variation for insertions/deletions.  However, not seeing anything very large.  
tcga_dels = var.length(delSize)
tcga_ins = var.length(insSize)

save(tcga_dels, tcga_ins, file=paste(outdir, "tcga-ins-del.Rdata", sep="/"))
