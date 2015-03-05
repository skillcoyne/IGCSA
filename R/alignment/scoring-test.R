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



create.matrix<-function(dir, bands)
  {
  if (is.null(bands) | length(bands) <= 0)
    stop("bands required")
  
  files = list.files(path=dir, pattern="summary.Rdata", recursive=T)
  files = files[grep(".*_\\d+/", files, value=F, invert=T)]
  
  if (length(files) > 300)
    files = sample(files, 300)
  
  print(length(files))
  
  cols = names(create.summary.obj())
  cols = c(cols[-which(cols %in% c('name', 'orientation', 'cigar', 'distance','phred', 'l.orientation','r.orientation', 'scored'))], 
           'prob.sum','right.ratio','max.pos.reads', 'max.pos.prob')  

  m = matrix(ncol=length(cols), nrow=length(files), dimnames=list(gsub("/.*", "", files), cols), data=0)
  or = matrix(ncol=4, nrow=length(files), dimnames=list(gsub("/.*", "", files), c('F:F','F:R','R:F','R:R')))
  
  for (i in 1:length(files))
    {
    load(file=paste(dir, files[i],sep="/"))
    print(paste(dir, files[i], sep="/"))
    
    name = dirname(files[i])
    
    if (is.null(summary[['max.pos.reads']]))
      summary[['max.pos.reads']] = max(summary[['top.pos.clusters']]$ct)

    if (is.null(summary[['emr']]))
      {
      summary[['emr']] = summary[['score']]
      summary[['score']] = summary[['emr']]+(summary[['max.pos.reads']]/summary[['n.right.reads']])*10
      }
    
    for (c in cols)
      {
      className = class(summary[[c]])
      #print(className)
      
      if (is.null(summary[[c]]))
        m[name,c] = NA
      else if (className %in% c('numeric', 'integer'))
        m[name,c] = round(summary[[c]], 4)
      else if (className == 'htest')
        m[name,c] = round(summary[[c]]$p.value, 4)
      else
        print(paste(c, summary[[c]], sep=":"))
      }

      m[name,'prob.sum'] = sum(summary$top.pos.clusters$prob)
      m[name,'right.ratio'] = sum(summary$top.pos.cluster$ct)/summary$n.right.reads
      if (is.null(m[name,'max.pos.reads']))
          m[name, 'max.pos.reads'] = max(summary$top.pos.cluster$ct)
      
        m[name,'max.pos.prob'] = max(summary$top.pos.cluster$prob)
    
    if(!is.null(summary$r.orientation))  
      or[i,] = cbind(summary$r.orientation)
    }
  m = as.data.frame(m)
  
  m$bp = unlist(lapply(rownames(m), function(x) sum(bands[which(bands$name %in% unlist(strsplit(x, "-"))), 'len'])  ))
  m$gene.count = unlist(lapply(rownames(m), function(x) sum(bands[which(bands$name %in% unlist(strsplit(x, "-"))), 'gene.count'])  ))
  
  # Below 0.1 can be discarded as not normal, the rest might be normal (though a lot cleary are not)
  #m = m[-which(m$l.shapiro <= 0.1),]
  m = m[m$estimated.dist == 2,]  # not an issue anymore as I'm only looking for 2 distributions
  
  m$name = rownames(m)
  rownames(m) = c(1:nrow(m))
  
  m$estimated.dist = NULL
  m$l.shapiro = NULL
  m$r.shapiro = NULL
  
  return(m)
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
  par(mfrow=c(4,4))
  for (col in colnames(df))
    {
    if (col %in% c("name", 'gene.count','bp', 'type', 'total.reads')) next
    
    plot( (df[[col]]), col=df$type, pch=20, ylab=col,lwd=2, cex.lab=2, cex.main=2, cex.axis=2,cex=2)
      
    for (name in names(points))
      {
      rows = points[[name]]
      points(rows, df[[col]][rows], pch=21,col=colors[[name]], cex=2)
      }
    }
  plot.new()
  par(xpd=TRUE)
  legend("center", legend = c(levels(df$type), names(points)), 
         col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type))),rep(21, length(points))), horiz = F, cex=2)
  }

