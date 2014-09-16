library('rbamtools')

#source("lib/bam_funcs.R")

load_files<-function(files, dir)
{
  for (f in files)
  {
    f = paste(dir, f, sep="/")
    load(f)
    if (exists("means_var"))
      means_var = rbind(means_var, means)
    else
      means_var = means
    
    rownames(means_var) = c(1:nrow(means_var))
  }
  rm(means)
  return(means_var)
}


args <- commandArgs(trailingOnly = TRUE)
print(args)

args = c("/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.7/5q35-8q24")
bam_files = list.files(path=args[1], recursive=T, pattern="bam$", full.names=T)

`%nin%` <- Negate(`%in%`) 

bands=read.table("~/Analysis/band_genes.txt", header=T)

for (bam in bam_files)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  print(isOpen(reader))
  current_dir = dirname(bam)

  chrRef = referenceData[1,]
  
  #startPos = sample(1:(chrRef$LN-kb), 1)
  range = bamRange(reader, c(chrRef$ID, 1, chrRef$LN) )
  rewind(range)
  
  cols = c('dist','pos', 'mate.pos', 'phred', 'mapq', 'pp')
  is = matrix(ncol=length(cols),nrow=0,dimnames=list(c(), cols))
  
  current_dir = dirname(bam)
  sink(paste(current_dir, "paired_reads.txt", sep="/"))
  cat(paste(c('pos','mate.pos','len','phred','mapq','cigar','orientation','ppair'), collapse="\t"))
  cat("\n")
  
  align = getNextAlign(range)
  while(!is.null(align))
    {
    if ( !unmapped(align) & !mateUnmapped(align) & insertSize(align) > 0)
      {
      cd = cigarData(align)
      cat( paste(
          c(  position(align), 
              matePosition(align), 
              abs(insertSize(align)),
              sum(alignQualVal(align)), 
              mapQuality(align),
              paste(paste(cd$Length, cd$Type, sep=":"), collapse=','),
              paste(ifelse(reverseStrand(align), 'R','F'), ifelse(mateReverseStrand(align), 'R','F'), sep=":"),
              ifelse(properPair(align), '1','0') ) 
          , collapse = "\t") )
      cat("\n")
      
      }
    align = getNextAlign(range)
    }
  }
sink()

