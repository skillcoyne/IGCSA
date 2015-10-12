library('mclust')
library('e1071')



window.cluster<-function(reads, minLength = 12)
  {
  reads = as.data.frame(reads)
  high = reads[ which(log(reads$len) > minLength),  ]
  if (nrow(high) <= 0)
    stop("Cluster error")
  
  slide = 500
  window = 1000
  start = high[1, 'pos']
  
  windows =(matrix(ncol=4,nrow=0,dimnames=list(c(),c('start','end','ct','merged'))))
  while( start < high[nrow(high), 'pos'] )
    {
    rows = which(high$pos >= start & high$pos < start+window)
    if (length(rows) > 0)
      windows = rbind(windows, cbind(start,start+window,length(rows),0))
    
    start = start+slide
    }
  
  #windows$merged = 0
  for (i in 1:(nrow(windows)-1))
    {
    a = windows[i,]
    b = windows[i+1,]
    if ( (b[['ct']] <= a[['ct']] & b[['ct']]+100 >= a[['ct']]) & (b[['start']] > a[['start']] & b[['start']] < a[['end']]) )
      {
      windows[i:(i+1), 'merged'] = 1
      windows = rbind(windows, cbind(min(a[['start']],b[['start']]), max(a[['end']],b[['end']]), sum(a[['ct']],b[['ct']]), 0))
      }
    }
  windows = windows[ -which(windows[,'merged'] > 0), ]
  
  windows = windows[,c(1:3)] 
  
  return(as.data.frame(windows))
  }

# Reads, max distance, min map quality
filter.reads<-function(rds, maxLen, minMapq, minPhred)
  {
  # adjust to get rid of low quality alignments where they are "correct"
  rds = rds[-which(rds$ppair & rds$mapq < minMapq),]
  rds = rds[-which(rds$len <= maxLen & rds$mapq < minMapq),]
  
  rds = rds[rds$phred >= minPhred,] ## from the original 'good' reads
  
  return(rds)
}

analyze.reads<-function(file, normal, savePlots=T, bam=NULL, simReads=F, addToSummary = NULL)
  {
  if (is.null(file))
    stop("Missing read file.")
  
  if (is.null(normal))
    stop("Missing object that includes normal values: mean.dist, sd.dist, mean.phred, sd.phred, read.len")
  
  summary = create.summary.obj()
  summary[['score']] = 0

  print(summary[['breakpoint']]) 
  ## Left mean should be near the mean of the normal distance
  score_dist = TRUE
  
  path = dirname(file)
  name = basename(path)
  summary[['name']] = name
  #print(path)
  
  breakpoint = readLines(file, n=1)
  if (grepl("#breakpoint", breakpoint)) summary[['breakpoint']] = as.numeric(sub("#breakpoint=","", breakpoint))
  
  if (is.null(summary[['breakpoint']]) & !is.null(bam)) summary[['breakpoint']] = getBreakpointLoc(bam)
  
  reads = read.file(file)
  dupd = which(duplicated(reads$readID))
  if (length(dupd) > 0)
    {
    reads = reads[-dupd,]
    rm(dupd)
    }
  print(nrow(reads))

  summary[['total.reads']] = nrow(reads)
  summary[['phred']] = summary(reads$phred)
  summary[['distance']] = summary(reads$len)
  
  reads = filter.reads(reads, maxLen=(normal$mean.dist+normal$sd.dist*4), minMapq=30, minPhred=(normal$mean.phred-normal$sd.phred) )
  
  if (is.null(reads$cigar.identity) | length(which(is.na(reads$cigar.identity))) > 0)
    reads$cigar.identity =  percent.identity(reads$cigar, normal$read.len)
  reads = reads[reads$cigar.identity >= 0.5,]
  
  if (nrow(reads) <= 0)
    stop("All reads had low cigar")

  summary[['filtered.reads']] = nrow(reads)
  summary[['orientation']] = table(reads$orientation)
  
  # The other ones I tested both from patient and the cell line were no more than 15%
  if (simReads)
    {
    print("Sampling simulated reads")
    norm_rows = which( reads$len <= normal$mean.dist+normal$sd.dist*4 & reads$len >= normal$mean.dist-normal$sd.dist*2 ) 
    if (length(norm_rows)/nrow(reads) > 0.15)
      {
      remove = norm_rows[-sample(norm_rows, 0.15*nrow(reads))]
      reads = reads[-remove,]
      }
    }
  counts = log(reads$len)
  #model = find.distributions(counts, log(normal$mean.dist+normal$sd.dist*4), "E")  # this isn't working so well
  model = densityMclust(counts, kernel="gaussian", modelNames="V", G=2)
  if ( min(model$parameters$mean) >= log(normal$mean.dist+normal$sd.dist*4)  )
    model = densityMclust(counts, kernel="gaussian", modelNames="E", G=2)
  
  print(model$parameters$mean)
  
  if (model$G != 2) score_dist = FALSE
  
  summary[['estimated.dist']] = model$G

  if (model$G == 2) summary = dist.eval(model, reads, summary)
  
  if (savePlots)
    {
    png_file=paste(path, "read_pair_distance.png", sep="/")
    print(png_file)
    png(filename=png_file, width=1000, height=800, units="px")
    }

  plot.mclust(model,counts,normal,summary, cex=1.5,cex.lab=1.5,cex.axis=2)
  title(main=name)
      
  if (savePlots) dev.off()

  if ( !is.null(addToSummary) )
    {
    if ( length(grep('model', addToSummary)) > 0) summary[['model']] = model
    if ( length(grep('reads', addToSummary)) > 0) summary[['reads']] = reads
    }
  
  summary[['scored']] = score_dist
  
  summary = top.position.clusters(reads, minLength=log(normal$mean.dist+normal$sd.dist*4), summaryObj=summary)
  
  summary[['score']] = summary[['emr']]+(summary[['max.pos.reads']]/summary[['n.right.reads']])*10
  
  print(summary[['score']])
  return(summary)
  }

