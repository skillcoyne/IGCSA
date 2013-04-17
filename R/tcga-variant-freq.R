source("lib/gc_functions.R")

gc_file = "~/Data/VariationNormal/GC/1000/chr1-gc.txt"
gcf = gc.data(paste(gc_dir, "chr1-gc.txt", sep="/"))
unk = which(gcf$UnkRatio == 1)


df = read.table("~/Analysis/TCGA/chr1/freq.txt", header=T, sep=" ")
df = df[,c(3:ncol(df))] # gets rid of some unwanted columns

cf = read.table("~/Analysis/TCGA/chr1/cancer-freq.txt", header=T, sep=" ")

#comb = cbind(df,cf)
#vcols = c(1:5)
#ccols = c(6:ncol(comb))

## These are likely errors. The FASTA files show these as UNKNOWN sequence so no way there can be mutations called
## To note, most of these are found in COAD
unknown_fragments = df[unk,]
unk_cancers = cf[unk,]
colSums(unk_cancers)

## Remove those for now for comparison with normal
bins = which(gcf$UnkRatio < 1)
df = df[bins,] 
cf = cf[bins,]
gcf = gcf[bins,]

par(mfrow=c(2,3))
for (var in colnames(df))
  {
  plot(df[[var]], type='h', ylab=paste(var, "counts", sep=" "), main="chr1")
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
