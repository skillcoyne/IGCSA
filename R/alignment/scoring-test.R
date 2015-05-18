source("~/workspace/IGCSA/R/alignment/lib/utils.R")

score.adjust<-function(s)
{
  # gene density
  #g = s[['gene.count']])/s[['bp']]
  g =  log(s[['gene.count']]) / s[['bp']]
  
  #g = log(s[['gene.count']])/log(s[['bp']])
  
  gdx = g *1E6
  score = s[['score']]
  #((gdx^2)*score)
  (gdx^2)+score
}

open.pngs<-function(df, dir)
  {
  for (n in rownames(df))
  {
    cmd=paste("open", paste(dir,n,"read_pair_distance.png",sep="/"))
    system(cmd)
  }
}

plot.all<-function(df, points=list(), colors=list())
  {
  par(mfrow=c(4,5))
  for (col in colnames(df))
    {
    if (col %in% c("name", 'gene.count','bp', 'type', 'total.reads')) next
    
    if ( is.na(x[[col]]) ) next
    
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
    text(x=which(order(df$score) %in% rows), y=df$score[order(df$score)][which(order(df$score) %in% rows)], 
         labels=df[order(df$score),][which(order(df$score) %in% rows) , 'name'], pos=2)
    points(which(order(df$score) %in% rows), df$score[order(df$score)][which(order(df$score) %in% rows)], pch=21,col=colors[[name]], cex=2)
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

full_sim = c("2-4","10-9","4-X","8-15")

samples = c(full_sim, c('HCC1954.G31860','GBM', 'BRCA','OV','KIRC', 'LAML','LUAD', 'COAD'))
wilcox=matrix(ncol=1,nrow=length(samples),dimnames=list(samples, c('p.value')))

sim_fpr = matrix(ncol=length(full_sim), nrow=2, data=0, dimnames=list(c('3','4'), full_sim))
spike_ins = vector(mode='numeric', length(samples))
names(spike_ins) = samples

sample_scores_dir = paste("~/Dropbox/Work/score_analysis", sep="")
dir.create(paste("~/Dropbox/Work/score_analysis", sep=""), recursive=T)
cluster_spikein = list()
for (sample in samples)
  {
  print(sample)
  patient = ifelse (!sample %in% c(full_sim, 'HCC1954.G31860'), paste(sample, "Patient",sep="-"), sample)

  random = create.score.matrix(dir=paste(testDir, "Random", patient, sep="/"), "Random", bands) 
  
  patients = create.score.matrix(paste(testDir, "PatientBPs", patient, sep="/"), "KnownBP", bands)
  if (sample %in% full_sim) patients$type="Random"

  x = rbind(random,patients)

  total = nrow(x)
  
  ## NEW TEST
  if (!is.null(x$right.in.span)) 
    x$score[which(x$right.in.span == 0)] = 0
  
  x = x[which(x$score > 0),]
  
  nrow(x)
  pt = which(x$type == "KnownBP")
  
  points=list("Known"=pt)
  colors=list("Known"="blue")

  palette(c('red', 'green'))
  if (sample %in% full_sim)
    {
    if (sample == '8-15') {
      pt = which(x$name == "8q21-15q15")
    } else if (sample == '2-4') {
      pt = which(x$name == '2p23-4p16')
    } else if (sample == '4-X') {
      pt = which(x$name == '4q22-Xq21')
    } else if (sample == '10-9') {
      pt = which(x$name == '10p14-9q21')
    }
    
    x[pt, 'type'] = "KnownBP"
    points=list("Correct"=pt)
    colors=list("Correct"='blue')
    }
  rand = which(x$type == 'Random')

  wilcox[sample, 'p.value'] = wilcox.test(x$score[-pt], x$score[pt])$p.value
  x$type=as.factor(x$type)
  
  #plot(x$right.in.span,x$n.right.reads,col=x$type)
  #plot(x$score,col=x$type)

  png(filename=paste("~/Desktop/Simulated", paste(patient, "all.png", sep="_"), sep="/"), width=1600, height=1200, units="px")
  plot.all(x, points, colors)
  dev.off()

  png(filename=paste("~/Desktop/Simulated", paste(patient, "score.png", sep="_"), sep="/"), width=800, height=600, units="px")
  #plot.scores(x,points,colors)
  plot.scores(x,points,colors,min=0.8)
  title(main=paste(sample,"Scores"), sub=paste("p.value=", round(wilcox[sample,], 3) ))
  dev.off()

  par(mfrow=c(2,4))
  for (sample in levels(all_samples$sample))
  {
  x = all_samples[all_samples$sample == sample,]
  
  stdev = sd(x$score)
  if (!sample %in% full_sim)
    {
    x = x[order(x$score),]
    pt = which(x$type == 'KnownBP')
    centers = c(mean(x$score)-stdev*2,  mean(x$score)-stdev, mean(x$score)+stdev, mean(x$score)+stdev*2)
    if (shapiro.test(x$score)$p.value < 0.05) centers = quantile(x$score, probs=seq(0,1,0.33)) 
      
    km = kmeans(x$score, centers, iter.max=50)
    top = x[which(km$cluster == which.max(km$centers)),]
    spike_ins[sample] = length(which(top$type == 'KnownBP'))
  
    cluster_spikein[[sample]] = km$cluster[which(x$type == 'KnownBP')]
    
    palette(c('green','orange', 'purple','blue'))
    
    plot(x$score, col=km$cluster, pch=19, main=sample, sub="k-means clusters", ylab="Scores", xlab="")
    points(pt, x$score[pt], col='red', lwd=4, pch=21, cex=1.5)
    legend('topleft', legend=c(order(km$centers), 'Known'), col=c(palette(), 'red'), pch=c(rep(19,4), 21))
    }
  }
  
  ##???maybe
  if (sample %in% full_sim)
    {
    fpr<-function(a,total) {
      return ((a-1)/(a-1+total-1))
    }
    km = kmeans(x$score, c(mean(x$score)-stdev*1.5, mean(x$score), mean(x$score)+stdev*1.5))
    top = length(which(km$cluster==which.max(km$centers)))
    sim_fpr['3', sample] = fpr(top,total)
    
    km = kmeans(x$score, c(mean(x$score)-stdev*2,  mean(x$score)-stdev, mean(x$score)+stdev, mean(x$score)+stdev*2))
    top = length(which(km$cluster==which.max(km$centers)))
    sim_fpr['4',sample] = fpr(top,total)      
    }
  
  
  x$sample = sample
  if (!exists('all_samples')) {
    all_samples = x
  } else {
    all_samples = rbind(all_samples,x)
  }
  }

save(all_samples, file=paste(testDir, 'PatientBPs', 'all_samples.Rdata', sep="/"))


write.table(round(wilcox[sort(rownames(wilcox)),],3), quote=F, sep="\t", file="~/Desktop/Simulated/wilcox.txt"))

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


