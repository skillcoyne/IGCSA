rm(list = ls())
plot = FALSE
binsize = "1000"

setwd(paste("~/Data/VariationNormal/Frequencies-OLD/", binsize, sep=""))
files = list.files(pattern=".txt")  

if (plot == TRUE) { par(mfrow=c(5,5)) }
for (f in files)
  {
  d = read.table(f, header=T, sep="\t") 
  # just interesting to note that many variations (not all) would appear to occur middle->end of the chromosome. Also that a few of the shorter chromosomes have none
  if (plot == TRUE) { plot(d$SNP, main=f, xlab="Chr Location", ylab="SNPs in bin") }

  if(!exists("var_data"))   var_data = d
  else var_data = rbind(var_data, d)
  rm(d)
  }
max_snps = max(var_data$SNP)

# set up vector for all counts
snp_c = vector('numeric', max_snps+1)
# just useful to keep in mind
snps_in_bin = 0:max_snps
names(snp_c) = snps_in_bin

# Count all bins such that snp counts not found have a 0 count
snp_f = table(var_data$SNP)
for (n in names(snp_f)) snp_c[[n]] = snp_c[[n]] + snp_f[[n]]

# Just for reference. The number of bins is equal to sum of observations
total_bins = sum(snp_c)

# doubt this makes a difference but since the observations start at 0 lets just start them at 1 in case 0 screws things up
# names(snp_c) = 1:(max_snps+1)

# not useful but what the hell
print(snp_c)
plot(log(snp_c), xlab=paste(binsize, " bp bins"), ylab="log(freq)", pch=19, main="SNP Counts from 1000genomes Data", sub="Normal Variation") 