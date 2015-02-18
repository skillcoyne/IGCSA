library('mclust')
library('e1071')


kmeansAIC = function(fit)
  {
  m = ncol(fit$centers)
  n = length(fit$cluster)
  k = nrow(fit$centers)
  D = fit$tot.withinss
  return(D + 2*m*k)
  }

window.cluster<-function(rds, minLength = 12)
  {
  high = rds[ which(log(rds$len) > minLength),  ]
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

analyze.reads<-function(file, normal, savePlots=T, addToSummary = NULL)
  {
  if (is.null(file))
    stop("Missing read file.")
  
  if (is.null(normal))
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

  summary[['total.reads']] = nrow(reads)
  summary[['phred']] = summary(reads$phred)
  summary[['distance']] = summary(reads$len)
  
  reads = filter.reads(reads, maxLen=(normal$mean.dist+normal$sd.dist*4), minMapq=30, minPhred=(normal$mean.phred-normal$sd.phred) )
  
  #if (is.null(reads$cigar.identity))
  reads$cigar.identity =  percent.identity(reads$cigar, normal$read.len)
  reads = reads[reads$cigar.identity >= 0.5,]

  summary[['filtered.reads']] = nrow(reads)
  summary[['orientation']] = table(reads$orientation)
  
  counts = log(reads$len)
  #model = find.distributions(counts, log(normal$mean.dist+normal$sd.dist*4), "V")  # this isn't working so well
  model = densityMclust(counts, kernel="gaussian", modelNames="V", G=2)
  print(model$parameters$mean)
  
  if (model$G != 2) score_dist = FALSE
  
  summary[['estimated.dist']] = model$G

  if (model$G == 2) summary = dist.eval(model, reads, summary)
  
  if (savePlots)
    {
    png_file=paste(path, "read_pair_distance.png", sep="/")
    print(png_file)
    png(filename=png_file, width=800, height=600)
    }

  plot.mclust(model,counts,normal,summary)
  title(name)
      
  if (savePlots) dev.off()

  if ( !is.null(addToSummary) )
    {
    if ( length(grep('model', addToSummary)) > 0) summary[['model']] = model
    if ( length(grep('reads', addToSummary)) > 0) summary[['reads']] = reads
    }
  
  summary[['scored']] = score_dist
  
  summary = top.position.clusters(reads, minLength=log(normal$mean.dist+normal$sd.dist*4), summaryObj=summary)
  
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
  lv = model$parameters$variance$sigmasq[lt] 
  
  ## STOP RIGHT HERE
  #if (left_mean > log(normal.mean+normal.sd*4)) score_dist = FALSE
  
  right_mean = model$parameters$mean[rt]
  rv = model$parameters$variance$sigmasq[rt] 
  
  leftD = rds[ model$data >= (left_mean-lv*2) & model$data <= (left_mean+lv*2) ,]
  rightD = rds[ model$data >= (right_mean-rv*3),]
  
  summaryObj[['l.orientation']] = table(leftD$orientation)
  summaryObj[['r.orientation']] = table(rightD$orientation)
  
  summaryObj[['l.kurtosis']] = kurtosis(log(leftD$len))
  summaryObj[['r.kurtosis']] = kurtosis(log(rightD$len))
  
  #if (score_dist)
    {
    summaryObj[['n.left.reads']] = nrow(leftD)
    summaryObj[['n.right.reads']] = nrow(rightD)
    }
  
  
  lrows = which(model$data >= (model$parameters$mean[lt] - model$parameters$variance$sigmasq[lt]) &
    model$data <= (model$parameters$mean[lt] + model$parameters$variance$sigmasq[lt]))

  rrows = which(model$data >= (model$parameters$mean[rt] - model$parameters$variance$sigmasq[rt]) &
                         model$data <= (model$parameters$mean[rt] + model$parameters$variance$sigmasq[rt]))
  
  summaryObj[['l.dens']] = max(model$density[lrows])
  max = ifelse(length(lrows) > 5000, 5000, length(lrows))
  summaryObj[['l.shapiro']] = shapiro.test(model$data[sample(lrows, max)])
  
  summaryObj[['r.dens']] = max(model$density[rrows])
  max = ifelse(length(rrows) > 5000, 5000, length(rrows))
  summaryObj[['r.shapiro']] = shapiro.test(model$data[sample(rrows, max)])
  
  #summaryObj[['score']] = ifelse (score_dist, round(mean(model$z[,rt]),4 ), 0) 
  summaryObj[['score']] = round(mean(model$z[,rt]),4 )
  
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


plot.mclust<-function(model, data, normal, summaryObj)
  {
  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))
  
  plotDensityMclust1(model, data=data, col='blue', lwd=2, hist.col = "lightblue",  breaks=100, xlab="log(read-pair distance)")
  
  abline(v=log(normal$mean.dist), col='red',lwd=2)
  text(log(normal$mean.dist), max(model$density)/3, labels=paste("Sampled normal mean:",round(log(normal$mean.dist),2)), pos=4)
  for (i in 1:ncol(model$z))
    { 
    m = model$parameters$mean[i]
    v = model$parameters$variance$sigmasq[i] 
    abline(v=m,lwd=2, col='blue')
    text(m, sd(model$density)+mean(model$density), labels=paste("mean:",round(m,2)), pos=2)
    
    if (!is.null(summaryObj))
      {
      kt = ifelse (i == lt, summaryObj[['l.kurtosis']], summaryObj[['r.kurtosis']] )
      text(m, (sd(model$density)/2)+mean(model$density), labels=paste("kurtosis:",round(kt, 3)), pos=2  )
      }
    
    text(m, mean(model$density), labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2)
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

