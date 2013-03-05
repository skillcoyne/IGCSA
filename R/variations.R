rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")


#args = commandArgs(trailingOnly = TRUE)
#ens_dir = args[1]
#gc_dir = args[2]

data_dir = "~/Data/VariationNormal"
setwd(data_dir)

plot=FALSE

ens_dir = paste(data_dir, "Frequencies/1000/Ensembl", sep="/")
var_files = list.files(path=ens_dir, pattern=".txt")  

gc_dir = paste(data_dir, "GC/1000", sep="/")
gc_files = list.files(path=gc_dir,pattern=".txt")  


rnames = c('GC', 'Total.Vars', 'Bins')
seq_ratios = data.frame()
seq_ratios = as.data.frame(matrix(nrow=0, ncol=length(rnames)))
colnames(seq_ratios) = rnames


colors=rainbow(length(var_files))
#plot(0:300, ann=F, type='n', ylim=c(0,10))
#par(mfrow=c(5,5))
#var_files = c('chr9.txt')
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
  #if (!exists("leg")) { leg = colnames(test); leg_test = test }
  
  #write.table(paste("###", chr, "###"),  file="VariationTests.txt", row.names=F, col.names=F, quote=F, append=T)
  #write.table(test, file="VariationTests.txt", quote=F, sep="\t", append=T)
  
  all = cbind(var_d, gc_d)

  nozero = table(all$SNV[all$SNV > 0])
  topvalues = nozero[ which(nozero == max(nozero)) ]
  write.table(all[all$SNV == names(topvalues) & !is.na(all$SNV),], quote=F, sep="\t", file=paste(chrdir, "dist2.txt", sep="/"))
  
  if (!nrow(var_d) == nrow(gc_d)) { stop("Bins don't match, quitting") }

  # Collect info about each chromosome
  #seq_ratios[chr, 'Chr'] = chr
  seq_ratios[chr, 'Length'] = nrow(gc_d)
  
  seq_ratios[chr, 'GC'] = round((sum(gc_d$GC)/sum(gc_d$BPs)), digits=2)
  seq_ratios[chr, 'Total.Vars'] = sum(var_d[,1:ncol(var_d)], na.rm=T)  
  seq_ratios[chr, 'Bins'] = nrow(var_d)
  
  if (plot)
    {
    plot_file = paste(chrdir, chr, sep="/")
    png(filename=paste(plot_file, "-overview.png", sep=""), bg="white", height=900, width=600)
    par(mfrow=c(2,1))
    plotVariations(all, chr) 
    plotRatios(gc_d, chr)
    dev.off()
    plotVariationsSep(var_d, chr, chrdir)
    }
  
  all$Total.Vars = rowSums(all[,1:7], na.rm=T)
  freq = table(all$Total.Vars)
  #lines(log(freq), type='l', col=colors[i])

  if (names(freq[ freq == max(freq) ]) != '0') { warning( paste(chr, "variation max frequency is not 0")  ) }
  
  # so ignoring 0 which is the start of the poisson     
  seq_ratios[chr, 'Max1'] = names(freq[freq == max(freq)])
  freq = freq[2:length(freq)]        
  seq_ratios[chr, 'Max2'] = names(freq[freq == max(freq)])
  
  rm(var_d)
  rm(gc_d)
  }
#legend("topright", legend=seq_ratios$Chr, col=colors, fill=colors)
#title(main="Log Variation Frequency per Chromosome", xlab="1kb bin counts", ylab="log(count frequency)")

# cheap way to get a legend
#plot(leg_test, axes=F, ann=F, type='n')
#legend("topleft", legend=colnames(leg_test), col=c('blue', 'red'), fill=c('blue', 'red'), bty='n')

seq_ratios = seq_ratios[order(seq_ratios$GC),]
if (plot)
  {
  png(filename=paste(getwd(),"/GC-content", ".png", sep=""), bg="white", height=600, width=1200)

  plot(seq_ratios$GC, ann=F, xaxt='n', type='n', ylim=c(0.01, max(seq_ratios$GC)))
  lines(seq_ratios$GC, ann=F, xaxt='n', col='blue', type='o', pch=19)
  axis(1, at=1:nrow(seq_ratios), lab=rownames(seq_ratios))
  text(seq_ratios$GC, col='blue', pos=1, labels=seq_ratios$GC )

  #text(1:nrow(seq_ratios), rep(0.25, nrow(seq_ratios)), labels=rownames(seq_ratios))
  
  lines((seq_ratios$Total.Vars/seq_ratios$Length)/100, type='o', col='red', pch=19)
  text((seq_ratios$Total.Vars/seq_ratios$Length)/100, col='red', pos=1, labels=round(seq_ratios$Total.Vars/seq_ratios$Length, 2)) 
  legend("topleft", legend=c('GC Ratio', 'Variations/Length'), fill=c('blue', 'red') )
  title(main='GC Content Per Chromosome', sub="Subplotted Variations per bin", ylab='GC Ratio')
  dev.off()
  }



