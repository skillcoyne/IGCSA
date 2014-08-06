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
  dc = vector(mode="integer")
  
  align <- getNextAlign(brg)
  while(!is.null(align))
  {
    if (!unmapped(align) & !mateUnmapped(align) & insertSize(align) != 0) 
    {
      #message( paste(name(align), position(align), sep=" ") )
      
      if ( paired(align) & !failedQC(align) & !pcrORopt_duplicate(align) )
      {
        if (properPair(align) & !secondaryAlign(align))  
          pp = append( pp, abs(insertSize(align)) )
        else if ( refID(align) == mateRefID(align) ) 
          dc = append( dc, abs(insertSize(align)) ) # inter-chr
      }
    }
    align = getNextAlign(brg)
  }
  return( list("ppair" = pp, "disc" = dc)  )
}

sample_alignments<-function(rdr, coords, window=5000)
{
  start = sample( c(coords[2]:coords[3]), 1)
  end = start+window
  range = bamRange(rdr, c(coords[1], start, end) )
  
  iters = 1;
  while (size(range) <= 500) #probably that's even too small
  {
    start = sample( c(coords[2]:coords[3]), 1)
    end = start+window
    range = bamRange(rdr, c(coords[1], start, end) )
    iters = iters+1
  }
  #print(range)
  #message(paste(iters, "iterations were required to reach a range with size > 500.", sep=" "))
  raln = read_alignment(range)
  return(raln)
}

run_test<-function(iters=10, reader, coords, window=5000)
{
  counts = matrix(ncol=2, nrow=iters, dimnames=list(c(1:iters),c('ppair','disc')))
  means = matrix(ncol=6, nrow=iters, dimnames=list(c(1:iters), c('ppair.mean', 'ppair.sd', 'ppair.ext.mean', 'ppair.ext.sd','disc.mean', 'disc.sd')))
  extremes = vector(mode='numeric')
  for (i in 1:nrow(means) )
  {
    aln = sample_alignments(reader, coords, window)
    #qq = quantile(aln$ppair)
    
    outliers = aln$ppair[aln$ppair > 1000]
    if (length(outliers) > 0) 
    {
      aln$ppair = aln$ppair[-which(aln$ppair > 1000)]
      extremes = c(extremes, outliers)
    }
    
    counts[i,] = c( length(na.omit(aln$ppair)), length(na.omit(aln$disc)) )
    means[i,] = c(mean(aln$ppair), sd(aln$ppair), mean(extremes), sd(extremes), mean(aln$disc), sd(aln$disc)   )  
    #print(means)
  }
  return( list("means" = means, "counts" = counts) )
  #return(means)
}
