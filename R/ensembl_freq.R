args = commandArgs(trailingOnly = TRUE)
filein = args[1]
fileout = args[2]
window = as.numeric(args[3])

#window = 1000

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t")

d = read.table(filein, header=T, sep="\t")
chr = d[1,1];

maxlength = chr_info[ chr_info$Chromosome == chr, 'Base.pairs']
bins = ceiling(maxlength/window)

min = 0; max = 0; rm(all_freq);
for (i in 1:bins)
  {
  max = max + window;

  chunk = d[ d$start >= min & d$end <= max,  ]  
  freq = t(table(chunk$var.type))
  rownames(freq) = paste(min, "-", max)
  
  if(!exists("all_freq"))   all_freq = freq
  else all_freq = rbind(all_freq, freq)

  min = max;
  # clean up since I'm creating a huge table
  rm(chunk)
  rm(freq)
  }
rm(d)

write.table(file=fileout, all_freq, quote=F, row.names=T, sep="\t")




