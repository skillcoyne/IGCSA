## 
## Script counts variations by fragment across the chromosome
##
rm(list=ls())

args = commandArgs(trailingOnly = TRUE)
filein = args[1]
fileout = args[2]
chrinfofile = args[3]
#window = as.numeric(args[3])

#filein = "~/Data/Ensembl/Variation/chromosomes/chr19.txt"
window = 1000

print(paste("File in (should be from the ruby read_variations.rb script:", filein))
print(paste("The file to write variation information to:", fileout))
print(paste("Chromosome info file:", chrinfofile))
#print(paste("Bin size: ", as.character(window)))

chr_info = read.table(chrinfofile, header=T, sep="\t")
#chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t")

d = read.table(filein, header=T, sep="\t")
chr = as.character(d[1,1]);

variation_types = unique(d$var.type)
d$var.length = d$end - d$start


maxlength = chr_info[ chr_info$Chromosome == chr, 'Base.pairs']

bins = ceiling(maxlength/window)

print( paste("Chr length:", as.character(maxlength), " bins:", as.character(bins) ) )

min = 0; max = 0; 
app=F; cols=NA
for (i in 1:bins)
  {
  max = max + window;
  print(paste(min, "-", max))
  chunk = d[ d$start >= min & d$end <= max, ]  
  freq = t(table(chunk$var.type))
  rownames(freq) = as.character(max)
  
  min = max;
  
  write.table(freq, file=fileout, quote=F, row.names=T, col.names=cols, append=app, sep="\t")
  app=T;cols=F

  rm(chunk, freq)
  }



