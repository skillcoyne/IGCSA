library('mclust')
library('rbamtools')
library('e1071')

kmeansAIC = function(fit)
{
  m = ncol(fit$centers)
  n = length(fit$cluster)
  k = nrow(fit$centers)
  D = fit$tot.withinss
  return(D + 2*m*k)
}


read.file<-function(file)
  {
  reads = NULL
  tryCatch({
    reads = read.table(file, header=T, sep="\t", comment.char="")
    reads$cigar = as.character(reads$cigar)
    reads$cigar.total = cigar.len(reads$cigar)
    reads$orientation = as.character(reads$orientation)
    return(reads) 
  }, error = function(err) {
    print(paste("Failed to read file", file))
    warning(err)
    cat(paste("Failed to read file", file), file="errors.txt", sep="\n", append=T)
    cat(paste(err, collapse="\n"), file="errors.txt", append=T)
  }, finally = {
    return(reads)
  })
  }

create.summary.obj<-function()
  {
  summary = list()
  for (n in c("name", "score", 'scored', 'total.reads', "cigar", "distance", "phred", "filtered.reads", "orientation", "estimated.dist", 
              "sum.l.prob", "sum.r.prob", "l.orientation", "r.orientation", "l.kurtosis", "r.kurtosis", "n.left.reads", "n.right.reads",
              "l.dens","r.dens",'l.shapiro', 'r.shapiro'))
    summary[[n]] = NA
  
  return(summary)
  }

analyze.reads<-function(file, normal.mean=NULL, normal.sd=NULL, normal.phred=0, savePlots=T, addToSummary = NULL)
  {
  summary = create.summary.obj()
  summary[['score']] = 0

  ## Left mean should be near the mean of the normal distance
  score_dist = TRUE
  
  if (is.null(normal.mean) | is.null(normal.sd))
    stop("A normal mean and stdev is required for read-pair distance analysis")
  
  path = dirname(file)
  name = basename(path)
  summary[['name']] = name
  print(path)
  
  reads = read.file(file)
  
  summary[['total.reads']] = nrow(reads)
  
  summary[['cigar']] = summary(reads$cigar.total)
  reads = reads[reads$cigar.total > 0,]
  
  summary[['distance']] = summary(reads$len)
  summary[['phred']] = summary(reads$phred)
  reads = reads[reads$phred >= normal.phred,] ## from the original 'good' reads

  summary[['filtered.reads']] = nrow(reads)
  summary[['orientation']] = table(reads$orientation)
  
  counts = log(reads$len)
  model = find.distributions(counts, "V")
  if (model$G != 2) score_dist = FALSE
  
  summary[['estimated.dist']] = model$G
  #model = Mclust(counts, modelNames="V", G=num_dist)
  #model = getMixtures(log(reads$len), "V")

  if (model$G == 2)
    {
    rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
    lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))

    summary[['sum.l.prob']] = sum(model$z[,lt])
    summary[['sum.r.prob']] = sum(model$z[,rt])
  
    left_mean = model$parameters$mean[lt]
    lv = model$parameters$variance$sigmasq[lt] 
    
    ## STOP RIGHT HERE
    if (left_mean > log(normal.mean+normal.sd*4)) score_dist = FALSE
    
    right_mean = model$parameters$mean[rt]
    rv = model$parameters$variance$sigmasq[rt] 
    
    leftD = reads[ counts >= (left_mean-lv*2) & counts <= (left_mean+lv*2) ,]
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
    }
  
  
  if (savePlots)
    {
    png_file=paste(path, "read_pair_distance.png", sep="/")
    print(png_file)
    png(filename=png_file, width=800, height=600)
    }

  
  #hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(min(counts),max(counts)), xlab="log(read-pair distance)", main=name, sub=paste("Score?", score_dist))
    #d = density(counts, kernel="gaussian")
  #lines(d, col="blue", lwd=2)
  
    #lrows = which(d$x >= (left_mean-lv) & d$x <= (left_mean+lv))
    #rrows = which(d$x >= (right_mean-rv) & d$x <= (right_mean+rv))

    #dens = densityMclust(counts, G=num_dist)
    plotDensityMclust1(model, data=counts, col='blue', lwd=2, hist.col = "lightblue",  breaks=100, xlab="log(read-pair distance)")
    title(name,sub=paste("Score?", score_dist))
  
    abline(v=log(normal.mean), col='red',lwd=2)
    text(log(normal.mean), max(model$density)/3, labels=paste("Sampled normal mean:",round(log(normal.mean),2)), pos=4)
    for (i in 1:ncol(model$z))
      { 
      m = model$parameters$mean[i]
      v = model$parameters$variance$sigmasq[i] 
      abline(v=m,lwd=2, col='blue')
      text(m, sd(model$density)+mean(model$density), labels=paste("mean:",round(m,2)), pos=2)

      if (model$G == 2)
        {
        kt = ifelse (i == lt, kurtosis(log(leftD$len)), kurtosis(log(rightD$len)) )
        text(m, (sd(model$density)/2)+mean(model$density), labels=paste("kurtosis:",round(kt, 3)), pos=2  )
        }
      
      if (score_dist) text(m, mean(model$density), labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2)
      }
    
    if (savePlots) dev.off()

  if (model$G == 2)
    {  
    # can't use mclust density here because there are just too many observations, the standard density function compresses the data points
    d = density(counts, kernel="gaussian")
    
    lrows = which(d$x >= (left_mean-lv) & d$x <= (left_mean+lv))
    rrows = which(d$x >= (right_mean-rv) & d$x <= (right_mean+rv))
    
    summary[['l.dens']] = max(model$density[lrows])
    summary[['l.shapiro']] = shapiro.test(d$x[lrows])

    #ks.test(d$x[lrows], pnorm, mean(d$x[lrows]), sd(d$x[lrows]))
    
    summary[['r.dens']] = max(model$density[rrows])
    summary[['r.shapiro']] = shapiro.test(d$x[rrows])
    
    #ks.test(d$x[rrows], pnorm, mean(d$x[rrows]), sd(d$x[rrows]))
    
    summary[['score']] = ifelse (score_dist, round(mean(model$z[,rt]),4 ), 0) 
    summary[['scored']] = score_dist
    }
  
  if ( !is.null(addToSummary) )
    {
    if ( length(grep('model', addToSummary)) > 0) summary[['model']] = model
    if ( length(grep('reads', addToSummary)) > 0) summary[['reads']] = reads
    }
  summary[['scored']] = score_dist
  
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

