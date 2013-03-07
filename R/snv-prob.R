

prob.snv<-function(df)
  { 
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



