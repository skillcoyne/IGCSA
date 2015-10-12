source("~/workspace/IGCSA/R/denovo_alignment/lib/utils.R")
library('VennDiagram')


filled.km.plot<-function(scores, fit)
  {
  plot(scores, col=fit$cluster, pch=19, xlab="", ylab='score', type='h')
  
  colors = palette()
  for (i in order(-fit$center))
    {
    xx = c(which(fit$cluster == i), rev(which(fit$cluster == i)))
    yy = c(rep(0, fit$size[i]), rev(scores[which(fit$cluster == i)]))
    
    polygon(x=xx, y=yy, col= colors[i], border=NA  )  
    }
  legend('topleft', legend=paste(seq(1,4,1), " (", fit$size[order(-fit$center)], ")", sep=""), fill=colors[order(-fit$center)], border=NA)
  }
  
km.aic<-function(x)
  {
  aic = vector(mode="numeric")
  for (j in 2:10)
    aic[j] = kmeansAIC(kmeans(x, j))

  print(aic)
  return(which.min(aic))
  }

norm.plot<-function(score)
  {
  plot(function(x) dnorm(x,mean(score),sd(score)), min(score)-sd(score)*1,max(score)+sd(score)*1,
       ylab="Density",xlab="Scores")
  xpos=vector("numeric",2)
  ypos=vector("numeric",2)
  ypos[1]=0
  density_pos=dnorm(score,mean(score),sd(score))
  for(i in 1:length(score))
    {
    color='black'
    lwd=1
    
    if (score[i] >= mean(score)+sd(score)) color='blue'
    if (score[i] >= mean(score)+sd(score)*2) { color='red'; lwd=2}
    xpos[1]=score[i]
    xpos[2]=score[i]
    ypos[2]=density_pos[i]
    lines(xpos,ypos, col=color,lwd=lwd)
    text(xpos[2],ypos[2],labels=names(score[i]),pos=3)
    }
  }

load.files<-function(dirs,type,bands,weight=10)
  {
  all_scores = NULL
  for (dir in dirs)
    {
    print(dir)
    curr_n = create.score.matrix(dir, type, bands, weight)
    if (is.null(curr_n)) next
    
    if (!is.null(curr_n$right.in.span)) 
      curr_n$score[which(curr_n$right.in.span == 0)] = NA
    
    #curr_n = curr_n[curr_n$score > 0,]
    mean(curr_n$score)
    
    curr_n$sample = basename(dir)
    if (is.null(all_scores)) all_scores =curr_n  else  all_scores = rbind(all_scores,curr_n ) 
    }
    
  all_scores$sample = as.factor(all_scores$sample)
  all_scores$type = as.factor(all_scores$type)
  
  return(all_scores)
  }

bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")

#dir="/Volumes/exHD-Killcoyne/IGCSA/data/TCGA/sequence"
#bdx = grep(cancer, list.files(path=dir, pattern="*.ctx", recursive=T, full.names=T), value=T)
#norm_bd = read.breakdancer(bdx[1], bands)
#tumor_bd = read.breakdancer(bdx[2], bands)


## BRCA TEST
vol = "/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/BRCA-4q13"

extra_pst = load.files(grep("*P(S|B)T", list.files(vol,full.names=T), value=T), "PST",bands)
extra_nt = load.files(grep("*N(B|T)", list.files(vol,full.names=T), value=T), "NT",bands)

vol="/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/GA"
load(paste(vol,"all.Rdata", sep="/"))

for (f in grep("*P(S|B)T", list.files(vol,full.names=T), value=T))
  print(paste(f, length(grep('*Rdata', list.files(f, recursive=T), value=T))))

for (f in grep("*N(B|T)", list.files(vol,full.names=T), value=T))
  print(paste(f, length(grep('*Rdata', list.files(f, recursive=T), value=T))))
  
if (!exists('all_c'))
  {
  all_c = load.files(grep("*P(S|B)T", list.files(vol,full.names=T), value=T), "PST", bands)
  all_n = load.files(grep("*N(B|T)", list.files(vol,full.names=T), value=T), "NT", bands)
  
  save(all_c,all_n,file=paste(vol,"all.Rdata",sep="/"))
  save(all_c,all_n,file="~/Dropbox/Private/all.Rdata")
  }

all_c$sample= as.factor(as.character(all_c$sample))
all_n$sample= as.factor(as.character(all_n$sample))

cancer = table(all_c$sample)
normal = table(all_n$sample)

print(cancer)
print(normal)

cancer_regions = list()
for (i in 1:length(cancer))
  {
  print(names(cancer)[i])
  pst = all_c[all_c$sample == names(cancer)[i],]
  if (is.null(pst) | nrow(pst) <= 0) next
  pst = pst[order(pst$score),]
  rownames(pst) = c(1:nrow(pst))

  nt = all_n[all_n$sample == names(normal)[i],]
  nt = nt[order(nt$score),]
  rownames(nt) = c(1:nrow(nt))

  nrow(pst)
  nrow(nt)
  #if (nrow(nt) > nrow(pst))
  #  nt = nt[sample.int(nrow(nt), nrow(pst)),]
  
  #print(wilcox.test(pst$score, nt$score)$p.value)
  cols = c('name','score','type')
  km = sd.km(pst$score)
  top_c = pst[which(km$cluster == which.max(km$centers)), cols]

  #palette(c('green','orange', 'purple','blue'))
  par(mfrow=c(2,1))
  filled.km.plot(pst$score, km)
  title(main=names(cancer)[i])
  
  km = sd.km(nt$score)
  
  filled.km.plot(nt$score, km)
  title(main=names(normal)[i])

  #probs = -log(dnorm( nt$score, mean(nt$score), sd(nt$score)))
  #top = nt[which( probs >= mean(probs)+sd(probs)*2 ), cols]
  
  top_n = nt[which(km$cluster == which.max(km$centers)), cols]
  
  nrow(top_c)
  nrow(top_n)
  
  cols=c('name','score')
  g = merge(pst[,cols], nt[,cols], by='name')
  plot(sort(g$score.x-g$score.y))
  km = sd.km(g$score.x-g$score.y)
  g[which(km$cluster == 4),]
  
  
  
  write.table(setdiff(top_c$name,top_n$name), quote=F,sep="\t", row.names=F)

  cnc_type = sub("-N(B|T)", "", names(normal)[i])
  cancer_regions[[cnc_type]] = setdiff(top_c$name,top_n$name)

plot.new()
draw.pairwise.venn(nrow(top_c),nrow(top_n),length(intersect(top_c$name,top_n$name)), category=c('Tumor', 'Germline'), 
                   cat.pos=c(45,45), cex = rep(2,3), cat.cex=2, 
                   fill=c('blue','red'), col=c('blue','red'))
title(main=sub("-N(B|T)", "", names(normal)[i]))

  }

df = data.frame(matrix(data="", ncol=length(cancer_regions), nrow=max(unlist(lapply(cancer_regions, length)))))
colnames(df) = names(cancer_regions)
for (cnc in names(cancer_regions))
  df[,cnc] = cbind( c(cancer_regions[[cnc]], rep("", nrow(df)-length(cancer_regions[[cnc]]))) )

write.table(df, quote=F, sep="\t", row.names=F, file="~/tmp/cancer_regions.txt")
