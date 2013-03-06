
source("lib/gc_functions.R")

prob.snv<-function(df)
  { # this isn't right come back to it
  df$prob =  df[,2]/sum(df[,2] ) 
  return(df)
  }


dir = "~/Data/VariationNormal/Frequencies"

prob_dir = "~/Data/VariationNormal"

file = list.files(path=dir, pattern="snv-counts.txt")
d = read.table( paste(dir, file, sep="/"), header=F, sep="\t" )
colnames(d) = c('snv', 'count')
d = d[ order(d$snv), ]
  
a = prob.snv(d[1:3,])
c = prob.snv(d[4:6,])
g = prob.snv(d[7:9,])
t = prob.snv(d[10:12,])
  
newd = rbind(a,c,g,t)
newd = newd[,-2]
  
#write.table(newd, quote=F, sep="\t", col.names=F, row.names=F)
write.table(newd, quote=F, sep="\t", col.names=F, row.names=F, file=paste(prob_dir, "snp-prob.txt", sep="/"))
rm(a,c,g,t,d,newd)


## These probabilities are in addition to the probability of each variation type, determined per chromosome
sizes = c(1,10,100,1000)
files = list.files(path=dir, pattern="*length-count.txt")
for (file in files)
  {
  filename = sub("^.*\\/", "", file)
  variation = sub("-length-count.txt", "", filename)

  d = read.table( paste(dir, file, sep="/"), header=T, sep="\t")
  
  probs = matrix(ncol=2, nrow=length(sizes))
  probs[,1] = sizes
  
  for( i in 1:length(sizes) )
    {
    if (i == 1) chunk = d[d[,1] == sizes[i],]
    if (i > 1 ) chunk = d[d[,1] <= sizes[i] & d[,1] > sizes[i-1],]
    
    probs[i,2] = sum(chunk[,2])/sum(d[,2])
    }
  filename = paste(variation, "-prob.txt", sep="")
  write.table(probs, quote=F, col.names=F, row.names=F, sep="\t", file=paste(prob_dir, filename, sep="/"))
  #print(variation)
  #write.table(probs, quote=F, col.names=F, row.names=F, sep="\t")
  rm(probs, d, chunk)
  }






