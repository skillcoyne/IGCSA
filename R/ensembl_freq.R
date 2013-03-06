## 
## Script counts variations by fragment across the chromosome
##

args = commandArgs(trailingOnly = TRUE)
filein = args[1]
fileout = args[2]
window = as.numeric(args[3])

print(paste("File in (should be from the ruby read_variations.rb script:", filein))
print(paste("The file to write varation information to:", fileout))
print(paste("Bin size: ", as.character(window)))

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t")

d = read.table(filein, header=T, sep="\t")
chr = as.character(d[1,1]);

maxlength = chr_info[ chr_info$Chromosome == chr, 'Base.pairs']

bins = ceiling(maxlength/window)

print( paste("Chr length:", as.character(maxlength), " bins:", as.character(bins) ) )

min = 0; max = 0; 
for (i in 1:bins)
  {
  max = max + window;
  #print(paste(min, "-", max))
  chunk = d[ d$start >= min & d$end <= max,  ]  
  freq = t(table(chunk$var.type))
  rownames(freq) = as.character(max)
  
  if(!exists("all_freq"))   all_freq = freq
  else all_freq = rbind(all_freq, freq)

  min = max;
  # clean up since I'm creating a huge table
  rm(chunk)
  rm(freq)
  }
#rm(d)

write.table(file=fileout, all_freq, quote=F, row.names=T, sep="\t")




