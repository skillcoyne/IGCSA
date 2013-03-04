rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")


plotVariations<-function(vars, chr)
  {
  varnames = names(vars[1:7])
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


# ------------------ # MAIN # ------------------ #
#args = commandArgs(trailingOnly = TRUE)
#ens_dir = args[1]
#gc_dir = args[2]

plot=TRUE

ens_dir = "/Users/sarah.killcoyne/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = "/Users/sarah.killcoyne/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir,pattern=".txt")  


rnames = c('Chr', 'GC', 'Total.Vars', 'Bins')
seq_ratios = data.frame()
seq_ratios = as.data.frame(matrix(nrow=length(var_files), ncol=length(rnames)))
colnames(seq_ratios) = rnames

setwd("/Users/sarah.killcoyne/Data/VariationNormal/")
colors=rainbow(length(var_files))
plot(0:300, ann=F, type='n', ylim=c(0,10))
par(mfrow=c(4,5))
for (i in 1:length(var_files))
  {
  file = var_files[i]
  chr = sub(".txt", "", file)
print(chr)
  chrdir = paste(getwd(), chr, sep="/")
  if (!file.exists(chrdir)) dir.create(chrdir)  

  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")
  # Data with NA removed
  data = load.data(gc_f, var_f)
  var_d = data$vars
  gc_d = data$gc
  
  #test = ks.test.all(var_d[1:6], chr) 
  #plot(test$norm, col='blue', type='o', ann=F, xaxt='n')
  #lines(test$pois, col='red', type='o')
  #axis(1, at=1:nrow(test), labels=rownames(test))
  #title(main=chr)
  
  #write.table(paste("###", chr, "###"),  file="VariationTests.txt", row.names=F, col.names=F, quote=F, append=T)
  #write.table(test, file="VariationTests.txt", quote=F, sep="\t", append=T)
  
  all = cbind(var_d, gc_d)

  nozero = table(all$SNV[all$SNV > 0])
  topvalues = nozero[ which(nozero == max(nozero)) ]
  write.table(all[all$SNV == names(topvalues) & !is.na(all$SNV),], quote=F, sep="\t", file=paste(chrdir, "dist2.txt", sep="/"))
  
  if (!nrow(var_d) == nrow(gc_d)) { stop("Bins don't match, quitting") }

  # Collect info about each chromosome
  seq_ratios[i, 'Chr'] = chr
  seq_ratios[i, 'Length'] = nrow(gc_d)
  
  seq_ratios[i, 'GC'] = round((sum(gc_d$GC)/sum(gc_d$BPs)), digits=2)
  seq_ratios[i, 'Total.Vars'] = sum(var_d[,1:7], na.rm=T)  

  
  if (plot)
    {
    plot_file = paste(chrdir, chr, sep="/")
    png(filename=paste(plot_file, "-overview.png", sep=""), bg="white", height=900, width=600)
    par(mfrow=c(2,1))
    plotVariations(all, chr) 
    plotRatios(gc_d, chr)
    dev.off()
    }
  
  all$Total.Vars = rowSums(all[,1:7], na.rm=T)
  freq = table(all$Total.Vars)
  #lines(log(freq), type='l', col=colors[i])

  if (names(freq[ freq == max(freq) ]) != '0') { warning( paste(chr, "variation max frequency is not 0")  ) }
  
  # so ignoring 0 which is the start of the poisson     
  seq_ratios[i, 'Max1'] = names(freq[freq == max(freq)])
  freq = freq[2:length(freq)]        
  seq_ratios[i, 'Max2'] = names(freq[freq == max(freq)])
  
  if( !exists("sv_data") )  sv_data = all[,2:7]  else sv_data = rbind(sv_data, all[,2:7])
  
  #rm(var_d)
  #rm(gc_d)
  }
legend("topright", legend=seq_ratios$Chr, col=colors, fill=colors)
title(main="Log Variation Frequency per Chromosome", xlab="1kb bin counts", ylab="log(count frequency)")

# cheap way to get a legend
plot(test, axes=F, ann=F, type='n')
legend("topleft", legend=colnames(test), col=c('blue', 'red'), fill=c('blue', 'red'), bty='n')

seq_ratios = seq_ratios[order(seq_ratios$GC),]
if (plot)
  {
  png(filename=paste(getwd(),"/GC-content", ".png", sep=""), bg="white", height=600, width=900)

  plot(seq_ratios$GC, ann=F, xaxt='n', type='n', ylim=c(0.01, max(seq_ratios$GC)))
  lines(seq_ratios$GC, ann=F, xaxt='n', col='blue', type='o', pch=19)
  axis(1, at=1:nrow(seq_ratios), lab=seq_ratios$Chr)
  text(seq_ratios$GC, col='blue', pos=1, labels=seq_ratios$GC )

  lines((seq_ratios$Total.Vars/seq_ratios$Length)/100, type='o', col='red', pch=19)
  text((seq_ratios$Total.Vars/seq_ratios$Length)/100, col='red', pos=1, labels=round(seq_ratios$Total.Vars/seq_ratios$Length, 2)) 
  legend("topleft", legend=c('GC Ratio', 'Variations/Length'), fill=c('blue', 'red') )
  title(main='GC Content Per Chromosome', sub="Subplotted Variations per bin", ylab='GC Ratio')
  dev.off()
  }

sv_data$Total = rowSums(sv_data)

if (plot)
  {
  png(filename=paste(getwd(), "all-strucvar.png", sep="/"), bg="white", height=800, width=950)
  par(mfrow=c(2,3))
  for (var in colnames(sv_data[1:6]))
    {
    freq = table(sv_data[[var]])
    kpois = ks.test(freq,ppois,mean(freq))
    knorm = ks.test(freq,pnorm,mean(freq),sd(freq))
  
    test = ""
    if (knorm$p.value > kpois$p.value)
      {
      k = kpois 
      test = "poisson" 
      }
    else 
      {
      k = knorm
      test = "norm"
      }
    print( paste(var, "p-value:", format.pval(k$p.value, 4), test)  )
    plot(log(freq), main=var, xlab=paste(test, format.pval(k$p.value, 4)), cex.main=2, cex.lab=1.5, cex.axis=1.5, col=sample(rainbow(6),1))
    }
  dev.off()
  }
  
#svfreq = table(sv_data$Total)
#plot(log(svfreq))
#ks.test(svfreq,ppois,mean(svfreq),sd(svfreq))

