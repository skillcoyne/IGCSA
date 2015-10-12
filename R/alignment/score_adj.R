source("~/workspace/IGCSA/R/denovo_alignment/lib/utils.R")
source("~/workspace/IGCSA/R/denovo_alignment/lib/read_eval.R")



get.top<-function(tdf, ndf)
  {
  palette(rainbow(4))
  par(mfrow=c(2,1))
  km = sd.km(tdf$score)
  plot(tdf$score, col=km$cluster, pch=19, main=levels(tdf$type))
  top_T = tdf[which(km$cluster == which.max(km$center)), c('name','type','score')]
  
  km = sd.km(ndf$score)
  plot(ndf$score, col=km$cluster, pch=19, main=levels(ndf$type))
  top_N = ndf[which(km$cluster == which.max(km$center)), c('name','type','score')]
  
  return(setdiff(top_T$name,top_N$name))
  }

load.files<-function(path, set, type, bands)
  {
  df = NULL
  for (t in list.files(path,pattern=type, full.names=T))
      df = rbind(df, create.score.matrix(paste(t,set,sep="/"), type, bands))
  df$type = as.factor(df$type)
  df = df[order(-df$score),]
  return(df)
  }



bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")

#dir = <alignment dirs>
set = "HCC1954.G31860"
#set = "LUAD-PST"
#set = "LUAD-NB"

#rand_celline = load.files(dir,set,'Random',bands)
#gaC = load.files(dir,set,'GA',bands)
#save(gaC, rand_celline,file="celline_de_rand.Rdata")
#load(file="cellline_de_rand.Rdata")
ga_celline = gaC[sample.int(nrow(gaC), nrow(rand_celline)),]

ga_celline = ga_celline[order(-ga_celline$score),]
km = sd.km(ga_celline$score)
plot(ga_celline$score, col=km$cluster, pch=19, main=levels(ga_celline$type))
top_G = ga_celline[which(km$cluster == which.max(km$center)), c('name','type','score')]

rand_celline = rand_celline[order(-rand_celline$score),]
km = sd.km(rand_celline$score)
plot(rand_celline$score, col=km$cluster, pch=19, main=levels(rand_celline$type))
top_R = rand_celline[which(km$cluster == which.max(km$center)), c('name','type','score')]

nrow(top_G)
nrow(top_R)
top_G


#tumor = "LAML-PBT-14"
#germline = "LAML-NB-14"

#tumor = "LUAD-PST"
#germline = "LUAD-NB"

#random_tumor = load.files(dir,tumor,'Random',bands)
#random_normal = load.files(dir,germline,"Random",bands)

#ga_tumor = load.files(dir,tumor,"GA",bands)
#ga_normal = load.files(dir,germline,"GA",bands)
#save(random_tumor, random_normal, ga_tumor,ga_normal,file="luad_de_norm.Rdata")
#save(random_tumor, random_normal, ga_tumor,ga_normal,file="laml_de_norm.Rdata")
#load(file="luad_de_norm.Rdata")
#load(file="laml_de_norm.Rdata")


top_GA = get.top(ga_tumor,ga_normal)
top_Rand = get.top(random_tumor,random_normal)




fitNorm=aov(score~type,rbind(top_Gnorm,top_Rnorm))
fitPST=aov(score~type,rbind(top_GPST,top_RPST))

summary(fitNorm)
summary(fitPST)

