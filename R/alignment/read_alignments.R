library('rbamtools')
source("~/workspace/IGCSA/R/alignment/lib/read_eval.R")
source("~/workspace/IGCSA/R/alignment/lib/utils.R")

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
#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/PatientBPs/10-9/10p14-9q21"
#args[2] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/PatientBPs/KIRC-Patient/kirc.normal.txt"
#args[1] = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/Random/HCC1954.G31860"


bam_files = list.files(path=args[1], recursive=T, pattern="bam$", full.names=T)

if (length(args) < 2)
  stop("Missing required arguments: <bam> <normal.txt>")

normal = read.normal.txt(args[2], c("mean.dist","sd.dist","mean.phred","sd.phred","read.len"))

if (length(bam_files) <= 0)
  stop(paste("No bam files found in path:", args[1]))

for (bam in bam_files)
  {
  #bai = paste(bam, "bai", sep=".")
  #load.index(reader, bai)
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  
  referenceData = getRefData(reader)

  current_dir = dirname(bam)
  cols = c('readID', 'pos','mate.pos','len','phred','mapq','cigar', 'cigar.identity', 'orientation','ppair')

  fout = file(paste(current_dir, "paired_reads.txt", sep="/"), "w")
  writeLines(paste(cols, collapse="\t"), fout)
  #write(cols, file=paste(current_dir, "paired_reads.txt", sep="/"), append=F, sep="\t", ncolumns=length(cols)) 
  
  if (nrow(referenceData) <= 0)
  	stop(paste("No reads in bam file:",bam))
  
  nreads = 1
  align = getNextAlign(reader)
  while(!is.null(align))
    {
    if (nreads %% 10000 == 0) 
	    {
	    flush(fout)	
	    print(paste(nreads, "reads"))
	    }
    # If read is aligned, and the Phred score is equal or greater than the mean normal
    if ( !unmapped(align) & !mateUnmapped(align) & abs(insertSize(align)) > 0 & !secondaryAlign(align) ) # &  sum(alignQualVal(align)) >= mean.phred )
      {
      cd = cigarData(align)
      cd = paste(paste(cd$Length, cd$Type, sep=":"), collapse=',')
      #cig_len = cigar.len(cd)
      identity = percent.identity(cd, normal$read.len)
      
      orient = paste(ifelse(reverseStrand(align), 'R','F'), ifelse(mateReverseStrand(align), 'R','F'), sep=":") 
      if (reverseStrand(align) & !mateReverseStrand(align))
        orient = ifelse( matePosition(align) < position(align), 'F:R', 'R:F') 
      
      writeLines( paste( c( name(align), 
                position(align), 
                matePosition(align), 
                abs(insertSize(align)),
                sum(alignQualVal(align)), 
                mapQuality(align),
                cd, 
                identity,
                orient,
                properPair(align) ), collapse="\t"), fout ) 
      }
    align = getNextAlign(reader) #getNextAlign(range)
      
    nreads = nreads + 1
    }
  flush(fout)
  close(fout)
  print(paste(bam, "total reads:", nreads))
  }
