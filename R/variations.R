plotVariations<-function(vars, gc, chr)
  {
  varnames = names(vars)
  plot(vars[[1]], type='n', ann=F)
  colors = rainbow(ncol(vars))
  names(colors) = varnames
  for(var in varnames)
    {
    lines(vars[[var]], type='h', col=colors[var])  
    }
  lines(gc$Unk, type='h', col="grey")

  legend("topright", legend=append(varnames, 'Unknown Sequence'), col=append(colors, 'grey'), fill=append(colors,'grey'))
  title(main=paste(chr, sep=""), ylab="Variation Count per bin", xlab="Chromosome position, 1kb bins")
  }
	
plotVariationsSep<-function(vars, gc, chr, dir)
  {
  varnames = names(vars)
  colors = rainbow(ncol(vars))
  names(colors) = varnames
  for(var in varnames)
    {
    plot_file = paste(dir, chr, sep="/")	
    png(filename=paste(plot_file, "-", var, ".png", sep=""), bg="white", height=600, width=600)
    par(mfrow=c(2,1))
    
    plot(vars[[var]], type='h', col=colors[var], main=paste(chr, var), xlab=paste(var, "counts"), ylab="Chromosome position per 1kb")  
    lines(gc$Unk, type='h', col='grey')
    
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

# ------------------ # MAIN # ------------------ #
#args = commandArgs(trailingOnly = TRUE)
#ens_dir = args[1]
#gc_dir = args[2]



plot=FALSE

ens_dir = "/Users/sarah.killcoyne/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = "/Users/sarah.killcoyne/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir,pattern=".txt")  


rnames = c('Chr', 'GC', 'Total.Vars', 'Bins')
seq_ratios = data.frame()
seq_ratios = as.data.frame(matrix(nrow=length(var_files), ncol=length(rnames)))
colnames(seq_ratios) = rnames

#var_files = c('chr17.txt')
for (i in 1:length(var_files))
  {
  file = var_files[i]
  chr = sub(".txt", "", file)
  gcfile = paste(chr, "-gc.txt", sep="")

  var_f = paste(ens_dir, file, sep="/")
  gc_f = paste(gc_dir, gcfile, sep="/")

  var_d = read.table(var_f, header=T, sep="\t")
  gc_d = read.table(gc_f, header=T, sep="\t")


  gc_d$GCRatio = gc_d$GC/gc_d$BPs
  gc_d$UnkRatio = gc_d$Unk/gc_d$BPs

  gc_d$UnkRatio[gc_d$Unk == 0 ] = NA
  gc_d$GCRatio[gc_d$GC == 0 ] = NA

  if (!nrow(var_d) == nrow(gc_d)) { stop("Bins don't match, quitting") }

  seq_ratios[i, 'Chr'] = chr
  seq_ratios[i, 'Length'] = nrow(gc_d)
 
  
  seq_ratios[i, 'GC'] = round((sum(gc_d$GC)/sum(gc_d$BPs)), digits=2)
  total_vars = 0
  for (var in names(var_d)) { total_vars = total_vars + sum(var_d[[var]]) }
  seq_ratios[i, 'Total.Vars'] = total_vars
  

  # Counts can't matter if the sequence was all unknown (the counts would all be 0)
  var_d[which(gc_d$UnkRatio == 1) ,] = NA

  setwd("/Users/sarah.killcoyne/Data/VariationNormal/")
  chrdir = paste(getwd(), chr, sep="/")
  if (!file.exists(chrdir)) { dir.create(chrdir) }

  if (plot)
    {
    plotVariationsSep(var_d, gc_d, chr, chrdir) 
    plot_file = paste(chrdir, chr, sep="/")
    png(filename=paste(plot_file, "-overview.png", sep=""), bg="white", height=900, width=600)
    par(mfrow=c(2,1))
    plotVariations(var_d, gc_d, chr) 
    plotRatios(gc_d, chr)
    dev.off()
    }
  }

seq_ratios = seq_ratios[order(seq_ratios$GC),]

png(filename=paste(getwd(),"/GC-content", ".png", sep=""), bg="white", height=600, width=900)

plot(seq_ratios$GC, ann=F, xaxt='n', type='n', ylim=c(0.01, max(seq_ratios$GC)))
lines(seq_ratios$GC, ann=F, xaxt='n', col='blue', type='o', pch=19)
axis(1, at=1:nrow(seq_ratios), lab=seq_ratios$Chr)
text(seq_ratios$GC, col='blue', pos=1, labels=seq_ratios$GC )

lines((seq_ratios$Total.Vars/seq_ratios$Length)/100, type='o', col='red', pch=19)
text((seq_ratios$Total.Vars/seq_ratios$Length)/100, col='red', pos=1, labels=round(seq_ratios$Total.Vars/seq_ratios$Length, 2)) 
legend("topleft", legend=c('GC Ratio', 'Variations/Length'), fill=c('blue', 'red') )
dev.off()


title(main='GC Content Per Chromosome', sub="Subplotted Variations per bin", ylab='GC Ratio')


