rm(list = ls())
plot = FALSE
binsize = "100"

setwd(paste("~/Data/VariationNormal/GC/", binsize, sep=""))

if (file.exists("allgc.RData"))
  {
  load("allgc.RData")
  }
else
  {
  files = list.files(pattern=".txt")  
  if (plot == TRUE) { par(mfrow=c(5,5)) }

  for (f in files)
    {
  d = read.table(f, header=T, sep="\t") 
  
  d$GCRatio = d$GC/d$BPs
  d$UnkRatio = d$Unk/d$BPs
  #d$GapRatio = d$Gap/d$BPs

  # Can't know GC content in these regions
  d[d$UnkRatio == 1,][, 'GC'] = NA
  d[d$UnkRatio == 1,][, 'GCRatio'] = NA
  #d[d$GapRatio == 1,][, 'GC'] = NA
  
  # just interesting to note that many variations (not all) would appear to occur middle->end of the chromosome. Also that a few of the shorter chromosomes have none
  if (plot == TRUE) # Very difficult to run for each chromosome as they are dense
    { 
    def.new()
    s = 1
    n = nrow(d)
    # GC Ratio
    plot(d$GCRatio[s:n], col="blue", pch=19, ylim=c(0,1), ann=FALSE)
    # Unk Ratio
    lines(d$UnkRatio[s:n], col="red", pch=18, type="p")
    title(main=f)
    title(xlab="Chr location")
    title(ylab="bp Ratio")
    drange = range(0, d$GCRatio[s:n], d$UnkRatio[s:n])
    legend(1, drange[2], c("unk", "gc"), col=c("red", "blue"), pch=18:19)
    }
  
  if(!exists("gcd")) gcd = d
  else gcd = rbind(gcd, d)
  rm(d)
  }
  save.image(file="allgc.RData")
  }
  
gc_f = table(gcd$GCRatio)
unk_f = table(gcd$UnkRatio)

plot(gc_f, main="GC Ratios Across Genome", xlab="Ratio", ylab="freq", type="h")

lgc = log(gc_f)
ks.test(lgc,ppois,mean(lgc),sd(lgc))

# Suggests it is poisson
poisson.test(sum(gc_f), mean(gc_f))

probability_list=ppois(lgc,mean(lgc),sd(lgc))
# log or not? 
chisq.test(lgc)

# hrm...not understanding
#library(vcd) ## loading vcd package
#gf = goodfit(gc_f,type="poisson",method= "MinChisq")
#summary(gf)
#predict(gf, type="prob")
#plot(gf,main=paste("Count data vs ", gf$type, " distribution"))  

