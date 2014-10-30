
right.dist<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  mean(model$z[, rightside]) 
  }
  
fill.matrix<-function(sv, smatrix, colname)
  {
  for (i in 1:length(sv))
    smatrix[names(sv[i]), colname] = sv[i]

  return(smatrix)
  }

kmeansAIC = function(fit)
  {
  m = ncol(fit$centers)
  n = length(fit$cluster)
  k = nrow(fit$centers)
  D = fit$tot.withinss
  return(D + 2*m*k)
  }

setwd("/Volumes/exHD-Killcoyne/Insilico/runs/alignments")

files = list.files(pattern="reads.Rdata", recursive=T)
files = grep(".*-N.*", files, value=T, invert=T)  # just cancer

cols = sub("/reads.Rdata", "", files)
scores = as.data.frame(matrix(ncol=length(cols),nrow=0,data=0,dimnames=list(c(), cols)))

for (file in files)
  {
  name = sub("/reads.Rdata", "", file)
  load(file)
  print(paste(name, nrow(rAll)))
  scr = sort(unlist(lapply(emmodel, right.dist)))
  
  scores = fill.matrix(scr, scores, name)

  rm(rAll, rLeft, rRight, emmodel)
  }

apply(scores, 2, function(x) length(which(is.na(x))))
save(scores, file="~/tmp/scores.Rdata")

# which(apply(scores, 2, function(x) length(which(is.na(x))) )  > nrow(scores)/3) 
#scores = scores[,-which(apply(scores, 2, function(x) length(which(is.na(x)))  > nrow(scores)/3))]

scores = na.omit(scores)

# temporarily
colnames(scores)
scores = scores[, names(sort(apply(scores, 2, max), decreasing=T))]
scores = as.matrix(scores)


# find the number of clusters for the scores
params = vector(mode='integer', length=15)
for (i in 1:length(params))
  {
  km = kmeans(scores[!is.na(scores)], i)
  params[i] = kmeansAIC(km)
  }
num_clusters = which(min(params) == params)

km = kmeans(scores[!is.na(scores)],  num_clusters)
print(km$centers)
km$size

cols = vector(mode='character',length=num_clusters)
cols[which(km$centers == max(km$centers))] = 'red'
cols[which(km$centers != max(km$centers))] = c('blue','green','purple')

colors = sapply(km$cluster,function(x) cols[x])
library(MASS)
parcoord(scores, col=colors,var.label=T )
legend('topright', fill=cols[order(-km$centers)], legend=c(1:length(km$centers)), title="clusters")

## just from HCC1954
write.table( rownames(scores[which(colors[1:nrow(scores)] == 'red'),]), quote=F)


byscores = matrix(data=colors, ncol=ncol(scores), nrow=nrow(scores), dimnames=list(rownames(scores), colnames(scores)))

write.table(names(sort(scores[,'GBM-PST'][which(byscores[,'GBM-PST'] == 'red')], decreasing=T)), row.names=T, quote=F)




#top_clusters = vector(mode="integer",length=ncol(scores))
#names(top_clusters) = colnames(scores)
#clusters = scores
#for (name in colnames(scores))
#  {
#  print(name)
#  km = kmeans(scores[[name]], num_clusters)
#  print(km$centers)

#  cls = km$cluster
#  centers = km$centers[order(-km$centers),]
#  for (cent in centers)
#    {
#    clust = names( which(centers == cent) )
#    cls[cls == clust] = cols[which(centers == cent)]
#    }

#  clusters[[name]] = cls
#  }

scoresAtZero <- scores==0
naScores <- is.na(scores)
cscore = combn(ncol(scores), 2)
dev.off()
layout( matrix(c(1,0,0,2,3,0,4,5,6), 3, 3, byrow = TRUE) )
for (i in 1:ncol(cscore))
  {
  p = cscore[,i]
  rowsToUse = !( naScores[,p[2]] | naScores[,p[1]] )
  #rowsToUse = !(scoresAtZero[,p[2]] | scoresAtZero[,p[1]])
  plot(scores[rowsToUse,p[2]], scores[rowsToUse,p[1]], xlab=colnames(scores)[p[2]], ylab=colnames(scores)[p[1]])
  #points(scores[rowsToUse,p[2]], scores[rowsToUse,p[1]], col=clusters[rowsToUse,p[1]], pch=19)
  points(scores[rowsToUse,p[2]], scores[rowsToUse,p[1]], col=colors[rowsToUse], pch=19)
  }

clusters = clusters[rownames(scores),]


tops = list()
for (col in 1:ncol(scores))
  {
  k = kmeans(scores[,col], num_clusters)
  tops[[colnames(scores)[col]]] = scores[,col][k$cluster == which(k$centers == max(k$centers))]
  }

tops = lapply(tops, names)
unlist(lapply(tops, length))


a = intersect(tops[[1]], tops[[2]])
b =  intersect(a, tops[[3]])
c = intersect(b, tops[[4]])
d = intersect(c, tops[[5]])
e = intersect(d, tops[[6]])
f = intersect(a, tops[[7]])
