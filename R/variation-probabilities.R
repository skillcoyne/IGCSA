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
  
  snp_f = table(d$SNP)
  indel_f = table(d$INDEL)

  # doesn't make a difference that I see
  #counts = vector('numeric', length(snp_f))  
  #names(counts)=1:length(snp_f)
  #names(snp_f) = names(counts)
  
  ks.test(snp_f,pnorm,mean(snp_f),sd(snp_f))

  ks.test(snp_f,ppois,mean(snp_f),sd(snp_f))
  
  probability_list=pnorm(snp_f,mean(snp_f),sd(snp_f))
  
  
  poisson.test(sum(snp_f), length(snp_f), 0)
  probability_list=ppois(snp_f,mean(snp_f),sd(snp_f))
  
  funplot(probability_list)
  
  write.table(snp_f, row.names=F, quote=F)
  
  
  }