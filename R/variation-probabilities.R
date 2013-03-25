rm(list = ls())
data_dir =  "~/Data/Ensembl/Variation/chromosomes"
files = list.files(path=data_dir, pattern=".txt")  

out_dir = "~/Analysis/Database"

for (f in files)
  {
  filein = paste(data_dir, f, sep="/")
  d = read.table(filein, header=T, sep="\t")
  chr = as.character(d[1,1]);

  variation_types = unique(d$var.type)
  d$var.length = d$end - d$start
  
 if(!exists('all_d')) all_d = d else all_d = rbind(all_d, d) 
  }
  
size = data.frame()
for (var in variation_types)
  {
  if (var == 'SNV') next
  t = table(all_d[all_d$var.type == var, 'var.length']+1)
  size['10', var] =  sum( t[ as.character(c(1:10)) ], na.rm=T )
  size['100', var] = sum( t[ as.character(c(11:100)) ], na.rm=T )
  for (i in seq(100, 900, 100) )
    {
    min=i;max=i+100;
    size[ as.character(max), var ] = sum( t[ as.character(c(min:max)) ], na.rm=T)
    }
    
  size[[var]] = size[[var]]/sum(size[[var]])
  size[[var]] = round(size[[var]], 4)
  if (sum(size[[var]]) > 1)  size[1,var] = size[1,var] + (1-sum(size[[var]]))
  if (sum(size[[var]]) < 1)  size[1,var] = size[1,var] + (1-sum(size[[var]]))
  }


#write.table(all_vars[ ,c(8,7,1:6) ], paste(out_dir, "variation-size-table.txt", sep="/"), quote=F, sep="\t", col.names=T, row.names=F)

