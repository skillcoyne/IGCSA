
source("lib/bam_funcs.R")
source("lib/utils.R")
source("lib/read_eval.R")

args <- commandArgs(trailingOnly = TRUE)

if (length(args) < 1)
  stop("Missing BAM file argument.")
  
orig = sampleReadLengths(args[1])
distances = orig$dist
phred = orig$phred
mapq = orig$mapq
cigar = orig$cigar

print(summary(distances))
distances = distances[which(distances < median(distances)*2)]


outdir = dirname(args[1])
outfile = args[2]
print(paste(outdir, outfile, sep="/"))

m = matrix( c(mean(distances),sd(distances),mean(phred), sd(phred), mean(cigar), mean(orig$reads)), ncol=1,
            dimnames=list(c('mean.dist','sd.dist','mean.phred', 'sd.phred','cigar','read.len')) )

write.table(m, quote=F,sep="\t",col.names=F, file=paste(outdir, outfile, sep="/"))


print(paste("Mean dist:",mean(distances)))
print(paste("SD dist:", sd(distances)))
print(paste("Mean phred:",mean(phred)))
print(paste("SD phred:",sd(phred)))
print(paste("CIGAR:", mean(cigar)))
print(paste("Read lens:",mean(orig$reads)))
