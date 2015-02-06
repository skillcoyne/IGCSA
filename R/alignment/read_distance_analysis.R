source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")

args <- commandArgs(trailingOnly = TRUE)

#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/GA/HCC1954.G31860/3q12-8p11"
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random/HCC1954.G31860/10q21-15p12"
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/SH-SYS5/SH-SY5Y/2p15-9q34"

testDir = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments"
args[1] = paste(testDir, "PatientBPs/KIRC-Patient/10p14-9q21", sep="/")
args[2] = paste(testDir, "PatientBPs/KIRC-Patient/normal.txt", sep="/")

if (length(args) < 1)
  stop("Missing required arguments: <directory to read in> <original aligned bam: OPTIONAL>")

read_file = list.files(path=args[1], pattern="*paired_reads.txt", recursive=T)
#summary = analyze.reads(paste(args[1],read_file,sep="/") , mean(distances), sd(distances), mean(phred) )
summary=NULL


if (is.na(args[2])) {
  # Good for HCC1954 only
  print("HCC1954...")
  summary = analyze.reads(
    file=paste(args[1],read_file,sep="/"),
    normal.mean=318.5,
    normal.sd=92.8,
    normal.phred=3097,
    read.len=50,
    savePlots=T,
    addToSummary = c('model') )
  } else {
    print(args[2])
 
    normal = read.table(args[2], header=F, row.names=1)   
   
#    print(summary(distances))
#    distances = distances[which(distances < median(distances)*2)]
    summary = analyze.reads(
          file=paste(args[1],read_file,sep="/"), 
			    normal.mean=normal['mean.dist',], 
			    normal.sd=normal['sd.dist',], 
			    normal.phred=normal['mean.phred',],
          read.len=normal['read.len',],
			    savePlots=T,
			    addToSummary = c('model','reads') )
  }

write.table(summary$score, file=paste(args[1], "score.txt", sep="/"), quote=F, col.name=F, row.name=F)
save(summary, file=paste(args[1], "summary.Rdata", sep="/"))



