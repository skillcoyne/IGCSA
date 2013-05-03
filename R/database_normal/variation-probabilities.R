# POST ANALYSIS SCRIPT
# This script just generates the size frequency for variations that are length-variable (e.g. deletions, insertions)
# The input data are the files generated from the ensembl variation data that provide start/end locations and variation type


rm(list = ls())
data_dir =  "~/Data/Ensembl/chromosomes"
files = list.files(path=data_dir, pattern="chr*")  

out_dir = "~/Analysis/Database/normal"

maxbp = c(10,seq(100,1000,100))

variation_rdata = paste(out_dir, "vars.Rdata", sep="/")
if ( !file.exists(variation_rdata) ) 
  { 
  variations = vector(mode="character")
  for (f in files)
    {
    filein = paste(data_dir, f, sep="/")
    d = read.table(filein, header=T, sep="\t")
    chr = as.character(d[1,1]);
    variations = c(variations, as.vector(unique(d$var.type)))
    variations = unique(variations)
    }
  save(variations, file=variation_rdata)
  }
load(variation_rdata) 

size = as.data.frame(matrix(0, ncol=length(variations), nrow=length(maxbp), dimnames=list(maxbp, variations)))
for (f in files)
  {
  print(f)
  filein = paste(data_dir, f, sep="/")
  d = read.table(filein, header=T, sep="\t")
  chr = as.character(d[1,1]);
  if(chr == "Y") next
  
  variation_types = unique(d$var.type)
  d$var.length = d$end - d$start
  
  for (var in variation_types)
    {
    if (var == 'SNV') next
    t = table(d[d$var.type == var, 'var.length']+1)
    size['10', var] = size['10', var] +  sum( t[ as.character(c(1:10)) ], na.rm=T )
    size['100', var] = size['100', var] + sum( t[ as.character(c(11:100)) ], na.rm=T )
    for (i in seq(100, 900, 100) )
      {
      min=i;max=i+100;
      size[ as.character(max), var ] = size[ as.character(max), var ] + sum( t[ as.character(c(min:max)) ], na.rm=T)
      }
    }
  rm(d)
  }
 
for (var in colnames(size))
  {
  size[[var]] = size[[var]]/sum(size[[var]], na.rm=T)
  size[[var]] = round(size[[var]], 4)

  if (sum(size[[var]], na.rm=T) > 1)  size[1,var] = size[1,var] + (1-sum(size[[var]], na.rm=T))
  if (sum(size[[var]], na.rm=T) < 1)  size[1,var] = size[1,var] + (1-sum(size[[var]], na.rm=T))
  }

size[ is.na(size) ] = 0


write.table(size, quote=F, sep="\t", col.names=NA, row.names=T)
write.table(size, file=paste(out_dir, "variation-size-table.txt", sep="/"), quote=F, sep="\t", col.names=NA, row.names=T)

