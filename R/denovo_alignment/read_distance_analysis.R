source("lib/utils.R")
source("lib/read_eval.R")
source("lib/bam_funcs.R")

args <- commandArgs(trailingOnly = TRUE)

if (length(args) < 2)
  stop("Missing required arguments: <directory to read in> <normal txt file>")

read_file = list.files(path=args[1], pattern="*paired_reads.txt", recursive=T, full.names=T)
if (length(read_file) <= 0)
  stop(paste("No paired_reads.txt file in", args[1]))

bam_file = list.files(path=args[1], pattern="*.bam$", recursive=T, full.names=T) 
if (length(bam_file) <= 0) bam_file = NULL

#summary = analyze.reads(paste(args[1],read_file,sep="/") , mean(distances), sd(distances), mean(phred) )
summary=NULL

print(args)

simd = F
if (!is.na(args[3])) simd = T
    
summary = analyze.reads(
          file=list.files(path=args[1], pattern="*paired_reads.txt", recursive=T, full.names=T), 
          bam=bam_file,
					normal=read.normal.txt(args[2], c("mean.dist","sd.dist","mean.phred","sd.phred","read.len")),
          simReads = simd,
			    savePlots=T)
			    #addToSummary = c('model','reads') )

write.table(summary$score, file=paste(args[1], "score.txt", sep="/"), quote=F, col.name=F, row.name=F)
print(paste("Saving summary objects to", args[1]))
save(summary, file=paste(args[1], "summary.Rdata", sep="/"))



