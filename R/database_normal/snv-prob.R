#
# This can output the SNV probabilities, currently used in a properties file but should probably be put in database.
#
rm(list = ls())

count.snv<-function(df, nuc)
  {
  seq = df[ df[,'ref.seq'] == nuc, 'var.seq' ]
  freqNuc = table(seq)
  # R thing I still don't get but this makes sure all of the other variations stay out
  freqNuc = freqNuc[freqNuc > 0]
  
  for (n in names(freqNuc))
    {
    if ( length(grep(",", n)) > 0  )
      {
      count = freqNuc[n]
      multNuc = strsplit(n, ",")
      if (length(multNuc) >= 1)
        {
        for (m in multNuc[1])
          {
          freqNuc[m] = freqNuc[m] + count
          }
        freqNuc[n] = NA
        }
      }  
    }
  freqNuc[nuc] = 0
  freqNuc = freqNuc[!is.na(freqNuc)]
  # always return A,C,G,T order
  freqNuc = freqNuc[order(names(freqNuc))] 
  return(freqNuc)
  }

prob.snv<-function(counts)
  { 
  probs = counts/sum(counts, na.rm=T)  
  probs = probs[order(names(probs))] 
  probs = round(probs[ !is.na(probs) ], 2)
  
  # make sure it all sums to 0
  if (sum(probs) > 0)
    {
    maxIndex = which(probs == max(probs))
    probs[maxIndex] = probs[maxIndex] + (1-sum(probs))
    }
  return(probs)  
  }

snv.vector<-function()
  {
  return(vector(mode="numeric", length=4))
  }

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
  
  cntA = colSums(rbind(cntA, count.snv(d[ d$var.type == 'SNV', ], 'A')))
  cntC = colSums(rbind(cntC, count.snv(d[ d$var.type == 'SNV', ], 'C')))
  cntG = colSums(rbind(cntG, count.snv(d[ d$var.type == 'SNV', ], 'G')))
  cntT = colSums(rbind(cntT, count.snv(d[ d$var.type == 'SNV', ], 'T')))
  
  rm(d)
  }

A = prob.snv(cntA)
C = prob.snv(cntC)
T = prob.snv(cntT)
G = prob.snv(cntG)
all = rbind(A,C,G,T)

write.table(all, file=outfile, quote=F, col.names=F, sep="\t")
