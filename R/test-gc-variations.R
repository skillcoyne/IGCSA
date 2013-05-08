#
# Analysis script.
# Compares sequential bins based on either GC content or just chunks of the genome (total bps/10) using t-test to identify best method to break down
# variations for use in genome generation.
#
rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/var-ttests.R")

ens_dir = "~/Data/VariationNormal/Frequencies/1000/Ensembl"
var_files = list.files(path=ens_dir, pattern="*.txt")

gc_dir = "~/Data/VariationNormal/GC/1000"
gc_files = list.files(path=gc_dir, pattern="*-gc.txt")

out_dir = "~/Analysis/Normal"


result = tryCatch({
  load("~/Analysis/Database/normal/vars.Rdata")
}, warning = function(w) {
  warning(paste(gettext(w), "Run the variation-table script in /database_normal first.", sep=""))
  exit(-1)
}, error = function(e) {
  print("Missing list of variations.  Run the variation-table script in /database_normal first.")
  exit(-1)
})


rho = data.frame()
for (i in 1:length(var_files))
  {
  file = var_files[i]
  chr = sub(".txt", "", file)
  if (chr == 'chrX' | chr == 'chrY') next
  print(chr)

  # Variation & gc files
  gc_f = paste(gc_dir, paste(chr, "-gc.txt", sep=""), sep="/")
  var_f = paste(ens_dir, file, sep="/")

  data = load.data(gc_f, var_f)
  vd = data$vars; gd = data$gc
  cg = cbind(vd, gd)
  
  chr = sub("chr", "", chr)
  bp.test = test.bp.bins(cg, 10, variations) 
  bp.cor = plot.var.cor(bp.test,T)
  rho[i, 'chr'] = chr
  rho[i, 'bp.rho'] = bp.cor['rho', 'SNV']
  
  gc.test = test.gc.bins(cg, 10, variations) 
  
#  chr_dir = paste(out_dir, chr, sep="/")
#  png(filename=paste(chr_dir, "gc-bins.png", sep="/"), bg="white", height=900, width=900)
  gc.cor = plot.var.cor(gc.test,T)
#  dev.off()
  rho[i, 'gc.rho'] = gc.cor['rho', 'SNV']

  for (var in variations)
    {
    if ( ! var %in% colnames(cg) ) cg[,var] = NA
    }
  
  if (!exists("all_cg")) all_cg = cg else all_cg = rbind(all_cg,cg)
  #print(nrow(all_cg))

  rm(data,vd,gd,cg)
  }

# sort by GC high -> low
all_cg = all_cg[order(-all_cg[,'GC']),]
all_cg[1:10,] # just double check
# reorder, variations first GC/UNK etc last
all_cg = all_cg[, c( which( colnames(all_cg) %in% variations ), which( !colnames(all_cg) %in% variations ) )]

## So there's huge sections of the genome that do not include most of the variations so just test the variations?? ##
all_gc_tests = list()
all_bp_tests = list()
for (var in variations)
  {
  rows = all_cg[ !is.na(all_cg[var]), ]
  title = paste(var, nrow(rows),  sep=": ") 
  all_gc_tests[[title]] = test.gc.bins(rows, 10, c(var)) 
  all_bp_tests[[title]] = test.bp.bins(rows, 10, c(var)) 
  }

gc.tests = test.gc.bins(all_cg, 10, variations)
bp.tests = test.bp.bins(all_cg, 10, variations)

filename=paste(out_dir, "whole-genome-bins.txt", sep="/")
write("# Ordered by GC high->low, split into bins by total fragments (e.g. 2684800/10) #", file=filename)
write.table(bp.tests, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")

#png(filename=paste(out_dir, "whole-genome-by-bp.png", sep="/"), bg="white", height=900, width=900)
#bp.cor = plot.var.cor(bp.tests, plot=T)
#dev.off()

write(" # Correlation values by bp bins", file=filename, app=T)
write.table(bp.cor, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")
write("# Broken down to fragments that include only a given variation #", file=filename, app=T)

for(n in names(all_bp_tests))
  {
  write(n, file=filename, app=T)
  write.table(all_bp_tests[[n]], quote=F, col.name=NA, row.name=T, app=T, sep="\t")
  }

write("  ", file=filename, app=T)
write("# Split by each 10% GC content low->high (e.g. 908/10) Bin size varies #", file=filename, app=T)
write.table(gc.tests, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")
write("# Broken down to fragments that include only a given variation #", file=filename, app=T)

for(n in names(all_gc_tests))
  {
  write(n, file=filename, app=T)
  write.table(all_gc_tests[[n]], quote=F, col.name=NA, row.name=T, app=T, sep="\t")
  }

#png(filename=paste(out_dir, "whole-genome-by-gc.png", sep="/"), bg="white", height=900, width=900)
#gc.cor = plot.var.cor(gc.tests, plot=T)
#dev.off()

write(" # Correlation values by GC bins", file=filename, app=T)
write.table(gc.cor, file=filename, app=T, col.name=T, row.name=T, quote=F, sep="\t")



png(filename=paste(out_dir, "whole-genome-bins.png", sep="/"), bg="white", height=900, width=900)
rho = rho[order(as.numeric(rho[,'chr'])),]
par(mfrow=c(2,1))
plot(rho[,'bp.rho'], type='o', xaxt='n', col='blue', main="BP bin, rho", ylab='SNV', xlab='Chromosome', sub="Line = rho across genome")
axis(1, at=1:nrow(rho), labels=rho$chr)
abline(bp.cor['rho', 'SNV'], 0, col='red')

plot(rho[,'gc.rho'], type='o', xaxt='n', col='blue', main="GC bin, rho", ylab='SNV', xlab='Chromosome')
axis(1, at=1:nrow(rho), labels=rho$chr)
abline(gc.cor['rho', 'SNV'], 0, col='red')
dev.off()



