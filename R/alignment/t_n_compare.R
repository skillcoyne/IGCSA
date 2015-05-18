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
load(paste(vol,"all.Rdata", sep="/"))

for (f in grep("*P(S|B)T", list.files(vol,full.names=T), value=T))
  print(paste(f, length(list.files(f))))

for (f in grep("*N(B|T)", list.files(vol,full.names=T), value=T))
  print(paste(f, length(list.files(f))))
  
if (!exists('all_c'))
  all_c = load.files(grep("*P(S|B)T", list.files(vol,full.names=T), value=T), "PST", bands)

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

cancer_regions = list()
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
  
  #print(wilcox.test(pst$score, nt$score)$p.value)
  
  sd.km<-function(score)
    {
    stdev=sd(score)
    
    km = tryCatch({
      #kmeans(score,  c(mean(score)-stdev*1.5,  mean(score)-stdev, mean(score)+stdev, mean(score)+stdev*1.5), iter.max=100)
      kmeans(score,  c(mean(score)-stdev*1.5,  mean(score)-stdev, mean(score)+stdev, mean(score)+stdev*1.5), iter.max=100)
    }, error = function(err) {
      kmeans(score, quantile(score, seq(0,1,0.3)), iter.max=100)
      #kmeans(score, quantile(score, seq(0,1,0.5)), iter.max=100)
    })
    
    return( km )
    }

  cols = c('name','score','type')
  stdev=sd(pst$score)
  km = sd.km(pst$score)
  #km = kmeans(pst$score, quantile(pst$score, probs=seq(0,1,0.3)))
  #km = kmeans(pst$score, 4)
  top_c = pst[which(km$cluster == which.max(km$centers)), cols]

  #palette(c('green','orange', 'purple','blue'))
  par(mfrow=c(2,1))
  plot(pst$score, col=km$cluster, pch=19, main=names(cancer)[i], sub="k-means clusters", ylab="Scores", xlab="")
  legend('topright', legend=c(order(km$centers)), col=palette(), pch=c(rep(19,4)))
  
  #km = kmeans(nt$score, quantile(nt$score, probs=seq(0,1,0.3)))
  #km = kmeans(nt$score, quantile(nt$score, probs=seq(0,1,0.3)), iter.max=100)
  km = sd.km(nt$score)
  top_n = nt[which(km$cluster == which.max(km$centers)), cols]

  plot(nt$score, col=km$cluster, pch=19, main=names(normal)[i], sub="k-means clusters", ylab="Scores", xlab="")
  legend('topright', legend=c(order(km$centers)), col=palette(), pch=c(rep(19,4)))

  nrow(top_c)
  nrow(top_n)
  write.table(setdiff(top_c$name,top_n$name), quote=F,sep="\t", row.names=F)

  cnc_type = sub("-N(B|T)", "", names(normal)[i])
  cancer_regions[[cnc_type]] = setdiff(top_c$name,top_n$name)

#plot.new()
#draw.pairwise.venn(nrow(top_c),nrow(top_n),length(intersect(top_c$name,top_n$name)), category=c('Tumor', 'Germline'), 
#                   cat.pos=c(45,45), cex = rep(2,3), cat.cex=2, 
#                   fill=c('blue','red'), col=c('blue','red'))
#title(main=sub("-N(B|T)", "", names(normal)[i]))

  }

df = data.frame(matrix(data="", ncol=length(cancer_regions), nrow=max(unlist(lapply(cancer_regions, length)))))
colnames(df) = names(cancer_regions)
for (cnc in names(cancer_regions))
  df[,cnc] = cbind( c(cancer_regions[[cnc]], rep("", nrow(df)-length(cancer_regions[[cnc]]))) )

write.table(df, quote=F, sep="\t", row.names=F, file="~/tmp/cancer_regions.txt")
