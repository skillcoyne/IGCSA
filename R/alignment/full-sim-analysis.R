source("~/workspace/IGCSA/R/denovo_alignment/lib/utils.R")


plot.scores<-function(df,points=list(), colors=list(), min=0)
  {
  plot(df$score[order(df$score)], ylab='score', xlab='', col=df$type[order(df$score)], pch=20, ylim=c(min,max(df$score)))
  for (name in names(points))
    {
    rows = points[[name]]
    if (length(rows) > 0)
      {
      text(x=which(order(df$score) %in% rows), y=df$score[order(df$score)][which(order(df$score) %in% rows)], 
           labels=df[order(df$score),][which(order(df$score) %in% rows) , 'name'], pos=2)
      points(which(order(df$score) %in% rows), df$score[order(df$score)][which(order(df$score) %in% rows)], pch=21,col=colors[[name]], cex=2)
      }
    }
  
  mn = mean(df$score[df$score > 0])
  sd1 = sd(df$score[df$score > 0])
  
  abline(h=mn, col='blue', lty=3, lwd=2)
  text(x=40,y=mn, labels=paste("mean (-0)", round(mn,2)), pos=3)
  
  abline(h=mn+sd1, col='orange', lty=3, lwd=2)
  text(x=40,y=mn+sd1, labels=paste("+1sd (-0)", round(mn+sd1,2)), pos=3)
  
  abline(h=mn+sd1*2, col='red', lty=3, lwd=2)
  text(x=40,y=mn+sd1*2, labels=paste("+2sd (-0)", round(mn+sd1*2,2)), pos=3)
  
#  legend("topleft", legend = c(levels(df$type)), 
#         col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type)))), horiz = F)
  }

fpr<-function(a,total) { return ((a-1)/(a-1+total-1)) }

sd.fpr<-function(df, sds=c(1,1.5,2,2.5,3))
  {
  f = rep(NA, length(sds))
  names(f) = c(paste('nlog',sds,'SD',sep=""))
  
  for (sd in sds)
    {
    probs = -log(dnorm( df$score, mean(df$score), sd))
    top = df[which( probs >= mean(probs)+sd(probs)*sd ),]
    
    if ('KnownBP' %in% top$type)
      f[paste('nlog',sd,'SD', sep='')] = fpr(nrow(top), nrow(df))
    }
  return(f)
  }

km.fpr<-function(df, centers=NULL)
  {
  if (is.null(centers))
    {
    stdev = sd(df$score)
    centers = list( 
      "2" = c(mean(df$score)-stdev*1.5, mean(df$score)+stdev*1.5),
      "3" = c(mean(df$score)-stdev*1.5, mean(df$score), mean(df$score)+stdev*1.5),
      "4" = c(mean(df$score)-stdev*2,  mean(df$score)-stdev, mean(df$score)+stdev, mean(df$score)+stdev*2),
      "5" = c(mean(df$score)-stdev*2,  mean(df$score)-stdev, mean(df$score), mean(df$score)+stdev, mean(df$score)+stdev*2) )
    }
  f = rep(NA, length(centers))
  names(f) = c(paste('km',names(centers),sep=""))
  
  fit = list()
  
  for (center in centers) 
    {
    km = tryCatch({
      kmeans(df$score, center, iter.max=100)
    }, error = function(e) {
      kmeans(df$score, length(center), iter.max=100)
      warning(paste("km", length(center), "failed"))
      #NULL
    })
    
    fit[[length(center)]] = km
    
    if (!is.null(km))
      {
      top = df[which(km$cluster == which.max(km$centers)),]
      if ('KnownBP' %in% top$type)
        f[paste('km',length(center), sep='')] = fpr(nrow(top), nrow(df))
      }
    }
  return(list(f, fit))
  }

calculate.fpr<-function(df)
  {
  sdfpr = sd.fpr(df)
  kmfpr = km.fpr(df)[[1]]
  
  return( list('km'=kmfpr, 'sd'=sdfpr, cbind(c(sdfpr,kmfpr))) )
  }



bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")
bands = bands[,c('name','len','gene.count')]


testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments"


#full_sim = grep("[0-9]+-[0-9]+", list.files(paste(testDir, 'GA', test, sep="/")), value=T)

test = "Test02"
samples = grep("[0-9]+-[0-9]+", list.files(paste(testDir, 'PatientBPs', test ,sep="/")), value=T)
names(samples) = samples

samples = unlist(lapply(samples, function(s) length(list.files(path=paste(testDir, "GA", test, s, sep="/"), recursive=T, pattern="*.Rdata"))))
samples = samples[samples > 0]

