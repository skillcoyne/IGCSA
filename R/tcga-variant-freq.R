rm(list=ls())
source("lib/gc_functions.R")

args = commandArgs(trailingOnly = TRUE)
chr = args[1]

chr=10


var_f = paste("~/Data/VariationNormal/Frequencies/1000/Ensembl", "chr10.txt", sep="/")

gc_file = paste("~/Data/VariationNormal/GC/1000/chr", chr, "-gc.txt", sep="")
gcf = gc.data(gc_file)
unk = which(gcf$UnkRatio == 1)

data = load.data(gc_file, var_f)
vd = data$vars
gd = data$gc


chr_data_dir = paste("~/Data/CancerVariation/chr", chr, sep="")
d_file = paste(chr_data_dir, "freq.txt", sep="/")
df = read.table(d_file, header=T, sep=" ", row.names=1)
#df = df[,c(2:ncol(df))] # gets rid of some unwanted columns

cf = read.table( paste(chr_data_dir, "cancer-freq.txt", sep="/"), header=T, sep=" ", row.names=1)

#comb = cbind(df,cf)
#vcols = c(1:5)
#ccols = c(6:ncol(comb))

## These are likely errors. The FASTA files show these as UNKNOWN sequence so no way there can be mutations called
## To note, most of these are found in COAD. Removing WU mutation files gets rid of most of these
unknown_fragments = df[unk,]
unk_cancers = cf[unk,]
colSums(unk_cancers)

## Remove those for now for comparison with normal
bins = which(gcf$UnkRatio < 1)
nrow(df)
df = df[bins,] 
cf = cf[bins,]
gcf = gcf[bins,]
nrow(df)

par(mfrow=c(2,3))
for (var in colnames(df))
  {
  plot(df[[var]], type='h', ylab=paste(var, "counts", sep=" "), main="chr10")
  }


snvCutoff = 1
snvCutoffUpper = 29
snvBump = gdvd[gdvd$SNV > snvCutoff & gdvd$SNV < snvCutoffUpper ,]

q = quantile(gdvd$GCRatio)

low = gdvd[gdvd$GCRatio <= 0.3,]  
cor.test(low$SNV, low$GCRatio, methods="pearson")
range(low$SNV)
median(low$SNV)
table(low$SNV)

# High GC *seems* to be more frequent in the fragments with large numbers of SNVs  
high = gdvd[gdvd$GCRatio >= 0.6,]
cor.test(high$SNV, high$GCRatio, methods="pearson")
range(high$SNV)
median(high$SNV)
table(high$SNV)
