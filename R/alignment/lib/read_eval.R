library('mclust')
library('rbamtools')
library('e1071')
library('data.table')

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
    if (file.info(file)$size > 1000000000) {
      reads = fread(file,  header=T, sep="\t", showProgress=T, stringsAsFactors=F) } else {
        reads = read.table(file, header=T, sep="\t", comment.char="", stringsAsFactors=F)
      }
    #reads$orientation = as.character(reads$orientation)
    #reads$cigar = as.character(reads$cigar)
    
    if ( length(which(colnames(reads) == 'cigar.total')) == 0 )
      reads$cigar.total = cigar.len(reads$cigar)
  
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

analyze.reads<-function(file, normal, savePlots=T, addToSummary = NULL)
  {
  if (is.null(file))
    stop("Missing read file.")
  
  '%nin%' = Negate(`%in%`)
  if (is.null(normal) || 
        length(which(c("mean.dist","sd.dist","mean.phred","sd.phred","read.len") %nin% colnames(normal))) > 0 )
    stop("Missing object that includes normal values: mean.dist, sd.dist, mean.phred, sd.phred, read.len")
  
  summary = create.summary.obj()
  summary[['score']] = 0

  ## Left mean should be near the mean of the normal distance
  score_dist = TRUE
  
  path = dirname(file)
  name = basename(path)
  summary[['name']] = name
  #print(path)
  
  reads = read.file(file)
  dupd = which(duplicated(reads$readID))
  if (length(dupd) > 0)
    {
    reads = reads[-dupd,]
    rm(dupd)
    }
  print(nrow(reads))
  
  # adjust to get rid of low quality alignments where they are "correct"
  reads = reads[-which(reads$ppair & reads$mapq < 30),]
  reads = reads[-which(reads$len <= normal$mean.dist+normal$sd.dist*2 & reads$mapq < 30),]
  
  summary[['total.reads']] = nrow(reads)
  
  summary[['distance']] = summary(reads$len)
  summary[['phred']] = summary(reads$phred)
  reads = reads[reads$phred >= (normal$mean.phred-normal$sd.phred),] ## from the original 'good' reads
  
  #if (is.null(reads$cigar.identity))
  reads$cigar.identity =  percent.identity(reads$cigar, normal$read.len)
  reads = reads[reads$cigar.identity >= 0.5,]

  summary[['filtered.reads']] = nrow(reads)
  summary[['orientation']] = table(reads$orientation)
  
  counts = log(reads$len)
  #model = find.distributions(counts, log(normal$mean.dist+normal$sd.dist*4), "V")  # this isn't working so well
  model = densityMclust(counts, kernel="gaussian", modelNames="V", G=2)
  
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
    #if (left_mean > log(normal.mean+normal.sd*4)) score_dist = FALSE
    
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

    plotDensityMclust1(model, data=counts, col='blue', lwd=2, hist.col = "lightblue",  breaks=100, xlab="log(read-pair distance)")
    title(name,sub=paste("Score?", score_dist))
  
    abline(v=log(normal$mean.dist), col='red',lwd=2)
    text(log(normal$mean.dist), max(model$density)/3, labels=paste("Sampled normal mean:",round(log(normal$mean.dist),2)), pos=4)
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

## HERE IS THE PROBLEM -- I'm not sure I'm determining the clusters correctly.  Should I instead take everthing that is > 4sd from the mean and call it??  
## That would change the scoring function pretty drastically.  I also clearly can't just assume 2 clusters, but I also can't dismiss those that have more than 2
find.distributions<-function(dt, disc, modelName="V")
  {
  ## cheap way to find how many real distributions may be there, more than 2 is a problem
  mod1 = densityMclust(dt, kernel="gaussian", modelNames=modelName)
  #plotDensityMclust1(mod1, data=dt, col='blue', lwd=2, hist.col = "lightblue",  breaks=100, xlab="log(read-pair distance)")
  
  centers=list()
  means = mod1$parameters$mean
  ## First distribution should fall below the definition for discordant reads
  centers[[1]] = means[means < disc]
  i = length(centers[[1]])+1
  
  #max_i = which(round(mod1$parameters$mean) > round(max(mod1$data)-sd(mod1$data)/2))
  
  repeat
    {
    if (i >= mod1$G ) break
    m = mod1$parameters$mean[i]
    items = which(mod1$parameters$mean >= m - sd(mod1$data) & mod1$parameters$mean < m + sd(mod1$data))
    #items = items[-which(items == max_i)]
    #items = as.integer(names(which(items >= i)))

    if (length(items) > 0)
      centers[[i]] = mod1$parameters$mean[items]
    i = ifelse(length(items) > 0, max(items)+1, i+1)
    centers
    }
  g = length(which(lapply(centers, length) > 0))
  print(g)    

  mod1 = densityMclust(dt, kernel="gaussian", G=g)
  
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

percent.identity<-function(cigar, length)
  {
  length = (length)+1
  return (unlist(lapply(cigar, function(xs) (length + sum(unlist(
    lapply(strsplit(unlist(strsplit(xs, ",")), ":"), function(x) ifelse(grepl("S|D", x[2]), as.integer(x[1])*-1, 0)))))/length )))
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

score.phred<-function(str)
{
  strtoi(charToRaw("A"), 16L)
  unlist(lapply(unlist(strsplit(str,"")), charToRaw))
  unlist(lapply(unlist(strsplit(str, "")), utf8ToInt)) 
}
