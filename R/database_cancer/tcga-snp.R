rm(list=ls())
table.allele<-function(vct)
  {
  freqNuc = table(vct)
  # R thing I still don't get but this makes sure all of the other variations stay out
  freqNuc = freqNuc[freqNuc > 0]
  return(freqNuc)  
  }

prob.snv<-function(df, nuc)
  { 
  seq = df[ df[,'RefAllele'] == nuc, c('TumorAllele1', 'TumorAllele2') ]

  freqA1 = table.allele( seq[ seq[,1] != nuc, 1] )
  freqA2 = table.allele( seq[ seq[,2] != nuc, 2] )

  freq = cbind(freqA1, freqA2)
  freq = rowSums(freq)

  probs = freq/sum(freq)  
  probs[nuc] = 0
  probs = probs[order(names(probs))] # always return A,C,G,T order
  probs = round(probs[ !is.na(probs) ], 2)
  
  # make sure it all sums to 0
  if (sum(probs) > 0)
    {
    maxIndex = which(probs == max(probs))
    probs[maxIndex] = probs[maxIndex] + (1-sum(probs))
    }
  return(probs)  
  }
  
dir = "~/Data/TCGA"  
files = list.files(path=dir, pattern="chr*")    
  
alleleCols = c('RefAllele', 'TumorAllele1', 'TumorAllele2')
for (file in files)
  {
  print(file)
  d = read.table(paste(dir, file, sep="/"), header=T, sep="\t")
  if (!exists('snps')) snps = d[d$VarType == 'SNP', alleleCols]
  else snps = rbind(snps, d[d$VarType == 'SNP', alleleCols])
  rm(d)
  }

A = prob.snv(snps, 'A')
C = prob.snv(snps, 'C')
G = prob.snv(snps, 'G')
T = prob.snv(snps, 'T')

all = rbind(A,C,G,T)

outfile = "~/Analysis/Database/cancer/snv-prob.txt"
write.table(all, file=outfile, quote=F, col.names=F, sep="\t")

  
  
  