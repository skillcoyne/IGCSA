rm(list=ls())
mergeTCGAVar<-function(tcga, cosmic)
  {
  counts = vector("numeric", length=max(as.numeric( c(names(tcga), names(cosmic))) ) )
  names(counts) = c(1:length(counts))
  for (i in 1:length(counts))
    counts[i] = sum(tcga[as.character(i)], cosmic[as.character(i)], na.rm=T)
  return(counts[counts > 0])
  }

snv.vector<-function()
  {
  return(vector(mode="numeric", length=4))
  }


source("../lib/fragment_calc.R")

dir = "/Volumes/Spark/Data/Ensembl/Variation/cosmic/chromosomes"  
files = list.files(path=dir, pattern="chr*")    

cntA = snv.vector()
cntC = snv.vector()
cntG = snv.vector()
cntT = snv.vector()

var_sizes = list()
for (f in files)
  {
  print(f)
  file = paste(dir, f, sep="/") 
  d = read.table(file, sep="\t", header=T)
  print(nrow(d))
  d$var.length = (d$end - d$start) + 1
  
  cntA = colSums(rbind(cntA, countSNV(d[ d$var.type == 'SNV', ], 'A')))
  cntC = colSums(rbind(cntC, countSNV(d[ d$var.type == 'SNV', ], 'C')))
  cntG = colSums(rbind(cntG, countSNV(d[ d$var.type == 'SNV', ], 'G')))
  cntT = colSums(rbind(cntT, countSNV(d[ d$var.type == 'SNV', ], 'T')))
  
  
  for (v in unique(d$var.type))
    {
    if (v == 'SNV') next
    
    var = d[ d[,'var.type'] == v, ]
  
    if (length(var_sizes[[v]]) <= 0)
        var_sizes[[v]] = var
    else 
      var_sizes[[v]] = rbind(var_sizes[[v]], var)
    }
  rm(d)
  }

# mix in TCGA
load(paste(outdir, "tcga-ins-del.Rdata", sep="/"))
load(paste(outdir, "tcga_snps.Rdata", sep="/"))

ins = mergeTCGAVar(tcga_ins, table(var_sizes[["insertion"]]$var.length))
del = mergeTCGAVar(tcga_dels, table(var_sizes[["deletion"]]$var.length))

# Substitutions have two sizes.  The reference sequence and the variant. Neither of which correspond 
# to the var.length calculated initially, not sure what to do with this
subst = var_sizes[["substitution"]]
table(nchar(as.character(subst$ref.seq)),nchar(as.character(subst$var.seq)))

# SA just means an insertion or deletion.  There's not enough here to really bother trying to ferret out which, 
# I'll have plenty large scale indels from the karyotype data
table(var_sizes[["sequence_alteration"]]$var.length)

# there are a fair number of deletions that are much larger than 1kb, but only show up once
maxbp = c(10,seq(100,1000,100))
sizeFreq(ins, maxbp)
sizeFreq(del, maxbp) 

cntA = cntA[ names(cntA) %in% c('A','C','G','T')]
cntC = cntC[ names(cntC) %in% c('A','C','G','T')]
cntG = cntG[ names(cntG) %in% c('A','C','G','T')]
cntT = cntT[ names(cntT) %in% c('A','C','G','T')]

tcga_c1 = countSNV


# SNPs
A = probSNV(cntA)
C = probSNV(cntC)
T = probSNV(cntT)
G = probSNV(cntG)
all = rbind(A,C,G,T)

outdir = "/Volumes/Spark/Analysis/Database/cancer"

snvfile = paste(outdir, "snv_table.txt", sep="/")
write.table(all, file=snvfile,   quote=F, col.names=F, append=F, row.names=T, sep="\t")

## NOT ENTIRELY SURE HOW I WANT TO CODE/USE THIS INFO ##

