library('rbamtools')
source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")

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
#args[1] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random/HCC1954.G31860"

bam_files = list.files(path=args[1], recursive=T, pattern="bam$", full.names=T)

if (length(bam_files) <= 0)
  stop(paste("No bam files found in path:", args[1]))

`%nin%` <- Negate(`%in%`) 

for (bam in bam_files)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)

  current_dir = dirname(bam)
  cols = c('readID', 'pos','mate.pos','len','phred','mapq','cigar', 'cigar.total', 'orientation','ppair')

  write(cols, file=paste(current_dir, "paired_reads.txt", sep="/"), append=F, sep="\t", ncolumns=length(cols)) 
  
  if (nrow(referenceData) <= 0)
	stop(paste("No reads in bam file:",bam))

  #range = bamRange(reader, c(chrRef$ID, start, start+window) )
  nreads = 1
  #align = getNextAlign(range)
  align = getNextAlign(reader)
  while(!is.null(align))
    {
    if (nreads %% 10000 == 0) print(paste(nreads, "reads"))
    if ( !unmapped(align) & !mateUnmapped(align) & abs(insertSize(align)) > 0)
      {
      cd = cigarData(align)
      cd = paste(paste(cd$Length, cd$Type, sep=":"), collapse=',')
      cig_len = cigar.len(cd)
      
      orient = paste(ifelse(reverseStrand(align), 'R','F'), ifelse(mateReverseStrand(align), 'R','F'), sep=":") 
      if (reverseStrand(align) & !mateReverseStrand(align))
        orient = ifelse( matePosition(align) < position(align), 'F:R', 'R:F') 
      
      write( c( name(align), 
                position(align), 
                matePosition(align), 
                abs(insertSize(align)),
                sum(alignQualVal(align)), 
                mapQuality(align),
                cd, 
                cig_len,
                orient,
                properPair(align) ), 
             file=paste(current_dir, "paired_reads.txt", sep="/"), append=T, sep="\t", ncolumns=length(cols))
      }
    align = getNextAlign(reader) #getNextAlign(range)
      
    nreads = nreads + 1
    }
  print(paste("Total reads:", nreads))
  }
