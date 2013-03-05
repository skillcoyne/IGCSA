

setwd("~/Data/VariationNormal/")
files = list.files(pattern="genes-dist2.txt", recursive=T)  

rm(counts)
for (f in files)
  {
  chr = sub("/genes-dist2.txt", "", f)
  g = read.table(f, header=T, sep="\t")
  
  if (!exists("counts")) counts = g else counts = rbind(counts, g)
  #rm(g)
  break
  }

freq = table(counts$Total.Genes)
png(filename="genes-dist2.png", width=650, height=650)
barplot(freq, main="From 2nd dist across all Chromosomes", xlab="# of genes found in fragments", ylab="1kb fragments", col="blue", sub=paste("Total bins:", nrow(counts)))
dev.off()