plot.scores<-function(df,points=list(), colors=list())
  {
  #par(mfrow=c(2,1))
  
  #plot(df$score, col=df$type, lwd=2, main=paste("Scores"), pch=20)
  #for (name in names(points))
  #  {
  #  rows = points[[name]]
  #  points(rows, df[[col]][rows], pch=21,col=colors[[name]], cex=2)
  #  }
  #legend("topleft", legend = c(levels(df$type), names(points)), 
  #       col=c(palette(), unlist(colors)), pch=c(rep(20, length(levels(df$type))),rep(21, length(points))), horiz = F)
  
  plot(df$score[order(df$score)], ylab='score', xlab='', col=df$type[order(df$score)], main="Sorted Scores", pch=20)
  for (name in names(points))
    {
    rows = points[[name]]
    text(x=which(order(df$score) %in% rows), y=df$score[order(df$score)][which(order(df$score) %in% rows)], 
         labels=df[order(df$score),][which(order(df$score) %in% rows) , 'name'], pos=2)
    #points(which(order(df$score) %in% rows), df$score[order(df$score)][which(order(df$score) %in% rows)], pch=21,col=colors[[name]], cex=2)
    }
  abline(h=mean(df$score), col='blue', lty=3, lwd=2)
  text(x=40,y=mean(df$score), labels=paste("mean", round(mean(df$score),2)), pos=3)
  
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
top10=list()

testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments"

samples = c('8-15','HCC1954.G31860','GBM', 'BRCA','OV','KIRC', 'LAML','LUAD', 'COAD')
wilcox=matrix(ncol=1,nrow=length(samples),dimnames=list(samples, c('p.value')))


for (sample in samples)
  {
  patient = ifelse (!sample %in% c('8-15', 'HCC1954.G31860'), paste(sample, "Patient",sep="-"), sample)

  random = create.matrix(paste(testDir, "Random", patient, sep="/"), bands) 
  random$type='Random'

  patients = create.matrix(paste(testDir, "PatientBPs", patient, sep="/"), bands)
  patients$type="KnownBP"
  if (sample=='8-15') patients$type="Random"

  x = rbind(random,patients)
  pt = which(x$type == "KnownBP")
  
  nrow(x)
  
  points=list()
  colors=list()

  palette(c('red', 'green'))
  
  if (sample=="8-15")
    {
    pt = which(x$name == "8q21-15q15")
    x[pt, 'type'] = "KnownBP"
    points=list("Correct"=pt)
    colors=list("Correct"='blue')
    }
  rand = which(x$type == 'Random')

  wilcox[sample, 'p.value'] = wilcox.test(x$score[-pt], x$score[pt])$p.value

  x$type=as.factor(x$type)

  png(filename=paste("~/Desktop/Simulated", paste(patient, "all.png", sep="_"), sep="/"), width=1600, height=1200, units="px")
  plot.all(x, points, colors)
  dev.off()

  png(filename=paste("~/Desktop/Simulated", paste(patient, "score.png", sep="_"), sep="/"), width=800, height=600, units="px")
  plot.scores(x,points,colors)
  title(sub=paste("p.value=", round(wilcox[sample,], 3) ))
  dev.off()

  png(filename=paste("~/Desktop/Simulated", paste(patient, "gene_density.png", sep="_"), sep="/"), width=800, height=600, units="px")
  gene.density.plots(x,points,colors)
  dev.off()

  top10[[patient]] = x[order(-x$score),][1:10,]

  rm(x,random,patients)
  print(patient)
  }


lapply(top10, function(x) {
  max(x[['score']])
})

lapply(top10, function(x){
  c(max(x[['score']]), range(x[which(x[['type']] == 'KnownBP'), 'score']))
})



lapply(top10, function(x) length(which(x[['type']] == 'Patient')))

merged = NULL
topPairs = lapply(top10, function(x) x[,c('name','score','type')])
for (n in names(topPairs))
  {
  topPairs[[n]]$set = n
  merged = rbind(merged, topPairs[[n]])
  }


spiked = list.files(path=paste(testDir, "PatientBPs", "KIRC-Patient", sep="/"), pattern="-")

repeats = merged[which(duplicated(merged$name)), 'name']
repeats = repeats[-which(repeats %in% spiked)]

m = merged[which(merged$name %in% repeats),]
m[order(m$name),]