find.distributions<-function(dt, modelName="V")
  {
  ## cheap way to find how many real distributions may be there, more than 2 is a problem
  mod1 = densityMclust(dt, kernel="gaussian", modelNames=modelName)
  #g = 2
  #repeat
  #  {
    centers = list()
    i = 1
  #  print(mod1$G)    
    
    repeat
      {
      if (i > length(mod1$parameters$mean)+1) break
      m = mod1$parameters$mean[i]
      items = which(mod1$parameters$mean >= m - sd(mod1$data) & mod1$parameters$mean < m + sd(mod1$data))
      if (length(items) > 0)
        centers[[i]] = mod1$parameters$mean[items]
      i = ifelse(length(items) > 0, max(items)+1, i+1)
      }
    g = length(which(lapply(centers, length) > 0))
    #if (mod1$G == g | mod1$G == 1) break
    #print(g)    
    
    mod1 = densityMclust(counts, kernel="gaussian", G=g)
    #}
  
  return(mod1)
  }

#getMixtures<-function(vv, modelName="E")
#  {
#  cutoff = (max(vv)-min(vv))/2
#  z = matrix(0,length(vv),2) 
#  z[,1] = as.numeric(vv >= cutoff)
#  z[,2] = as.numeric(vv < cutoff)
#  msEst = mstep(modelName, vv, z)
#  modelName = msEst$modelName
#  parameters = msEst$parameters
#  em(modelName, vv, parameters)
#  }

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


