source("~/workspace/IGCSA/R/alignment/lib/utils.R")
source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")


args <- commandArgs(trailingOnly = TRUE)

#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/GA/HCC1954.G31860/3q12-8p11"
#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/Random/HCC1954.G31860/10q21-15p12"
#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/SH-SYS5/SH-SY5Y/2p15-9q34"

band_pair="8q21-15q15"
band_pair = "10p14-9q21"
band_pair = "2p23-4p16"
band_pair = "4q22-Xq21"
testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/PatientBPs"
#args[1] = paste(testDir, "KIRC-No-Sim", band_pair, sep="/")
#args[1] = paste(testDir, "KIRC-Patient", band_pair, sep="/")
#args[1] = paste(testDir, "BRCA-Patient", band_pair, sep="/")
#args[1] = paste(testDir, "8-15", band_pair, sep="/")
#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/SH-SYS5/SH-SY5Y/1q11-1q44"

#args[2] = paste(testDir, "KIRC-Patient/kirc.normal.txt", sep="/")
#args[2] = paste(testDir, "BRCA-Patient/brca.normal.txt", sep="/")
#args[2] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/SH-SYS5/SH-SY5Y/sh-sy5y-normal.txt"

if (length(args) < 2)
  stop("Missing required arguments: <directory to read in> <normal txt file>")

read_file = list.files(path=args[1], pattern="*paired_reads.txt", recursive=T, full.names=T)
if (length(read_file) <= 0)
  stop(paste("No paired_reads.txt file in", args[1]))
#summary = analyze.reads(paste(args[1],read_file,sep="/") , mean(distances), sd(distances), mean(phred) )
summary=NULL

print(args)

if (is.na(args[2])) {
  # Good for HCC1954 only
  print("HCC1954...")
  summary = analyze.reads(
    file=list.files(path=args[1], pattern="*paired_reads.txt", recursive=T, full.names=T),
    normal = as.data.frame(t(matrix(c(318.5, 92.8, 3097, 300, 100), dimnames=list( c('mean.dist','sd.dist','mean.phred','sd.phred', 'read.len'))))),
    savePlots=T,
    addToSummary = c('model','reads') )
  } else {
    simd = F
    if (!is.na(args[3])) simd = T
    
      summary = analyze.reads(
            file=list.files(path=args[1], pattern="*paired_reads.txt", recursive=T, full.names=T), 
            normal=read.normal.txt(args[2], c("mean.dist","sd.dist","mean.phred","sd.phred","read.len")),
            simReads = simd,
  			    savePlots=T,
  			    addToSummary = c('model','reads') )
  }

write.table(summary$score, file=paste(args[1], "score.txt", sep="/"), quote=F, col.name=F, row.name=F)
print(paste("Saving summary objects to", args[1]))
save(summary, file=paste(args[1], "summary.Rdata", sep="/"))