dist.eval<-function(model, rds, summaryObj)
  {
  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))
  
  summaryObj[['sum.l.prob']] = sum(model$z[,lt])
  summaryObj[['sum.r.prob']] = sum(model$z[,rt])
  
  left_mean = model$parameters$mean[lt]
  lv = ifelse (model$modelName == "V", model$parameters$variance$sigmasq[lt], model$parameters$variance$sigmasq)
    
  right_mean = model$parameters$mean[rt]
  rv = ifelse (model$modelName == "V", model$parameters$variance$sigmasq[rt], model$parameters$variance$sigmasq)
  
  ## Reads which pretty unambiguously fit in either 1st or 2nd distribution
  leftD = which(model$z[,lt] > 0.98)
  rightD = which(model$z[,rt] > 0.98)
  
  bp = summaryObj[['breakpoint']]
  if (!is.null(bp))
    {
    span = which(rds$pos <= bp & rds$mate.pos >= bp)
    summaryObj[['span']] = length(span)
    summaryObj[['right.in.span']] = length(which(rightD %in% span))
    }
  
  summaryObj[['l.orientation']] = table(rds[leftD,]$orientation)
  summaryObj[['r.orientation']] = table(rds[rightD,]$orientation)
  
  summaryObj[['l.kurtosis']] = kurtosis(model$data[leftD])
  summaryObj[['r.kurtosis']] = kurtosis(model$data[rightD])
  
  #if (score_dist)
    {
    summaryObj[['n.left.reads']] = length(leftD)
    summaryObj[['n.right.reads']] = length(rightD)
    }
  
  summaryObj[['l.dens']] = max(model$density[leftD])
  max = ifelse(length(leftD) > 5000, 5000, length(leftD))
  summaryObj[['l.shapiro']] = shapiro.test(model$data[sample(leftD, max)])
  
  summaryObj[['r.dens']] = max(model$density[rightD])
  max = ifelse(length(rightD) > 5000, 5000, length(rightD))
  summaryObj[['r.shapiro']] = shapiro.test(model$data[sample(rightD, max)])
  
  #summaryObj[['emr']] = ifelse (score_dist, round(mean(model$z[,rt]),4 ), 0) 
  summaryObj[['emr']] = round(mean(model$z[,rt]),4 )
  
  return(summaryObj)
  }


top.position.clusters<-function(reads, minLength, summaryObj)
  {
  clusters = window.cluster(reads, minLength)
  
  vm = vector(mode="numeric", length=20)
  for (i in 1:length(vm))
    vm[i] = kmeansAIC(kmeans(clusters$ct, i)) 
  km = kmeans(clusters$ct, which.min(vm))
  
  # poisson probabilities
  for (i in 1:nrow(clusters))
    clusters[i,'prob'] = dpois(round(log(clusters[i,'ct'])), mean(log(clusters$ct)))
  
  min_ct = sort(km$centers[order(-km$centers)][1:3])[1]
  
  summaryObj[['pos.cluster']] = clusters
  summaryObj[['top.pos.clusters']] =  clusters[clusters$ct >= min_ct, ]
  
  summaryObj[['max.pos.reads']] = max(summaryObj[['top.pos.clusters']]$ct)
  
  return(summaryObj)
  }

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


plot.mclust<-function(model, data, normal, summaryObj, ...)
  {
  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))
  
  plotDensityMclust1(model, data=data,  col='blue', lwd=2, hist.col = "lightblue",  breaks=100, xlab="log(read-pair distance)")
  
  abline(v=log(normal$mean.dist), col='red',lwd=2)
  text(log(normal$mean.dist), max(model$density)/3, labels=paste("Sampled normal mean:",round(log(normal$mean.dist),2)), pos=4, ...)
  for (i in 1:ncol(model$z))
    { 
    m = model$parameters$mean[i]
    v = ifelse (model$modelName == "V", model$parameters$variance$sigmasq[i], model$parameters$variance$sigmasq)
    abline(v=m,lwd=2, col='blue')
    text(m, sd(model$density)+mean(model$density), labels=paste("mean:",round(m,2)), pos=2, ...)
    
    if (!is.null(summaryObj))
      {
      kt = ifelse (i == lt, summaryObj[['l.kurtosis']], summaryObj[['r.kurtosis']] )
      #text(m, (sd(model$density)/2)+mean(model$density), labels=paste("kurtosis:",round(kt, 3)), pos=2  )
      }
    
    text(m, mean(model$density), labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2, ...)
    }
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

