


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


dir = "~/Data/TCGA"  
files = list.files(path=dir, pattern="chr*")    

for (file in files)
  {
  print(file)
  d = read.table(paste(dir, file, sep="/"), header=T, sep="\t")

  for (v in c('INS', 'DEL'))
    {
    var = d[ d[,'VarType'] == v, ]

    var$SizeA1 = nchar( as.vector(var[,'TumorAllele1']) )  
    var$SizeA2 = nchar( as.vector(var[,'TumorAllele2']) ) 
  
    if (var == 'INS')
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
dels = var.length(delSize)
ins = var.length(insSize)




sum(dels[1:10])/sum(dels)
round(sum(dels[11:99])/sum(dels), 4)

