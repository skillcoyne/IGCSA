
args <- commandArgs(trailingOnly = TRUE)
print(args)

depth_file = file.path(args[1])

d = read.table(depth_file, header=F)
dir = dirname(depth_file)

for (c in c(1:22, 'X', 'Y'))
  {
  v=vector(mode='numeric', length=10)
  vnm=vector(mode='character', length=10)

  cd = d[which(d[,1] == c), ]
  
  if (nrow(cd) > 0)
    {
    i=1
    start = signif(min(cd[,2])-50, 3)
    window = 100
    while(start < max(cd[,2]))
      {
      v[i] = sum(cd[ which(cd[,2] >= start & cd[,2] < start+window), 3])
      vnm[i] = start
      start = start + window
      i = i + 1
      }
    names(v) = vnm
    
    bin_file = paste(file.path(dir, paste("chr", c, sep='')), "-depth.txt", sep="")
    write.table(v, quote=F, sep="\t", col.names=F, file=bin_file)
    }
  }
