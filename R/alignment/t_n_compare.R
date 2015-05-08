source("~/workspace/IGCSA/R/alignment/lib/utils.R")
library('VennDiagram')

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

load.files<-function(dirs,type,bands)
  {
  all_scores = NULL
  for (dir in dirs)
    {
    print(dir)
    curr_n = create.score.matrix(dir, type, bands)
    if (is.null(curr_n)) next
    
    if (!is.null(curr_n$right.in.span)) 
      curr_n$score[which(curr_n$right.in.span == 0)] = 0
    
    curr_n = curr_n[curr_n$score > 0,]
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

vol="/Volumes/exHD-Killcoyne/IGCSA/runs/alignments/GA"
#load(paste(vol,"all.Rdata", sep="/"))

if (!exists('all_c'))
  all_c = load.files(grep("*PST", list.files(vol,full.names=T), value=T), "PST", bands)

if (!exists('all_n'))
  all_n = load.files(grep("*N(B|T)", list.files(vol,full.names=T), value=T), "NT", bands)

save(all_c,all_n,file=paste(vol,"all.Rdata",sep="/"))
save(all_c,all_n,file="~/Dropbox/Private/all.Rdata")

all_c$sample= as.factor(as.character(all_c$sample))
all_n$sample= as.factor(as.character(all_n$sample))

cancer = table(all_c$sample)
normal = table(all_n$sample)

print(cancer)
print(normal)


svs = as.data.frame(matrix(data=0,nrow=0,ncol=length(cancer),dimnames=list(c(),c(unlist(lapply(names(cancer), sub, pattern="-PST",replacement=""))))))
for (i in 1:length(cancer))
  {
  print(names(cancer)[i])
  pst = all_c[all_c$sample == names(cancer)[i],]
  if (is.null(pst) | nrow(pst) <= 0) next
  rownames(pst) = c(1:nrow(pst))

  nt = all_n[all_n$sample == names(normal)[i],]
  rownames(nt) = c(1:nrow(nt))

  nrow(pst)
  nrow(nt)
  if (nrow(nt) > nrow(pst))
    nt = nt[sample.int(nrow(nt), nrow(pst)),]
  
  
  #print(paste("scaled:",wilcox.test(scale(pst$score), scale(nt$score))$p.value))
  #print(t.test(pst$score, nt$score)$p.value)
  
#  pst$score = scale(pst$score)
#  nt$score = scale(nt$score)
  
  par(mfrow=c(2,1))
  norm.plot(pst$score)
  summary(pst$score)
  title(names(cancer)[i])
  
  norm.plot(nt$score)
  summary(nt$score)
  title(names(normal)[i])
  
  cols = c('name','score','type')
  km = kmeans(pst$score, quantile(pst$score, probs=seq(0,1,0.33)))
  top_c = pst[which(km$cluster == which.max(km$centers)), cols]

  #palette(c('green','orange', 'purple','blue'))
  par(mfrow=c(2,1))
  plot(pst$score, col=km$cluster, pch=19, main=names(cancer)[i], sub="k-means clusters", ylab="Scores", xlab="")
  legend('topright', legend=c(order(km$centers)), col=palette(), pch=c(rep(19,4)))
  
  km = kmeans(nt$score, quantile(nt$score, probs=seq(0,1,0.33)))
  top_n = nt[which(km$cluster == which.max(km$centers)), cols]

  plot(nt$score, col=km$cluster, pch=19, main=names(normal)[i], sub="k-means clusters", ylab="Scores", xlab="")
  legend('topright', legend=c(order(km$centers)), col=palette(), pch=c(rep(19,4)))

  nrow(top_c)
  nrow(top_n)
#  write.table(setdiff(top_c$name,top_n$name), quote=F,sep="\t", row.names=F)

#plot.new()
#draw.pairwise.venn(nrow(top_c),nrow(top_n),length(intersect(top_c$name,top_n$name)), category=c('Tumor', 'Germline'), 
#                   cat.pos=c(45,45), cex = rep(2,3), cat.cex=2, 
#                   fill=c('blue','red'), col=c('blue','red'))
#title(main=sub("-N(B|T)", "", names(normal)[i]))

fill.sv<-function(cnc,norm,name)
    {
    matches = union(cnc$name,norm$name)
    sv = vector(mode="numeric",length=length(matches))
    sv = unlist(lapply(matches, function(name){
      in_tumor = ifelse( length(which(cnc$name == name)) > 0, 2, 0)  
      in_tumor = in_tumor + ifelse( length(which(norm$name == name)) > 0, 1, 0) 
      return(in_tumor)      
    }))
    names(sv) = matches
    
    return(sv)
    }

  cnc_type = sub("-N(B|T)", "", names(normal)[i])
  pairs = fill.sv(top_c,top_n, cnc_type)
  for (pair in names(pairs))
    svs[pair, cnc_type] = pairs[pair]
  }
write.table(svs, sep="\t", col.names=NA, quote=F, file="~/tmp/")

venn<-function(a,b,aib,category,title) 
  {
  plot.new()
  draw.pairwise.venn(a,b,aib, category=category,
                   cat.pos=c(45,45), cex = rep(2,3), cat.cex=2, 
                   fill=c('blue','red'), col=c('blue','red'))
  title(title)
  }

dev.off()
ds = colnames(svs)[3]
venn(length(which(svs[[ds]] >= 2)), length(which(svs[[ds]] == 1 | svs[[ds]] == 3)), length(which(svs[[ds]] == 3)), c('Tumor', 'Normal'), ds)



stop("")
#svs = svs[,-which(colnames(svs) == 'LUAD')]
#svs[is.na(svs)]
#svs[is.na(svs)] = 0
#svs = svs[which(rowSums(svs, na.rm=T) > 0),]


image(1:ncol(svs), 1:nrow(svs), t(as.matrix(svs)), col=c('white','red','blue'), xlab="",ylab="",axes=F)
axis(BELOW<-1, at=1:ncol(svs), labels=colnames(svs), cex.axis=0.7)
axis(LEFT<-2, at=1:nrow(svs), labels=rownames(svs), las=HORIZONTAL<-1,cex.axis=0.7)

cols=colorRampPalette(c("blue","white","red"))(256)
heatmap((as.matrix(svs)), border='black', Rowv=NA,Colv=NA, col=c('blue','white','red'))
levelplot(t(as.matrix(svs)), border='black', col.regions=cols, zlim=c(0, 3), scale=F) 





aic = vector(mode="numeric")
for (i in 1:10)
  aic[i] = kmeansAIC(kmeans(curr_n$score, i))

km = kmeans(curr_n$score, which.min(aic))
clusters[[dir]] = km


#save(all_n, all_c, file=paste(vol, "all.Rdata",sep="/"))

all_n$sample = as.factor(all_n$sample)
normal=list()
for (n in levels(all_n$sample))
  {
  curr = all_n[all_n$sample == n,]
  #curr$score = scale(curr$score)[,1]
  
  aic = vector(mode="numeric")
  for (i in 2:10)
    aic[i] = kmeansAIC(kmeans(curr$score, i))
  km = kmeans(curr$score, which.min(aic))
  normal[[n]] = curr[which(km$cluster == which.max(km$centers)),'name']
  }

length(unlist(normal))
length(unique(unlist(normal)))


all_c$sample = as.factor(all_c$sample)
cancer=list()
for (n in levels(all_c$sample))
  {
  curr = all_n[all_c$sample == n,]
  #curr$score = scale(curr$score)[,1]
  
  aic = vector(mode="numeric")
  for (i in 2:10)
    aic[i] = kmeansAIC(kmeans(curr$score, i))
  km = kmeans(curr$score, which.min(aic))
  cancer[[n]] = curr[which(km$cluster == which.max(km$centers)),'name']
  }
lapply(cancer, length)
lapply(normal,length)


length(unlist(cancer))
length(unique(unlist(cancer)))


#nt = create.score.matrix(paste(vol, paste(cancer,"NB","14",sep="-"),sep="/"), "NT", bands)
#pst = create.score.matrix(paste(vol, paste(cancer,"PST","14",sep="-"),sep="/"), "PST", bands)

par(mfrow=c(2,1))
hist(pst$score, breaks=50)
abline(v=mean(pst$score)+sd(pst$score))

hist(nt$score, breaks=50)
abline(v=mean(nt$score)+sd(nt$score))

par(mfrow=c(2,1))
norm.plot(pst$score)
summary(pst$score)
title(paste(cancer,"Tumor"))

norm.plot(nt$score)
summary(nt$score)
title(paste(cancer,"Normal"))


aic = vector(mode="numeric")
for (i in 1:10)
  aic[i] = kmeansAIC(kmeans(pst$score, i))

kmc = kmeans(pst$score, which.min(aic))
cancer = pst[which(kmc$cluster == which.max(kmc$centers)),'name']

aic = vector(mode="numeric")
for (i in 1:10)
  aic[i] = kmeansAIC(kmeans(nt$score, i))
kmn = kmeans(nt$score, which.min(aic))
normal = nt[which(kmn$cluster == which.max(kmn$centers)),'name']


multiplier=1
topPST = pst[pst$score > mean(pst$score)+sd(pst$score)*multiplier, c('name','score')]
topNT = nt[nt$score > mean(nt$score)+sd(nt$score)*multiplier, c('name','score')]

tumor_only = topPST[topPST$name %in% setdiff(topPST[,'name'],topNT[,'name']),]
shared = topPST[topPST$name %in% intersect(topPST[,'name'],topNT[,'name']),]

write.table(tumor_only[,1],quote=F,row.names=F)
write.table(shared[,1], quote=F,row.names=F)

which(tumor_bd$Band1 %in% unlist(strsplit(shared$name, "-")))
which(tumor_bd$Band2 %in% unlist(strsplit(shared$name, "-")))

which(tumor_bd$Band1 %in% unlist(strsplit(tumor_only$name, "-")))
which(tumor_bd$Band2 %in% unlist(strsplit(tumor_only$name, "-")))




