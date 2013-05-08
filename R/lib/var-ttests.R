rm.na<-function(row)
  {
  row = t(row)
  row = row[ complete.cases(row), ]
  return( t(row) )
  }

plot.var.cor<-function(tests, plot=F)
  {
  tests = tests[ complete.cases(tests), ]
  tests = t(tests)
  
  cors = matrix(nrow=2, ncol=ncol(tests))
  rownames(cors) = c('rho', 'p.value')
  colnames(cors) = colnames(tests)
  cors = as.data.frame(cors)
  
  if (plot) par(mfrow=c(3,3))
  for (var in colnames(tests))
    {
    ct = cor.test(1:nrow(tests), tests[,var], m="s", na.action=na.omit)
    cors['rho', var] = round(ct$estimate, 3)
    cors['p.value', var] = round(ct$p.value, 3)
    if (plot)
      {
      plot(1:9,tests[,var], type='l', col='blue', ylab=var,
           main=paste('rho:', round(ct$estimate,3), 'pvalue:', round(ct$p.value,3) ))
      }
    }
  return(cors)
  } 

# Run t.test over each variation in chunks of the *known* genome (all unknowns were filtered out) broken up by bins based on GC content.
test.gc.bins<-function(cg, binsize, variations)
  {
  tests = data.frame()
  last_var = which(colnames(cg) == "GC")-1
  
  size = round(max(cg[,'GC'])/10)
  for(i in 0:9)
    {
    max=i*size; min=max-size; 
    if (min < 0) next
    
    lowRows = cg[,'GC'] >= min & cg[,'GC'] < max
    highRows = cg[,'GC'] >= max & cg[,'GC'] < max+size
    
    rowA = rm.na( cg[lowRows,] )
    rowB = rm.na( cg[highRows,] )
    
    for (var in variations)
      {
      if (var %in% colnames(rowA) )
        {
        tt = t.test(rowA[,var], rowB[,var])
        tests[var, paste(i, i+1, sep="-")] = round(tt$statistic, 3)
        }
      else
        {
        tests[var, paste(i, i+1, sep="-")] = NA
        }
      }
    }
  return(tests)
  }

# Run t.test over each variation in chunks of the *known* genome (all unknowns were filtered out) broken up by number of bins.  Entirely regardless
# of the GC content
test.bp.bins<-function(cg, binsize, variations)
  {
  tests = data.frame()
  size = round(nrow(cg)/binsize)
  
  last_var = which(colnames(cg) == "GC")-1
  
  for(i in 0:(binsize-1))
    {
    max=i*size; min=max-size; nextStep=max+size
    if (min < 0) next
    
    if (max >= nrow(cg))  break
    if (nextStep > nrow(cg)) nextStep = nrow(cg)
    
    rowA = rm.na( cg[min:max,] )
    rowB = rm.na( cg[max:nextStep,] )
    
    for (var in variations)
      {
      if (var %in% colnames(rowA)  )
        {
        tt = t.test(rowA[,var], rowB[,var])
        tests[var, paste(i, i+1, sep="-")] = round(tt$statistic, 3)
        }
      else
        {
        tests[var, paste(i, i+1, sep="-")] = NA
        }
      }
    }
  return(tests)
  }
