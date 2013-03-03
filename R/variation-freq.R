plotVariation<-function(table, chr, var)
  {
  plotname = paste("variation/", paste(chr, var, sep="-"), ".png", sep="")
  png(filename=plotname, height=600, width=600, bg='white', units='px')
  par(mfrow=c(2,1))
  plot(d[[var]], type='h', xlab='Chromosome location', ylab=paste(var, 'Counts'), main=chr)
  freq = table(d[[var]])
  plot(log(freq), xlab=paste(var, "in bin"), ylab="log(frequency)", main=chr)
  dev.off()
  }

setwd("~/Data/Ensembl")
files = list.files(pattern=".txt")  
for(i in 1:length(files))
  {
  d = read.table(files[i], header=T, sep="\t")
  chr = sub(".txt", "", files[i])
  variations = names(d)
  for (var in variations)
  	{ plotVariation(d, chr, var) }  
  rm(d)
  }