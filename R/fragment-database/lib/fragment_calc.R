countSNV<-function(df, nuc)
{
  seq = df[ df[,'ref.seq'] == nuc, 'var.seq' ]
  freqNuc = table(seq)
  # R thing I still don't get but this makes sure all of the other variations stay out
  freqNuc = freqNuc[freqNuc > 0]
  
  for (n in names(freqNuc))
  {
    if ( length(grep(",", n)) > 0  )
    {
      count = freqNuc[n]
      multNuc = strsplit(n, ",")
      if (length(multNuc) >= 1)
      {
        for (m in multNuc[1])
        {
          freqNuc[m] = freqNuc[m] + count
        }
        freqNuc[n] = NA
      }
    }  
  }
  freqNuc[nuc] = 0
  freqNuc = freqNuc[!is.na(freqNuc)]
  # always return A,C,G,T order
  freqNuc = freqNuc[order(names(freqNuc))] 
  return(freqNuc)
}

probSNV<-function(counts)
{ 
  probs = counts/sum(counts, na.rm=T)  
  probs = probs[order(names(probs))] 
  probs = round(probs[ !is.na(probs) ], 2)
  
  # make sure it all sums to 0
  if (sum(probs) > 0)
  {
    maxIndex = which(probs == max(probs))
    probs[maxIndex] = probs[maxIndex] + (1-sum(probs))
  }
  return(probs)  
}


sizeFreq<-function(var_table, maxbp)
{
  size = vector("numeric", length(maxbp))
  names(size) = maxbp
  size['10'] =  sum( var_table[ as.character(c(1:10)) ], na.rm=T )
  size['100'] = sum( var_table[ as.character(c(11:100)) ], na.rm=T )
  for (i in seq(100, max(maxbp)-100, 100) )
  {
    min=i;max=i+100;
    size[ as.character(max) ] = sum( var_table[ as.character(c(min:max)) ], na.rm=T)
  }
  
  size = size/sum(size, na.rm=T)
  size = round(size, 6)
  
  if (sum(size, na.rm=T) > 1)  size[1]  = size[1] + (1-sum(size, na.rm=T))
  if (sum(size, na.rm=T) < 1)  size[1] = size[1] + (1-sum(size, na.rm=T))
  
  return(size)
}


