args = commandArgs(trailingOnly = TRUE)
chr = args[1]


chr = 1

window = 1000

out_dir = "~/Analysis/TCGA"

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t")
cancer_info = read.table("~/Data/TCGA/cancer_abbreviations.txt", header=T, sep="\t")
cancers = cancer_info$Study.Abbr

d = read.table(paste("~/Data/TCGA/chr", chr, "_variants.txt", sep=""), header=T, sep="\t")
d = d[order(d$StartPosition),]

chrdir = paste(out_dir, paste('chr',chr,sep=""), sep="/")
dir.create(chrdir, recursive=T)

maxlength = chr_info[ chr_info$Chromosome == chr, 'Base.pairs']
bins = ceiling(maxlength/window)
    
cols=T; app=F
min = 0; max = 0; 
for (i in 1:bins)
  {
  max = max + window;
  print(paste(chr, min, max, sep=" : "))
  chunk = d[ d$StartPosition >= min & d$EndPosition <= max, ]    

  # Just wondering if it's common for the mutations to show up in the same locations
  app=F; cols=T;
  t = t(table(chunk$Cancer, chunk$StartPosition))
  locs = t[apply(t, 1, function(row){all(row > 0)}),]
  if (length(locs) > 0)
    {
    write.table(locs, file=paste(chrdir, "locations.txt", sep="/"), quote=F, append=app, col.names=cols, row.names=T)
    app=T; cols=F;
    }
    
  freq = t(table(chunk$VarType))
  rownames(freq) = as.character(max)
  write.table(freq, file=paste(chrdir, "freq.txt", sep="/"), quote=F, col.names=cols, row.names=T, append=app)  
  
  ct = t(table(chunk$Cancer))
  rownames(ct) = as.character(max)
  write.table(ct, file=paste(chrdir, "cancer-freq.txt", sep="/"), quote=F, col.names=cols, row.names=T, append=app)  

  
  center = t(table(chunk$Center))
  rownames(center) = as.character(max)
  write.table(center, file=paste(chrdir, "center-freq.txt", sep="/"),quote=F, col.names=cols, row.names=T, append=app)  
  
  min = max;
  rm(chunk)
  }
  





