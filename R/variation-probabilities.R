# http://cran.r-project.org/doc/contrib/Ricci-distributions-en.pdf
# clear workspace


# --------------Functions---------------------- #
normplot<-function(scores)
  {
  #and now a (vaguely) pretty pic just for ewe
  plot(function(x) dnorm(x,mean(scores),sd(scores)), min(scores)-sd(scores)*1,max(scores)+sd(scores)*1,
       ylab="Density",xlab="SNP Freq per 1kb")
  xpos=vector("numeric",2)
  ypos=vector("numeric",2)
  ypos[1]=0
  density_pos=dnorm(scores,mean(scores),sd(scores))
  labels = names(scores)
  for(i in 1:length(scores))
    {
    xpos[1]=scores[i]
    xpos[2]=scores[i]
    ypos[2]=density_pos[i]
    lines(xpos,ypos)
    text(xpos[2],ypos[2],labels=labels[i],pos=3)
    }
  }

histp<-function(expected)
  {
  h <- hist(expected,breaks=length(table(expected)))
  xhist <- c(min(h$breaks),h$breaks)
  yhist <- c(0,h$density,0)
  xfit <- min(expected):max(expected)
  yfit <- dpois(xfit,lambda=lambda.est)
  plot(xhist,yhist,type="s",ylim=c(0,max(yhist,yfit)), main="Poison density and histogram")
  lines(xfit,yfit, col="red")
  }
# --------------/Functions---------------------- #

rm(list = ls())
setwd("~/Data/VariationNormal/Frequencies/1000/Ensembl")
files = list.files(pattern=".txt")  

for (f in files)
  {
  d = read.table(f, header=T, sep="\t") 
  # just interesting to note that many variations (not all) would appear to occur middle->end of the chromosome. Also that a few of the shorter chromosomes have none
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


# Ok, so looking around using a generalized linear model to look into possible fit?
# Pretty sure this tells me poisson isn't appropriate. deviance/df should be ~ 1 but is not
model = glm(snp_c~1, family="poisson")
summary(model)

# I'm presuming number of events is total number of bins.  Anyhow, this is just the same as the glm really 
poisson.test(sum(snp_c), mean(snp_c))

# The pvalue is too extreme, so either I'm doing the test wrong (very possible) or...the number of bins per number of snps is not independent? 
chisq.test(snp_c)

# ks complains about ties...
# however, it would see to be normal?
ks.test(snp_c,pnorm,mean(snp_c),sd(snp_c))
#probability_list=pnorm(snp_c,mean(snp_c),sd(snp_c))
#normplot(log(probability_list)) # again, is this useful? I'm thinking not so much

# Example poisson distribution with generated data (generates warnings)
n = 100
x.poi = rpois(n=n, lambda=2.5)
lambda.est = mean(x.poi)
tab.os = table(x.poi)
freq.os = vector()
for(i in 1: length(tab.os)) freq.os[i]<-tab.os[[i]]  ## vector of emprical frequencies
freq.ex = (dpois(0:max(x.poi),lambda=lambda.est)*length(x.poi)) ## vector of fitted (expected) frequencies
acc = mean(abs(freq.os-trunc(freq.ex))) ## absolute goodness of fit index acc
acc/mean(freq.os)*100 ## relative (percent) goodness of fit index
histp(x.poi)

# So real data? Using the "by the book" example it looks sort of poisson...I'm lost 
x.poi = var_data$SNP
lambda.est = mean(x.poi)
tab.os = table(x.poi) # this is snp_c
freq.os = vector()
for(i in 1: length(tab.os)) freq.os[i]<-tab.os[[i]]  ## vector of emprical frequencies
freq.ex = (dpois(0:max(x.poi),lambda=lambda.est)*length(x.poi)) ## vector of fitted (expected) frequencies
acc = mean(abs(freq.os-trunc(freq.ex))) ## absolute goodness of fit index acc
acc/mean(freq.os)*100 ## relative (percent) goodness of fit index
histp(x.poi)

# Again, I just found this.  It seems to suggest that it fits a negative binomial?  
# No matter what data I use (real or generated) I get a warning
library(vcd) ## loading vcd package
gf = goodfit(log(snp_c+1),type= "nbinomial",method= "MinChisq")
summary(gf)
plot(gf,main=paste("Count data vs ", gf$type, " distribution"))  

# but not poisson (I think)
gf = goodfit(log(snp_c+1),type= "poisson",method= "MinChisq")
summary(gf)
plot(gf,main=paste("Count data vs ", gf$type, " distribution"))  

# But...a ks test for a negative binomial doesn't work!
ks.test(snp_c,pnbinom,mean(snp_c),sd(snp_c))



  
  
  
  
  
  
  
  