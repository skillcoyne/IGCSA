args = commandArgs(trailingOnly = TRUE)
chr = args[1]
out_dir=args[2]
data_dir=args[3]

print(paste("Chromosome", chr, sep=" "))
print(paste("Output dir", out_dir, sep=": "))
print(paste("Data dir", data_dir, sep=": ")) 

window = 1000

#out_dir = "~/Analysis/TCGA"
setwd(data_dir)
chr_info = read.table("chromosome_gene_info_2012.txt", header=T, sep="\t")
cancer_info = read.table("cancer_abbreviations.txt", header=T, sep="\t")
cancers = cancer_info$Study.Abbr

d = read.table(paste("chr", chr, "_variants.txt", sep=""), header=T, sep="\t", quote="")
d = d[order(d$Start),]
d = d[,1:8]
# Due to the way the mafs are structured there's a lot of duplicate data
d = unique(d)

chrdir = paste(out_dir, paste('chr',chr,sep=""), sep="/")
dir.create(chrdir, recursive=T)

maxlength = chr_info[ chr_info$Chromosome == chr, 'Base.pairs']
bins = ceiling(maxlength/window)

#rm(all_freq, all_cnc, center)
min = 0; max = 0; 
app=F; cols=T;
for (i in 1:bins)
  {
  max = max + window;
#  print(paste(chr, min, max, sep=" : "))
  chunk = d[ d$Start >= min & d$End <= max, ]    

  # Just wondering if it's common for the mutations to show up in the same locations
  t = t(table(chunk$Cancer, chunk$Start))
  locs = t[apply(t, 1, function(row){all(row > 0)}),]
  if (length(locs) > 0)
    {
    write.table(locs, file=paste(chrdir, "locations.txt", sep="/"), quote=F, append=app, col.names=cols, row.names=T)
    app=T; cols=F;
    }
    
  freq = t(table(chunk$VarType))
  rownames(freq) = as.character(max)
  if (!exists("all_freq")) all_freq = freq
  else all_freq = rbind(all_freq, freq)
  
  freq = t(table(chunk$Cancer))
  rownames(freq) = as.character(max)
  if (!exists("all_cnc")) all_cnc = freq
  else all_cnc = rbind(all_cnc, freq)
  
  freq = t(table(chunk$Center))
  rownames(freq) = as.character(max)
  if (!exists("center")) center = freq
  else center = rbind(center, freq)
  
  min = max;

  rm(chunk, freq)
  }
  
write.table(all_freq, file=paste(chrdir, "freq.txt", sep="/"), quote=F, col.names=NA, row.names=T)
write.table(all_cnc, file=paste(chrdir, "cancer-freq.txt", sep="/"), quote=F, col.names=NA, row.names=T)  
write.table(center, file=paste(chrdir, "center-freq.txt", sep="/"), quote=F, col.names=NA, row.names=T)  


