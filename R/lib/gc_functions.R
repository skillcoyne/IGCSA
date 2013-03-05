corrGC<-function(gd, vd, var='SNV', var.counts, method="pearson")
  {
  print(var.counts)
  bins1 = which(vd[[var]] == var.counts[1] )
  r1 = gd$GCRatio[bins1]
  bins2 = which(vd[[var]] == var.counts[2] )
  r2 = gd$GCRatio[bins2]
  
  print(paste("count 1:", var.counts[1], length(r1)))
  print(paste("count 2:", var.counts[2], length(r2)))
  
  if ( length(r1[is.na(r1)]) > 0 ||  length(r1[is.na(r2)]) > 0)  warning("Ratio bins contain NA values.")

  if (length(r1) <= 1 || length(r2) <= 1) 
    { 
    return(NA)
    }
  if ( length(r1) > length(r2) )
    {
    message("Bins are not the same length, sampling larger bin.")
    samp = sample(r1, length(r2))
    ct = cor.test(samp,r2, method=method)
    }
  else
    {
    message("Bins are not the same length, sampling larger bin.")
    samp = sample(r2, length(r1))
    ct = cor.test(samp,r1, method=method)
    }
  
  ct$data.name = paste(var, "Bin counts:", var.counts[1], ",", var.counts[2])
  print(ct)
  return(ct)
  }



load.cpg<-function(cpgfile, cpgI.only=T)
  {
  cgd = read.table(cpgfile, sep=" ")
  # Col 1 values:  0 -> non   1 -> CpG
  # Col 2: position
  # Col 3: 2 -> + strand, 1 -> - strand
  # Col 4/5: 800bp range around CpG
  # Col 6: Probability of methylation
  # Col 7: Probability unmethylated
  # Col 8: Duplicate of 1, CGI-> CpG Island, NCGI-> non
  colnames(cgd) = c('CpG', 'Pos', 'Strand', 'RangeS', 'RangeE', 'Meth.Prob', 'Unmeth.Prob', 'Named')
  if (cpgI.only) cgd = cgd[cgd$CpG == 1,]  # Only want cpg islands
  return(cgd)
  }

load.data<-function(gcfile, varfile)
  {
  gd = read.table(gcfile, header=T, sep="\t")
  vd = read.table(varfile, header=T, sep="\t")
  
  # Get ratios for each bin  
  gd$UnkRatio = gd$Unk/gd$BPs
  # Can't have a GC ratio if the sequence is unknown
  gd$GC[ which(gd$UnkRatio == 1)  ] = NA
  gd$GCRatio = gd$GC/gd$BPs

  if (nrow(vd) != nrow(gd)) stop(paste("The bin sizes don't match for chromosome", chr))

  # Remove NA values
  gcBins = which(!is.na(gd$GCRatio))
  vd = vd[gcBins,] 
  gd = gd[gcBins,]
  if (nrow(vd) != nrow(gd)) stop("The bin sizes don't match after removing NA values")

  return(list("vars" = vd, "gc" = gd))
  }

# Load data and set up ratios
gc.data<-function(file)
  {
  gd = read.table(file, header=T, sep="\t")
  # Get ratios for each bin  
  gd$UnkRatio = gd$Unk/gd$BPs
  # Can't have a GC ratio if the sequence is unknown
  gd$GC[ which(gd$UnkRatio == 1)  ] = NA
  gd$GCRatio = gd$GC/gd$BPs
  return(gd)
  }

total.bp<-function(gcdata)
  {
  return(sum(gd$BPs))
  }


ks.test.all<-function(vars, chr)
  {
  test = as.data.frame(matrix(nrow=2, ncol=0))
  rownames(test) = c('norm', 'pois')
  for(var in names(vars))
    {
    freq = table(vars[[var]])
    kpois = ks.test(freq,ppois,mean(freq))
    knorm = ks.test(freq,pnorm,mean(freq),sd(freq))
    
    kpois$data.name = paste(chr, var, "frequency (poisson)")
    knorm$data.name = paste(chr, var, "frequency (normal)")
    
    #test['pois', var] = format.pval(kpois$p.value, digits = 3)
    #test['norm', var] = format.pval(knorm$p.value, digits = 3)
    
    test['pois', var] = kpois$p.value
    test['norm', var] = knorm$p.value
    }
  return(as.data.frame(t(test)))
  }

