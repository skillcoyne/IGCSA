library('mclust')
library('rbamtools')
library('e1071')

analyze.reads<-function(file, normal.mean=NULL, normal.sd=NULL, normal.phred=0, savePlots=T, addToSummary = NULL)
  {
  if (is.null(normal.mean) | is.null(normal.sd))
    stop("A normal mean and stdev is required for read-pair distance analysis")
  
  path = dirname(file)
  name = basename(path)
  print(path)
  
  summary = list()
  reads = NULL
  tryCatch({
  reads = read.table(file, header=T, sep="\t", comment.char="")
  }, error = function(err) {
    print(paste("Failed to read file", file))
    warning(err)
    cat(paste("Failed to read file", file), file="errors.txt", sep="\n", append=T)
    cat(paste(err, collapse="\n"), file="errors.txt", append=T)
  })
  
  reads$cigar = as.character(reads$cigar)
  reads$cigar.total = cigar.len(reads$cigar)
  reads$orientation = as.character(reads$orientation)
  
  summary[['total.reads']] = nrow(reads)
  
  summary[['cigar']] = summary(reads$cigar.total)
  reads = reads[reads$cigar.total > 0,]
  #reads = reads[reads$cigar.total >= mean(reads$cigar.total)+sd(reads$cigar.total),] ## not sure about this
  
  
  summary[['distance']] = summary(reads$len)
  summary[['phred']] = summary(reads$phred)
  reads = reads[reads$phred >= normal.phred,] ## from the original 'good' reads

  summary[['filtered.reads']] = nrow(reads)
  summary[['orientation']] = table(reads$orientation)
  
  counts = log(reads$len)
  model = getMixtures(log(reads$len), "V")

  model$parameters$mean
  
  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))
  
  left_mean = model$parameters$mean[lt]
  ## Left mean should be near the mean of the normal distance
  score_dist = TRUE
  ## STOP RIGHT HERE
  if (left_mean > log(normal.mean+normal.sd*4)) score_dist = FALSE
  
  lv = model$parameters$variance$sigmasq[lt] 
  leftD = reads[ counts >= (left_mean-lv*2) & counts <= (left_mean+lv*2) ,]
  
  right_mean = model$parameters$mean[rt]
  rv = model$parameters$variance$sigmasq[rt] 
  rightD = reads[ counts >= (right_mean-rv*3),]
  
  summary[['l.orientation']] = table(leftD$orientation)
  summary[['r.orientation']] = table(rightD$orientation)

  summary[['l.kurtosis']] = kurtosis(log(leftD$len))
  summary[['r.kurtosis']] = kurtosis(log(rightD$len))
  
  if (score_dist)
    {
    summary[['n.left.reads']] = nrow(leftD)
    summary[['n.right.reads']] = nrow(rightD)
    }
  
  if (savePlots)
    {
    png_file=paste(path, "read_pair_distance.png", sep="/")
    print(png_file)
    png(filename=png_file, width=800, height=600)
    }
  
    hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(min(counts),max(counts)), xlab="log(read-pair distance)", main=name, sub=paste("Score?", score_dist))
    d = density(counts, kernel="gaussian")
    lines(d, col="blue", lwd=2)

    abline(0,0,v=log(normal.mean), col='red',lwd=2)
    text(log(normal.mean), max(d$y)/2+sd(d$y), labels=paste("Sampled normal mean:",round(log(normal.mean),2)), pos=4)
  
    for (i in 1:ncol(model$z))
      { 
      m = model$parameters$mean[i]
      v = model$parameters$variance$sigmasq[i] 
      abline(0,0,v=m,lwd=2)
      text(m, sd(d$y)+mean(d$y), labels=paste("mean:",round(m,2)), pos=2)

      kt = ifelse (i == lt, kurtosis(log(leftD$len)), kurtosis(log(rightD$len)) )
      text(m, (sd(d$y)/2)+mean(d$y), labels=paste("kurtosis:",round(kt, 3)), pos=2  )
      
      if (score_dist) text(m, mean(d$y), labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2)
      }
    
    if (savePlots) dev.off()

    #if (score_dist)
      #{
      #png_file=paste(path, "sub-dist-read-length.png", sep="/")
      #png(filename=png_file, width=800, height=600)
      #par(mfrow=(c(2,1)))
    
      #hist(leftD$len, breaks=20, main=paste("Left sub-distribution mean=", round(mean(leftD$len), 2), sep=""), xlab="Read insert-distance", col="lightgreen", border=F,sub=name)
      #hist(rightD$len, breaks=20, main=paste("Right sub-distribution mean=", round(mean(rightD$len), 2), sep=""), xlab="Read insert-distance", col="lightgreen", border=F,sub=name)
      #dev.off()
      #}
    #}
  
  d = density(counts, kernel="gaussian")
  lrows = which(d$x >= (left_mean-lv) & d$x <= (left_mean+lv))
  summary[['l.dens']] = max(d$y[lrows])
  summary[['l.shapiro']] = shapiro.test(d$x[lrows])

  rrows = which(d$x >= (right_mean-rv) & d$x <= (right_mean+rv))
  summary[['r.dens']] = max(d$y[rrows])
  summary[['r.shapiro']] = shapiro.test(d$x[rrows])
  
  summary[['score']] = ifelse (score_dist, round(mean(model$z[,rt]),4 ), 0) 
  summary[['scored']] = score_dist
  
  if ( !is.null(addToSummary) )
    {
    if ( length(grep('model', addToSummary)) > 0) summary[['model']] = model
    if ( length(grep('reads', addToSummary)) > 0) summary[['reads']] = reads
    }
  
  write.table(summary[['score']], file=paste(path, "score.txt", sep="/"), quote=F, col.name=F, row.name=F)
  save(model, summary, file=paste(path, "summary.Rdata", sep="/"))
  
  print(summary[['score']])
  return(summary)
  }


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
  
  return(list("dist"=distances, "phred"=phred, "mapq"=mapq, "cigar"=cigar, "orientation"=orientation))
  }


