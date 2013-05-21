#
# This can output the SNV probabilities, currently used in a properties file but should probably be put in database.
#
rm(list = ls())

snv.vector<-function()
  {
  return(vector(mode="numeric", length=4))
  }

source("../lib/fragment_calc.R")

data_dir =  "~/Data/Ensembl/Variation/chromosomes"
files = list.files(path=data_dir, pattern="chr*")  

if (length(files) <= 0) 
  {
  print("No files in data directory")
  exit(-1)
  }

outfile = "~/Analysis/Database/normal/snv_table.txt"

cntA = snv.vector()
cntC = snv.vector()
cntG = snv.vector()
cntT = snv.vector()

for (f in files)
  {
  if (f == "chrY.txt" || f == "chrX.txt") next
  print(f)
  filein = paste(data_dir, f, sep="/")
  d = read.table(filein, header=T, sep="\t")
  chr = as.character(d[1,1]);
  print(nrow(d))
  
  cntA = colSums(rbind(cntA, countSNV(d[ d$var.type == 'SNV', ], 'A')))
  cntC = colSums(rbind(cntC, countSNV(d[ d$var.type == 'SNV', ], 'C')))
  cntG = colSums(rbind(cntG, countSNV(d[ d$var.type == 'SNV', ], 'G')))
  cntT = colSums(rbind(cntT, countSNV(d[ d$var.type == 'SNV', ], 'T')))
  
  rm(d)
  }

A = probSNV(cntA)
C = probSNV(cntC)
T = probSNV(cntT)
G = probSNV(cntG)
all = rbind(A,C,G,T)

write.table(all, file=outfile, quote=F, col.names=F, sep="\t")
