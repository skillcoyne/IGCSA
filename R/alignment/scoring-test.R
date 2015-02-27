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

bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")
bands = bands[,c('name','len','gene.count')]


#args <- commandArgs(trailingOnly = TRUE)

testDir = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments"

a = create.matrix(paste(testDir, "Random/8-15", sep="/"), bands)
#a = create.matrix(paste(testDir, "Random/10-9", sep="/"), bands)
#a = create.matrix(paste(testDir, "Random/KIRC-Patient", sep="/"), bands)
#a = create.matrix(paste(testDir, "Random/BRCA-Patient", sep="/"), bands)
a$type='Random'

b = create.matrix(paste(testDir, "PatientBPs/8-15", sep="/"), bands)
#b = create.matrix(paste(testDir, "PatientBPs/10-9", sep="/"), bands)
#b = create.matrix(paste(testDir, "GA/KIRC-Patient", sep="/"), bands)
b$type='Simulated'
#b$type="DE"

#c = create.matrix(paste(testDir, "PatientBPs/KIRC-Patient", sep="/"), bands)
#c = create.matrix(paste(testDir, "PatientBPs/BRCA-Patient", sep="/"), bands)
c$type="Patient"

x = rbind(a,b)
x$type=as.factor(x$type)

head(x)
nrow(x)

#cr = which(x$name %in% c$name)
cr = which(x$name == '8q21-15q15')
#cr = which(x$name == '10p14-9q21')
#sims = which(x$type == 'Simulated')
rand = which(x$type == 'Random')


palette(rainbow(length(table(x$type))))
par(mfrow=c(4,4))
for (c in colnames(x))
  {
  if (c %in% c("name", 'gene.count','bp', 'type')) next
  
  plot( (x[[c]]), col=x$type, pch=21, ylab=c,lwd=2)
  points(cr, x[[c]][cr], pch=20,col='purple')
  }
plot.new()
par(xpd=TRUE)
legend(x = "center", legend = c(levels(x$type), 'Correct'), 
       col=c(palette(), 'purple'), pch=c(21,21,20), horiz = F)

x$sum.r.prob^x$score
x[cr, c('sum.r.prob','score','n.right.reads', 'max.pos.reads')]

dev.off()
par(mfrow=c(2,2))
plot(x$max.pos.reads/x$total.reads, col=x$type)
points(cr, x$max.pos.reads[cr]/x$total.reads[cr], pch=20,col='purple')

plot(x$score, col=x$type)
points(cr, x$score[cr], pch=20,col='purple')

plot(x$max.pos.reads/x$n.right.reads, col=x$type)
points(cr, x$max.pos.reads[cr]/x$n.right.reads[cr], pch=20,col='purple')

plot(x$score+(x$max.pos.reads/x$n.right.reads)*10, col=x$type)
points(cr, x$score[cr]+(x$max.pos.reads[cr]/x$n.right.reads[cr])*10, col='purple',pch=20)


zz = x$score+(x$max.pos.reads/x$n.right.reads)*10
x$score = zz

dev.off()
par(mfrow=c(2,2))
plot(x$score, col=x$type)
points(cr, x$score[cr], col='purple',pch=20)

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

x$gcs = score.adjust(x)

plot(x$gcs, col=x$type)
points(cr, x$gcs[cr], pch=20,col='purple')


## distance measure?

t.test((abs(unlist(lapply(x$score, function(d){
  x$score-d
})))),

(abs(unlist(lapply(x$gcs, function(d){
  x$gcs-d
})))))



