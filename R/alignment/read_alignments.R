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

bam_files = list.files(path=args[1], recursive=T, pattern="bam$", full.names=T)

if (length(args) < 2)
  stop("Missing required arguments: <bam> <normal.txt>")

normal = read.normal.txt(args[2], c("mean.dist","sd.dist","mean.phred","sd.phred","read.len"))

if (length(bam_files) <= 0)
  stop(paste("No bam files found in path:", args[1]))

for (bam in bam_files)
  {
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  
  referenceData = getRefData(reader)

  current_dir = dirname(bam)
  outdir = paste("/tmp", basename(dirname(current_dir)), basename(current_dir), sep="/")
  dir.create(outdir, recursive=T)
  cols = c('readID', 'pos','mate.pos','len','phred','mapq','cigar', 'cigar.identity', 'orientation','ppair')

  tmp_file = paste(outdir, "paired_reads.txt", sep="/")
  fout = file(tmp_file, "w")
  writeLines(paste(cols, collapse="\t"), fout)
  
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
  bamClose(reader)
  flush(fout)
  close(fout)
  copied = file.copy(tmp_file, paste(current_dir, "paired_reads.txt", sep="/"), overwrite=T)
  if (!copied)
    stop(paste("Failed to write or move", tmp_file))

  file.remove(tmp_file)
  print(paste(bam, "total reads:", nreads))
  }

