#! /usr/bin/env Rscript

library('mclust')

right.dist.score<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  mean(model$z[, rightside]) 
  }


get.mixtures<-function(vv, modelName="E")
  {
  cutoff = (max(vv)-min(vv))/2
  z = matrix(0,length(vv),2) 
  z[,1] = as.numeric(vv >= cutoff)
  z[,2] = as.numeric(vv < cutoff)
  msEst = mstep(modelName, vv, z)
  modelName = msEst$modelName
  parameters = msEst$parameters
  em(modelName, vv, parameters)
  }


input = file("stdin", 'r')

read.lens = NULL
while(length(line <- readLines(input, n=-1L, warn=FALSE)) > 0) 
  {
  read.lens = as.integer(unlist(strsplit(line, "\n")))
  }

model = get.mixtures(log(read.lens), "V")
write.table(right.dist.score(model), quote=F, row.name=F,col.name=F)