all_samples = NULL
for (i in 1:length(samples))
  {
  sample=names(samples[i])
  print(sample)
  if (sample == "19-2") next
  
  random = create.score.matrix(dir=paste(testDir, "GA", test, sample, sep="/"), "Random", bands) 
  patients = create.score.matrix(paste(testDir, "PatientBPs",test, sample, sep="/"), "KnownBP", bands)
  if (is.null(patients)) next
  patients$type="Random"
  
  if (!is.null(random)) x = rbind(random,patients) else x = patients
  x = x[order(x$score),]

  pt = which(x$name == grep(
    paste(unlist(lapply(unlist(strsplit(sample, "-")), function(x) paste(x, "(p|q)[0-9]+", sep=""))), collapse="-"),
    patients$name, value=T) )
  if (nrow(x[pt,]) > 0) x[pt, 'type'] = "KnownBP"
  
  if (!is.null(x$right.in.span)) 
    x$score[which(x$right.in.span == 0)] = 0
  x = x[which(x$score > 0),]
  
  nrow(x)
  
  x$sample = sample
  if (is.null(all_samples)) all_samples = x  else  all_samples = rbind(all_samples,x ) 
  }

#save(all_samples, file=paste(testDir, paste(test,"full_sim.Rdata", sep="-"), sep="/"))

#load(file=paste(testDir, paste(test,"full_sim.Rdata", sep="-"), sep="/"))
unlist(lapply(table(all_samples$sample), function(x) x <=24))

all_samples$sample = as.factor(all_samples$sample)

fpr_rows = c(paste('nlog',c(1,1.5,2,2.5,3),'SD',sep=""), paste('km',c(2:5),sep=''))
sim_fpr = matrix(ncol=length(table(all_samples$sample)), nrow=length(fpr_rows), data=0, dimnames=list(fpr_rows, levels(all_samples$sample)))

par(mfrow=c(2,2))
for (sample in levels(all_samples$sample))
  {
  print(sample)
  x = all_samples[which(all_samples$sample == sample),]
  
  #TEST
  #x$score = x$emr+(x$max.pos.reads/x$n.right.reads)*1000
  
  x = x[order(x$score),]
  
  points=list()
  colors=list()
  
  pt = which(x$type == 'KnownBP')
  points=list("Correct"=pt)
  colors=list("Correct"='blue')
  
  #if (nrow(x[pt,]) > 0) wilcox[sample] = wilcox.test(x$score[-pt], x$score[pt])$p.value
  x$type=as.factor(x$type)
  #plot.scores(x,points,colors)
  
  sim_fpr[,sample] = calculate.fpr(x)[[3]]
  
  kfpr = km.fpr(x)
  if (!is.na(kfpr[[1]]['km4']))
    {
    #km = km = kmeans(x$score, kfpr[[2]][[which.min(kfpr[[1]])]])
    km = kfpr[[2]][[4]]
    palette(rainbow(length(km$centers)))
    
    filled.km.plot<-function(scores, fit)
      {
      plot(scores, col=fit$cluster, pch=19, type='h',cex.axis=1.5, xlab="",ylab="")
      
      colors = palette()
      for (i in order(-fit$center))
        {
        xx = c(which(fit$cluster == i), rev(which(fit$cluster == i)))
        yy = c(rep(0, fit$size[i]), rev(scores[which(fit$cluster == i)]))
        
        polygon(x=xx, y=yy, col= colors[i], border=NA  )  
        }
      legend('topleft', legend=paste(seq(1,4,1), " (", fit$size[order(-fit$center)], ")", sep=""), fill=colors[order(-fit$center)], border=NA)
    }
    
    #png(filename=paste("~/Desktop/FullSim", paste(sample, "score.png", sep="_"), sep="/"), width=800, height=600, units="px")
    filled.km.plot(x$score, km)
    title(main=sample, sub=paste("fpr=", round(kfpr[[1]][which.min(kfpr[[1]])], 3)), xlab="index", ylab='Score', cex.lab=1.5, cex.sub=1.5, cex.main=1.5)
    #dev.off()
    #points(pt, x$score[pt], pch=21, col='red', cex=2, lwd=2)
    #text(pt, x$score[pt], pch=21, col='black', labels=x[pt,'name'], pos=2)
    }
  }

#write.table(sim_fpr, sep="\t", quote=F, col.names=NA, file=paste(testDir, "fpr.txt", sep="/"))




nrow(all_samples)
(table(all_samples$sample))

km_fpr = sim_fpr[which(grepl("km", rownames(sim_fpr))),]
boxplot(t(km_fpr), ylim=c(0,max(km_fpr, na.rm=T)), border='blue',col='lightblue', pch=19, lwd=2,  cex.axis=1.5)
title(main="K-means FPR", xlab="centroids", ylab="FPR", cex.lab=1.5, cex.main=1.5)
abline(h=0.1, col='red',lwd=2,lty=3)

sd_fpr = sim_fpr[which(grepl("SD", rownames(sim_fpr))),]
boxplot(t(sd_fpr), ylim=c(-0.01,max(sd_fpr, na.rm=T)), ylab="FPR", border='blue',col='lightblue', pch=19, lwd=2, main="SD FPR")
abline(h=0.1, col='red', lty=2)


