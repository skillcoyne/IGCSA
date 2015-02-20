source("~/workspace/IGCSA/R/alignment/lib/utils.R")

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
    load(file=paste(dir, files[i], sep="/"))
    print(files[i])
    
    name = dirname(files[i])
    
    for (c in cols)
      {
      className = class(summary[[c]])
      print(className)
      
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
      m[name,'max.pos.reads'] = max(summary$top.pos.cluster$ct)
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

score.adjust<-function(s)
  {
  # gene density
  #g = ( log(x[['gene.count']])^3/x[['bp']])
  g =  log2(s[['gene.count']]) / s[['bp']]
  
  gdx = g *1E6
  score = s[['score']]
  (gdx*score)
  }

args <- commandArgs(trailingOnly = TRUE)

testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments"
args[1] = paste(testDir, "GA/KIRC-Patient", sep="/")


bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")
bands = bands[,c('name','len','gene.count')]

b = create.matrix(args[1],bands)
b = b[b$prob.sum > 0,]
b$estimated.dist = NULL

a = create.matrix(paste(testDir, "PatientBPs/KIRC-Patient", sep="/"), bands)
a$estimated.dist=NULL

x = rbind(b,a)

#x = x[x$l.shapiro > 0.1,]
#x = x[x$score >= 0.4,]

x$gcs = apply(x, 1, score.adjust)

x = x[order(-x$gcs),]
head(x)

plot(x$gcs, ylim=c(0,max(x$gcs)), type='o',col='black')

sims = which(rownames(x) %in% rownames(a) )
points(sims, x$gcs[sims], col='red', pch=19) 

points(x$prob.sum*2,col='blue',pch=16)
points(sims, x$prob.sum[sims]*2,col='red',pch=16)

points(x$right.ratio*2,col='purple',pch=17)
points(sims, x$right.ratio[sims]*2,col='red',pch=17)

points(x$max.pos.reads/1000,col='orange',pch=18)
points(sims, x$max.pos.reads[sims]/1000,col='red',pch=18)

points(x$max.pos.prob*6, col='blue', pch=16)
points(sims, x$max.pos.prob[sims]*6, col='red', pch=16)


y = x

y$score = y$score/y$max.pos.prob/100

y$score = y$max.pos.reads/1000 + y$score
y$gcs = apply(y, 1, score.adjust)

points(y$gcs, col='green',pch=21)
points(sims, y$gcs[sims], col='red',pch=19)


y = x

y$score = y$score/y$prob.sum^.5   

y$gcs=apply(y,1,score.adjust)

lines(y$gcs/2,type='o',col='green',pch=19,lwd=2)


