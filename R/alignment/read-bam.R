library('rbamtools')

source("lib/bam_funcs.R")

load_files<-function(files, dir)
{
  for (f in files)
  {
    f = paste(dir, f, sep="/")
    load(f)
    if (exists("means_var"))
      means_var = rbind(means_var, means)
    else
      means_var = means
    
    rownames(means_var) = c(1:nrow(means_var))
  }
  rm(means)
  return(means_var)
}


args <- commandArgs(trailingOnly = TRUE)
print(args)

#args = c("/Volumes/exHD-Killcoyne/TCGA/sequence/patients/0a2475da-42bb-4adb-b86c-5492511e09f1/foo.bam")

print(getwd())

`%nin%` <- Negate(`%in%`) 

bands=read.table("band_genes.txt", header=T)
arms = bands[ which(bands$chr == '1' & bands$band %nin% c('p11','q11')), ]

rdata_dirs = vector(mode="character", length=length(args))
for (bam in args)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  
  print(isOpen(reader))
  
  current_dir = dirname(bam)
  rdata_dirs = append(rdata_dirs, current_dir)
  
  referenceData = referenceData[ grep("^(chr)?(\\d+|Y|X)$", as.vector(referenceData$SN), perl=T ), ]
  for (i in 1:nrow(referenceData))
    {
    chr_id = referenceData[i,]

  print("Centromeres")
  cm = range(get_band_range(bands, referenceData[chr_id,'SN'], c('p11','q11'))[,c('start','end')])
  centromere = c(referenceData[1,'ID'],cm)
  ## random
  means = run_test(90, reader, centromere, 5000)
  save(means, file=paste(current_dir, "centromere_means.RData", sep="/"))
  rm(means)

  for (band in sample(arms$band, 6))
    {
    print( paste("Band", band, sep=" "))
    cm = range(bands, get_band_range(referenceData[chr_id,'SN'], c(band))[,c('start','end')])
    st = run_test(30, reader, c(referenceData[chr_id, 'ID'],cm), 5000)
    if (exists("means")) means = rbind(means, st$means)
    else means = st$means
  
    if (exists("counts")) counts = rbind(counts, st$counts)
    else counts = st$counts
    
    print(nrow(means))
    }
  
  filename = paste(chr_id$SN, "band_means.RData", sep=".")
  save(means, file=paste(current_dir, filename, sep="/"))

  rm(means, counts)
  }
  bamClose(reader)
  }



dir = dirname(dirname(args[1]))
files = list.files(dir, ".RData$", recursive=T)

band_files = grep('band_means', files, value=T)
cent_files = grep('centromere_means', files, value=T)

band_means = as.data.frame(load_files(band_files, dir))
cent_means = as.data.frame(load_files(cent_files, dir))


print( t.test(cent_means$ppair.mean, band_means$ppair.mean) )# not very different
print( t.test(cent_means$ppair.sd, band_means$ppair.sd) )# not significant but does suggest some variability

print( t.test(cent_means$disc.mean, band_means$disc.mean) )# different
print( t.test(cent_means$disc.sd, band_means$disc.sd) )# different


par(mfrow=c(1,2))
plot(sort(cent_means$disc.mean), type='o',col='red')
plot(sort(band_means$disc.mean), type='o', col='blue')



ks.test(cent_means$disc.mean, pnorm, mean(cent_means$disc.mean,na.rm=T), sd(cent_means$disc.mean, na.rm=T)) # close to normal?
ks.test(cent_means$disc.mean, ppois, mean(cent_means$disc.mean,na.rm=T)) # not poisson?


length(na.omit(band_means$disc.mean))/nrow(band_means)
length(na.omit(cent_means$disc.mean))/nrow(cent_means)





