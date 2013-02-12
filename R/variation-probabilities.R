# http://cran.r-project.org/doc/contrib/Ricci-distributions-en.pdf

funplot<-function(scores)
  {
  #and now a (vaguely) pretty pic just for ewe
  plot(function(x) dnorm(x,mean(scores),sd(scores)), min(scores)-sd(scores)*1,max(scores)+sd(scores)*1,
       ylab="Density",xlab="SNP Freq per 1kb")
  xpos=vector("numeric",2)
  ypos=vector("numeric",2)
  ypos[1]=0
  density_pos=dnorm(scores,mean(scores),sd(scores))
  for(i in 1:length(scores))
    {
    xpos[1]=scores[i]
    xpos[2]=scores[i]
    ypos[2]=density_pos[i]
    lines(xpos,ypos)
    text(xpos[2],ypos[2],labels=i,pos=3)
    }
  }


setwd("~/Data/variation/frequencies")

files = list.files()

#for (f in files)
  {
  f = "chr1-counts.txt"
  print(f)
  
  d = read.table(f, header=T, sep="\t")
  nrow(d)
  plot(d$SNP)
  
  snp_e = unique(d$SNP)

  indel_e = unique(d$INDEL)

  snp_f = table(d$SNP)
  #indel_f = table(d$INDEL)

  
  # poisson does give a good pvalue
  pt = poisson.test(length(snp_e), mean(snp_e))
  
  probability_list=ppois(snp_e,mean(snp_e),sd(snp_e))
  funplot(probability_list)
  
  library(vcd) ## loading vcd package
  gf <- goodfit(snp_e,type= "poisson",method= "MinChisq")
  summary(gf)
  plot(gf,main="Count data vs Poisson distribution")  
  
  
  
  # Estimate poisson distribution?  
  #expected=rpois(n=100, lambda=mean(snp_e))
  pn = 200
  expected=rpois(n=pn, lambda=pt$estimate)
  hist(expected, main="Poisson Distribution?")
  lambda.est = mean(expected) ## estimate of parameter lambda...why???
  tab.os = table(expected) ## table with empirical frequencies  
  freq.os<-vector()
  for(i in 1: length(tab.os)) freq.os[i]<-tab.os[[i]]  ## vector of emprical frequencies

  freq.ex<-(dpois(0:max(expected),lambda=lambda.est)*pn) ## vector of fitted (expected) frequencies
  acc <- mean(abs(freq.os-trunc(freq.ex))) ## absolute goodness of fit index acc
  acc/mean(freq.os)*100 ## relative (percent) goodness of fit index
  
  h <- hist(expected,breaks=length(tab.os))
  xhist <- c(min(h$breaks),h$breaks)
  yhist <- c(0,h$density,0)
  xfit <- min(expected):max(expected)
  yfit <- dpois(xfit,lambda=lambda.est)
  plot(xhist,yhist,type="s",ylim=c(0,max(yhist,yfit)), main="Poison density and histogram")
  lines(xfit,yfit, col="red")
  
  
  library(vcd) ## loading vcd package
  gf <- goodfit(expected,type= "poisson",method= "MinChisq")
  summary(gf)
  plot(gf,main="Count data vs Poisson distribution")  
  
  
  
  
  
  
  
  
  
  
  }