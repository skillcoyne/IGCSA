library('mclust')

row.gen<-function(df)
  {
  row = cbind(nrow(df), 
              mean(log(df$len)), 
              sd(log(df$len)),
              mean(df$phred),
              sd(df$phred),
              mean(df$mapq),
              sd(df$mapq),
              nrow( df[df$ppair == TRUE,] )/nrow(df),
              length(which(df$orientation == 'F:F')),
              length(which(df$orientation == 'F:R')),
              length(which(df$orientation == 'R:R')),
              length(which(df$orientation == 'R:F'))
  )
  return(row)
  }

right.dist<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  mean(model$z[, rightside]) 
  }

right.param<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  return(list('mean' = model$parameters$mean[[rightside]], 'variance' = model$parameters$variance$sigmasq[[rightside]]))
  }

getMixtures<-function(vv, modelName="E")
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

cigar.len<-function(cv)
  {
  totals = lapply(cv, function(xs) sum( unlist(
    lapply(strsplit(unlist(strsplit(xs, ",")), ":"), 
           function(x) ifelse ( grepl("S|D", x[2]), as.integer(x[1])*-1, as.integer(x[1]))))
  ))
  return(unlist(totals))
  }

clipped.end<-function(xs)
  {
  cg = unlist(strsplit(xs, ","))
  end = 0
  if ( grepl("S", cg[1])  ) end = 1
  if ( grepl("S", cg[length(cg)]) ) end = end + 2
  return(end)
  }



