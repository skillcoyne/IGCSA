rm(list = ls())

simpleCap<-function(x) 
  {
  s <- strsplit(x, '_')[[1]]
  paste(toupper(substring(s, 1,1)), substring(s, 2),
        sep="", collapse="")
  }

data_dir =  "~/Data/Ensembl/Variation/chromosomes"
files = list.files(path=data_dir, pattern="chr*")  

out_dir = "~/Analysis/Database/normal"

variations = vector(mode="character")
print("Getting variation names")
variation_rdata = paste(out_dir, "vars.Rdata", sep="/")
var_per_chr = list()
for (f in files)
  {
  print(f)
  filein = paste(data_dir, f, sep="/")
  d = read.table(filein, header=T, sep="\t")
  chr = as.character(d[1,1]);
  var_per_chr[[chr]] = as.vector(unique(d$var.type))
  variations = c(variations, as.vector(unique(d$var.type)))
  variations = unique(variations)
  }
variations = sort(variations)
save(variations, file=variation_rdata)

chr_var_m = as.data.frame(matrix(ncol=2))
colnames(chr_var_m) = c("chr", "variation_id")

for (chr in names(var_per_chr))
  {
  print(chr)
  vars = var_per_chr[[chr]]

  temp_m = matrix(ncol=2, nrow=length(vars))
  colnames(temp_m) = colnames(chr_var_m)
  temp_m[,1] = chr
  for (i in 1:length(vars))
    {
    temp_m[i,2] = which( variations == vars[i] ) 
    }
  chr_var_m = rbind(chr_var_m, temp_m)
  }

variations = as.data.frame(variations)
for (i in 1:nrow(variations))
  {
  name = variations[i, 1]
  if (name == "sequence_alteration") name = "indel"
  name = simpleCap(as.character(name))
  variations[i,"class"] = paste("org.lcsb.lu.igcsa.variation.fragment.", name, sep="") 
  }

write.table(na.omit(chr_var_m), file = paste(out_dir, "var_to_chr.txt", sep="/"), quote=F, col.name=T, row.name=F, sep="\t")
write.table(variations,  file=paste(out_dir, "variation.txt", sep="/"), quote=F, row.names=T, col.names=NA, sep="\t")
