
plotVariations<-function(vars, chr)
{
  if (chr == 'chrY') end = 5 else end = 7
  varnames = names(vars[1:end])
  plot(vars[[1]], type='n', ann=F)
  colors = rainbow(ncol(vars))
  names(colors) = varnames
  for(var in varnames)
  {
    lines(vars[[var]], type='h', col=colors[var])  
  }
  lines(vars$Unk, type='h', col="grey")
  
  legend("topright", legend=append(varnames, 'Unknown Sequence'), col=append(colors, 'grey'), fill=append(colors,'grey'))
  title(main=paste(chr, sep=""), ylab="Variation Count per bin", xlab="Chromosome position, 1kb bins")
}

plotVariationsSep<-function(vars, chr, dir)
{
  colors = rainbow(length(names(vars)))
  names(colors) = names(vars)
  for(var in names(vars))
  {
    freq = table(vars[[var]])
    plot_file = paste(dir, chr, sep="/")	
    png(filename=paste(plot_file, "-", var, ".png", sep=""), bg="white", height=600, width=600)
    par(mfrow=c(2,1))
    
    plot(vars[[var]], type='h', col=colors[var], main=paste(chr, var), xlab=paste(var, "counts"), ylab="Chromosome position per 1kb")  
    lines(vars$Unk, type='h', col='grey')
    
    freq = table(vars[[var]])
    plot(log(freq), ylab="log(count frequency)", xlab=paste("Number of ", var), main=chr, col='black') 
    dev.off()
  }
}

plotRatios<-function(gcu, chr)
{
  plot(gcu$GCRatio, type='h', ann=F, ylim=c(0,1), col='blue', pch=20)
  lines(gcu$UnkRatio, type='h', col='red', pch=18)
  
  legend("topright", legend=c('GCRatio', 'UnkRatio'), col=c('blue', 'red'), fill=c('blue', 'red'))
  title(main=chr, ylab="Ratio per 1kb", xlab="Chromosome position, 1kb bins")
}

testZeros<-function(vars)
{
  par(mfrow=c(4,3))
  comb = combn(names(vars), 2)
  for (i in 1:ncol(comb))
  {
    current = comb[,i]
    freq = table( vars[[ current[1] ]], vars[[ current[2] ]] )
    plot(freq, main=current)
  }
}