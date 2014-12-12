source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")

args <- commandArgs(trailingOnly = TRUE)

#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/GA/HCC1954.G31860/3q12-8p11"
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/GA/HCC1954.G31860/6p23-12q13"
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random2/HCC1954.G31860/3p21-10p15"
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random/HCC1954.G31860/10q21-15p12"
#args[2] = "/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam"

#args[2] = "/Volumes/exHD-Killcoyne/TCGA/sequence/patients/c2047dc9-9361-47e4-90d2-abc17efba81f/TC#GA-AB-2977-03A-01D-0739-09_whole.bam"

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
    savePlots=T,
    addToSummary = c('model') )
  } else {
    print(args[2])
    orig = sampleReadLengths(args[2])
    distances = orig$dist
    phred = orig$phred
    mapq = orig$mapq
    cigar = orig$cigar
    
    print(summary(distances))
    distances = distances[which(distances < median(distances)*2)]
    summary = analyze.reads(paste(args[1],read_file,sep="/") , mean(distances), sd(distances), mean(phred) )
  }

write.table(summary$score, file=paste(args[1], "score.txt", sep="/"), quote=F, col.name=F, row.name=F)
save(summary, file=paste(args[1], "summary.Rdata", sep="/"))



