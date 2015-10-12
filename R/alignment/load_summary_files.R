source("~/workspace/IGCSA/R/denovo_alignment/lib/utils.R")
source("~/workspace/IGCSA/R/denovo_alignment/lib/read_eval.R")

read_dirs<-function(dirs, label)
  {
  all_scores = NULL
  for (i in 1:length(dirs))
    {
    dir = dirs[i]
    print(dir)
    scores = create.score.matrix(paste(dir, 'HCC1954.G31860', sep="/"), paste(label,i,sep=""), bands)
    all_scores = rbind(all_scores, scores)
    }
  return(all_scores)
  }


bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")

#path=<path to summary files for GA and Random>

gaDirs = list.files(path=path, pattern='GA*', full.names=T)
ga = read_dirs(gaDirs, "GA")

randomDirs = list.files(path=path, pattern='Random*', full.names=T)
random = read_dirs(randomDirs, 'Random')

par(mfrow=c(2,1))
hist(ga$score, breaks=100)
hist(random$score, breaks=100)


ga = ga[order(ga$score),]
ga = ga[!is.na(ga$score),]

kmg = kmeans(ga$score, quantile(ga$score, probs=seq(0,1,0.33)), iter.max=50)
kmg$centers
top_g = ga[ which(kmg$cluster == which.max(kmg$centers)), ]

random = random[order(random$score),]
random = random[!is.na(random$score),]
kmr = kmeans(random$score, quantile(random$score, probs=seq(0,1,0.33)), iter.max=50)
kmr$centers
top_r = random[ which(kmr$cluster == which.max(kmr$centers)),]


plot(ga$score, col=kmg$cluster)
plot(random$score, col=kmr$cluster)

setdiff(top_g$name, top_r$name)

top_g$name
top_r$name



