library('mclust')
library('rbamtools')

row.gen<-function(df)
  {
  if (nrow(df) <= 0)
    return(matrix(nrow=1,ncol=12,data=NA))
  
  row = cbind(nrow(df), 
              mean(log(df$len)), 
              sd(log(df$len)),
              mean(df$phred),
              sd(df$phred),
              mean(df$mapq),
              sd(df$mapq),
              nrow( df[df$ppair == TRUE,] )/nrow(df),
              length(which(df$orientation == 'F:F')),
              length(which(df$orientation == 'F:R')),
              length(which(df$orientation == 'R:R')),
              length(which(df$orientation == 'R:F'))
  )
  return(row)
  }

right.dist<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  mean(model$z[, rightside]) 
  }

sub.dist.means<-function(model)
  {
  v = sort(model$parameters$mean)
  names(v) = c('left','right')
  return(v)
  }

right.param<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  return(list('mean' = model$parameters$mean[[rightside]], 'variance' = model$parameters$variance$sigmasq[[rightside]]))
  }

getMixtures<-function(vv, modelName="E")
  {
  cutoff = (max(vv)-min(vv))/2
  z = matrix(0,length(vv),2) 
  z[,1] = as.numeric(vv >= cutoff)
  z[,2] = as.numeric(vv < cutoff)
  msEst = mstep(modelName, vv, z)
  modelName = msEst$modelName
  parameters = msEst$parameters
  em(modelName, vv, parameters)
  }

cigar.len<-function(cv)
  {
  totals = lapply(cv, function(xs) sum( unlist(
    lapply(strsplit(unlist(strsplit(xs, ",")), ":"), 
           function(x) ifelse ( grepl("S|D", x[2]), as.integer(x[1])*-1, as.integer(x[1]))))
  ))
  return(unlist(totals))
  }

clipped.end<-function(xs)
  {
  cg = unlist(strsplit(xs, ","))
  end = 0
  if ( grepl("S", cg[1])  ) end = 1
  if ( grepl("S", cg[length(cg)]) ) end = end + 2
  return(end)
  }

sampleReadLengths<-function(bam, sample_size=10000)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  referenceData = referenceData[ referenceData$SN %in% c(1:22,'X','Y'),]
  
  phred = vector(length=0,mode='numeric')
  distances = vector(length=0, mode='numeric')
  mapq = vector(length=0, mode='numeric')
  cigar = vector(length=0, mode='numeric')
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
        cd = cigarData(align)
        cigar = c(cigar, cigar.len(paste(paste(cd$Length, cd$Type, sep=":"), collapse=',')))
        }
      align = getNextAlign(range)
      n = n+1  
      }
    }
  bamClose(reader)
  
  return(list("dist"=distances, "phred"=phred, "mapq"=mapq, "cigar"=cigar))
  }


