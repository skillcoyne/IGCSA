source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")

args <- commandArgs(trailingOnly = TRUE)

#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random/HCC1954.G31860/12q13-5q15"

#args[2] = "/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam"

if (length(args) < 2)
  stop("Missing required arguments: <directory to read in> <original aligned bam: OPTIONAL>")


if (!is.null(args[2]))
  {
  orig = sampleReadLengths(args[2])
  distances = orig$dist
  phred = orig$phred
  mapq = orig$mapq
  cigar = orig$cigar
  
  print(summary(distances))
  print(summary(phred))
  }


read_file = list.files(path=args[1], pattern="*paired_reads.txt", recursive=T)
summary = analyze.reads(paste(args[1],read_file,sep="/") , mean(distances), sd(distances), mean(phred) )
