source("~/workspace/IGCSA/R/alignment/lib/utils.R")

km.fit<-function(df)
  {
  stdev = sd(df$score)
  center = c(mean(df$score)-stdev*2,  mean(df$score)-stdev, mean(df$score)+stdev, mean(df$score)+stdev*2)
  km = tryCatch({
    kmeans(df$score, center, iter.max=100)
  }, error = function(e) {
    kmeans(df$score, length(center), iter.max=100)
    #warning(paste("km", length(center), "failed"))
    #NULL
  })
  return(km)
  }


plot.all<-function(df, points=list(), colors=list())
  {
  par(mfrow=c(4,5))
  for (col in colnames(df))
    {
    if (col %in% c("name", 'gene.count','bp', 'type', 'total.reads')) next
    
    if ( is.na(df[[col]]) ) next
    
    plot( (df[[col]]), col=df$type, pch=20, ylab=col,lwd=2, cex.lab=2, cex.main=2, cex.axis=2,cex=2)
      
    for (name in names(points))
      {
      rows = points[[name]]
      points(rows, df[[col]][rows], pch=21,col=colors[[name]], cex=2)
      }
    mx = which.max(df[['score']])
    points(mx, df[[col]][mx],pch=15,col='purple', cex=2)
    
    }
  plot.new()
  par(xpd=TRUE)
  legend("center", legend = c(levels(df$type), names(points)), 
         col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type))),rep(21, length(points))), horiz = F, cex=2)
  }

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
  
  legend("topleft", legend = c(levels(df$type)), 
         col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type)))), horiz = F)
  }

gene.density.plots<-function(df,points=list(), colors=list())
  {
  df$gcs = score.adjust(df)
  
  par(mfrow=c(2,2))
  plot(log(df$gene.count)/df$bp, col=df$type, main="Gene Density", pch=20)
  for (name in names(points))
    {
    rows = points[[name]]
    points(rows, log(df$gene.count[rows])/df$bp[rows], pch=21,col=colors[[name]])
    }
  
  plot(df$score, col=df$type,  main="Score", pch=20)
  for (name in names(points))
    {
    rows = points[[name]]
    points(rows, df$score[rows], pch=21, col=colors[[name]])
    }
  
  plot(df$gcs, col=df$type,  main="GD Adjusted score", pch=20)
  for (name in names(points))
    {
    rows = points[[name]]
    points(rows, df$gcs[rows], pch=21,col=colors[[name]])
    }
  
  plot.new()
  par(xpd=TRUE)
  legend("center", legend = c(levels(df$type), names(points)), 
         col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type))),rep(21, length(points))), horiz = F, cex=2)
  }

bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")
bands = bands[,c('name','len','gene.count')]


#args <- commandArgs(trailingOnly = TRUE)

testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments"

patient_sim = grep("(.*-Patient|HCC1954.G31860)$", list.files(paste(testDir,'Random',sep="/")), value=T)

samples = patient_sim

sample_scores_dir = paste("~/Dropbox/Work/score_analysis", sep="")
dir.create(paste("~/Dropbox/Work/score_analysis", sep=""), recursive=T)

for (sample in samples)
  {
  if ( length(list.files(paste(testDir, "Random", sample, sep="/"))) < 20 | length(list.files(paste(testDir, "PatientBPs", sample, sep="/"))) <= 0 )
    {
    warning(paste(sample, "lacks files in both GA and PatientBPs, skipping"))
    next
    }
  
  random = create.score.matrix(dir=paste(testDir, "Random", sample, sep="/"), "Random", bands) 
  patients = create.score.matrix(paste(testDir, "PatientBPs", sample, sep="/"), "KnownBP", bands)
  
  x = rbind(random,patients)
  
  ## NEW TEST
  if (!is.null(x$right.in.span)) 
    x$score[which(x$right.in.span == 0)] = 0
  x = x[which(x$score > 0),]
  
  nrow(x)
  x = x[order(x$score),]
  
  x$sample = sample
  if (!exists('all_samples')) {
    all_samples = x
  } else {
    all_samples = rbind(all_samples,x)
  }
  }
all_samples$sample = as.factor(all_samples$sample)
#save(all_samples, file=paste(testDir, 'PatientBPs', 'all_samples.Rdata', sep="/"))

par(mfrow=c(2,4))
wilcox=matrix(ncol=3,nrow=length(samples),dimnames=list(samples, c('p.value', 'top.count','known.cluster')))
load(file=paste(testDir, 'PatientBPs', 'all_samples.Rdata', sep="/"))
for (sample in levels(all_samples$sample))
  {
  x = all_samples[all_samples$sample == sample,]
  total = nrow(x)
  
  #x$score = (x$emr + (x$max.pos.reads/x$n.right.reads)*30)
  
  x = x[order(x$score),]
  pt = which(x$type == "KnownBP")
  
  points=list("Known"=pt)
  colors=list("Known"="blue")

  wilcox[sample, 'p.value'] = NA
  if (nrow(x[pt,]) > 0) wilcox[sample, 'p.value'] = wilcox.test(x$score[-pt], x$score[pt])$p.value

  x$type=as.factor(x$type)
  km = km.fit(x)
  if (is.null(km))
    km = kmeans(x$score, c(mean(x$score)-stdev*1.5, mean(x$score), mean(x$score)+stdev*1.5))

  #png(filename=paste("~/Desktop/Simulated", paste(sample, "score.png", sep="_"), sep="/"), width=800, height=600, units="px")
  palette(c('green', 'cyan','blue','purple'))
  plot(x$score, col=km$cluster, pch=19, main=sample, sub="Factor 3x" ,xlab="")
  points(pt, x$score[pt], pch=21, lwd=2, col='red', cex=2)
  text(pt,x$score[pt], labels=x$name[pt], pos=2)
  #dev.off()
  
  wilcox[sample, 'top.count'] = km$size[which.max(km$centers)]
  }


write.table(round(wilcox[sort(rownames(wilcox)),],3), quote=F, sep="\t", file="~/Desktop/Simulated/wilcox.txt"))
top_counts
#pc = prcomp(xs)
#group = as.numeric(all_samples$type)
#pdf(file=paste("~/Desktop/Simulated", "all_pca.pdf", sep="/"), onefile=T)
#for (col in 1:ncol(pc$x))
#  {
#  for (j in 1:ncol(pc$x))
#    {
#    if (j == col) next
#    plot(pc$x[,col], pc$x[,j], main="PCA", xlab=col, ylab=j, col=group,pch=20)
#    }
#  }
#dev.off()


