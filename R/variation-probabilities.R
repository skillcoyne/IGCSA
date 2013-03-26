rm(list = ls())
data_dir =  "~/Data/Ensembl/Variation/chromosomes"
files = list.files(path=data_dir, pattern=".txt")  

out_dir = "~/Analysis/Database"


size = data.frame(maxbp=c(10,seq(100,900,100)), deletion=0, insertion=0, sequence_alteration=0, substitution=0, indel=0, tandem_repeat=0, row.names=1)
for (f in files)
  {
  filein = paste(data_dir, f, sep="/")
  d = read.table(filein, header=T, sep="\t")
  chr = as.character(d[1,1]);

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

 
write.table(size, paste(out_dir, "variation-size-table.txt", sep="/"), quote=F, sep="\t", col.names=NA, row.names=T)

