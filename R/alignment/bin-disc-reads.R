bin<-function(dt, window=8000)
  {
  v=vector(mode='numeric', length=10)
  vnm=vector(mode='character', length=10)
  
  start = signif(min(dt[,2]-50), 4)
  i = 1
  while(nrow(dt) > 0 & start < max(dt[,2]) )
    {
    wrows =  which(dt[,2] >= start & dt[,2] < start+window)
    count = nrow(dt[wrows,])
    if (count > 3)
      {
      v[i] = count
      vnm[i] = start
      i = i + 1
      }
    
    if (count > 0) dt = dt[ -wrows, ]
    start = start + window
    #print(nrow(dt))
    }
  names(v) = vnm
  return(v)
  }

args <- commandArgs(trailingOnly = TRUE)
print(args)

args = c("/tmp/depth/chr4.reads")

if (length(args) < 1) stop("TSV file required as input with the following fields: 'read' 'ref' 'position' 'mate' 'mate position' ")

window = 8000
if (length(args) == 2) window = args[2]
  

d = read.table(args[1], header=F)
colnames(d) = c("read", "ref", "pos", "mate", "mpos")

d[1:10,]

mates = unique(d$mate)
mates = mates[mates %in% c(1:22, 'X', 'Y')]

for (chr in mates)
  {
  rows = which(d$mate == chr)
  pair = d[rows,]
  # bin ref pos?
  chrA = bin(pair[, c('ref', 'pos')], window)
  chrB = bin(pair[, c('mate', 'mpos')], window)
  
  d = d[-rows,]
  
  }
