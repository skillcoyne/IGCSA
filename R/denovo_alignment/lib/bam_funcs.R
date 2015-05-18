library('rbamtools')

getBreakpointLoc<-function(bam)
  {
  reader = bamReader(bam)
  header = getHeader(reader)
  rs = refSeqDict(getHeaderText(header))
  items = unlist(strsplit(getHeaderText(rs), ","))
  if (length(items) < 2) stop("bam file does not include a breakpoint definition")

print(items[2])
  bp = regexpr("=(\\d+)\t", items[2], perl=T)
  st = as.numeric(attr(bp,'capture.start'))
  len = as.numeric(attr(bp,'capture.length'))
	print(substr(items[2],st,st+len))
  return(as.numeric(substr(items[2], st, st+len)))
  }



sampleReadLengths<-function(bam, sample_size=10000)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  referenceData = referenceData[grepl("[0-9]+|X|Y", referenceData$SN),]
  
  phred = vector(length=0,mode='numeric')
  distances = vector(length=0, mode='numeric')
  mapq = vector(length=0, mode='numeric')
  cigar = vector(length=0, mode='numeric')
  read_lens = vector(length=0,mode='numeric')
  orientation = vector(length=4,mode='numeric')
  names(orientation) = c('F:F','F:R','R:F','R:R')
  
  n = 0
  while (n < sample_size)
  {
    chr = referenceData[referenceData$ID == sample( referenceData$ID, 1),]
    start = sample( c(1:chr$LN), 1 )
    
    range = bamRange(reader, c(chr$ID, start, start+1000)) 
    align = getNextAlign(range)
    while(!is.null(align))
    {
      if ( properPair(align) & !failedQC(align) & !mateUnmapped(align) & !unmapped(align) & !secondaryAlign(align) & mapQuality(align) >= 20)
      {
        distances = c(distances, abs(insertSize(align)))
        phred = c(phred, sum(alignQualVal(align)))
        mapq = c(mapq, mapQuality(align))
        read_lens = c(read_lens, length(unlist(strsplit(alignSeq(align), ""))))
        
        cd = cigarData(align)
        cigar = c(cigar, cigar.len(paste(paste(cd$Length, cd$Type, sep=":"), collapse=',')))
        
        ## F:R is the expected orientation, but proper pairs are still correct with R:F so long as the position of F < position of R
        orient = paste(ifelse(reverseStrand(align), 'R','F'), ifelse(mateReverseStrand(align), 'R','F'), sep=":") 
        if (reverseStrand(align) & !mateReverseStrand(align))
          orient = ifelse( matePosition(align) < position(align), 'F:R', 'R:F') 
        
        orientation[orient] = orientation[orient] + 1 
      }
      align = getNextAlign(range)
      n = n+1  
    }
  }
  bamClose(reader)
  
  return(list("dist"=distances, "phred"=phred, "mapq"=mapq, "cigar"=cigar, "orientation"=orientation, "reads"=read_lens))
}

get_band_range<-function(bands, chr, band=NULL)
  {
  if (!is.data.frame(bands)) stop("Requires data frame with columns:'chr','band','start','end")
  
  chr = sub('chr', "",  chr) 

  if (!is.null(band))
    loc = bands[ which(bands$chr == chr & bands$band %in% band), c('band','start','end') ]
  else 
    loc = bands[which(bands$chr == chr,), c('band','start','end')]
  
  return(loc)
  }

read_alignment<-function(brg)  # takes bamRange
  {
  rewind(brg)
  pp = vector(mode="integer")
  ic = vector(mode="integer")
  dc = vector(mode="character")
  
  align <- getNextAlign(brg)
  while(!is.null(align))
  {
    if (!unmapped(align) & !mateUnmapped(align) ) 
      {
      #message( paste(name(align), position(align), sep=" ") )
      
      if ( paired(align) & !failedQC(align) & !pcrORopt_duplicate(align) )
        {
        if (properPair(align) & !secondaryAlign(align))  
          pp = append( pp, abs(insertSize(align)) )
        else if ( refID(align) == mateRefID(align) && insertSize(align) != 0) 
          ic = append( ic, abs(insertSize(align)) ) # inter-chr
        else if ( refID(align) != mateRefID(align) )
          dc = append( dc, mateRefID(align) ) # inter-chr
        }
      }
    align = getNextAlign(brg)
    }
  return( list("ppair" = pp, "disc" = dc, "inter" = ic)  )
  }


