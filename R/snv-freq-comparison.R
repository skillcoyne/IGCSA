rm(list=ls())

setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")

data_file = "~/Data/VariationNormal/Frequencies/1000/Ensembl/chr19.txt"
gc_file = "~/Data/VariationNormal/GC/1000/chr19-gc.txt"

syn_file = "~/Data/Insilico/19mutations.txt"

data = load.data(gc_file, data_file)
df = data$vars

sf = read.table(syn_file, header=T, sep="\t")

t = table(sf$Fragment, sf$Variation)

#  This might suggest that I need more randomness in my variations.  Need to look at more genomes/chromsomes
#par(mfrow=c(1,2))
plot(t[, 'SNV'], type='h', col='red', , xlab="SNV count", ylab="1kb fragments", main="Chromosome 19")
lines(df[,'SNV'], type='h', col='blue')
#legend('topright', legend=c("Real", "Insilico"), fill=c('blue', 'red'), bty='n')

syn_freq = table(t[,'SNV'])
d_freq = table(df[, 'SNV'])


plot(d_freq, type='o', pch=8, col='blue', lty=5,
     main="SNV Frequency Across Chromosome 19", ylab='Frequency count', xlab='1kb fragments')
lines(syn_freq, type='o', pch=17, col='red', lty=3)
legend('topright', legend=c("Real", "Insilico"), col=c('blue', 'red'), pch=c(8,17), bty='n')