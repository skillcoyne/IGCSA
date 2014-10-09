
setwd("~/Dropbox/Private/Work/Work Journal/Karyotype-Sequence Comparison")

match_count<-function(a, b, col='read')
  {
  a = a[[1]]; b = b[[1]]
  a = a[order(a[,col]),]; b = b[order(b[,col]),]
  n=nrow(a[ which(a[,col] %in% b[,col]), ])
  return(n)
  }


bands = list("1q21" = read.table("1q21-pp.reads.txt", header=T), 
             "1p36" = read.table("1p36-pp.reads.txt", header=T), 
             "10q24" = read.table("10q24-pp.reads.txt", header=T), 
             "5q31" = read.table("5q31-pp.reads.txt", header=T))

totals = vector(mode='numeric', length(bands))
names(totals) = names(bands)
for (i in 1:length(bands))
  {
  totals[i] = nrow(as.data.frame(bands[i]))
  }

data = as.data.frame(bands[1])

names=names(bands)
df = as.data.frame(matrix(data=0, nrow=4,ncol=4, dimnames=list(names, names)))

for (i in 1:length(bands))
  {
  for (j in 1:length(bands))
    {    
    if (names)
    
    count = match_count(bands[i], bands[j])
    df[ names(bands[i]), names(bands[j])] = count
#    print( paste(names(bands[i]), names(bands[j]), count, sep="  ") )
    }
  }

prop = df
for ( b in rownames(df) )
  {
  total = totals[b]
  prop[b,] = round(df[b,]/total, 3)*100
  }

write.table(df, quote=F, sep=",")

