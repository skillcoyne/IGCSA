
bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$len = bands$end-bands$start
bands$name = paste(bands$chr, bands$band, sep="")
#bands = bands[,c('name','len','gene.count')]

#dir=<path where breakdancer files are found>
bdx = list.files(path=dir, pattern="*.ctx", recursive=F, full.names=F)

#bdx = bdx[which(grepl("(BRCA-NB/|BRCA-PST/)", bdx))]

#bdx = bdx[which(grepl("[A-Z]+-[A-Z]+", bdx))]
bdx = paste(dir, bdx, sep="/")
bdx = bdx[unlist(lapply(bdx, function(x) file.info(x)$size > 400))]

#cols=(Chr1  Pos1	Orientation1	Chr2	Pos2	Orientation2	Type	Size	Score	num_Reads	num_Reads_lib	TCGA-AZ-4315-10A-01W-1461-10_wgs_Illumina.bam
regions = as.data.frame(matrix(nrow=0,ncol=length(bdx)))
colnames(regions) = sub("/B(D|C).ctx", "", sub(paste(dir, "/",sep=""), "", bdx))

samples = matrix(nrow=length(bdx), ncol=6, dimnames=list(colnames(regions),c('total','cent.uniq','arms.uniq', 'missing','high.score','high.score.name')))

for (n in 1:length(bdx))
  {
  file=bdx[n]
  print(file)
  conn = file(file, "r")
  lines = readLines(con=conn,n=10)
  close(con=conn)
  lines = lines[grep("^#", lines)]

  cols = unlist(lapply(unlist(strsplit(tail(lines,1), "\t")), function(x) sub("#","", x)))
  print(cols)
  
  bd = read.table(file, header=F, col.names=cols, sep="\t", fill=T)
  bd$num_Reads_lib = NULL
  bd$Chr1 = as.character(bd$Chr1)
  bd$Chr2 = as.character(bd$Chr2)

  bd$Chr1 = unlist(sub('chr','',bd$Chr1))
  bd$Chr2 = unlist(sub('chr','',bd$Chr2))

  bd = bd[bd$Chr1 %in% c(1:22,'X','Y') & bd$Chr2 %in% c(1:22,'X','Y'),]
  bd = bd[bd$Chr1 != bd$Chr2,]  # only inter-chromosomal
  bd = bd[order(-bd$Score),]

  total=nrow(bd)
  bd$Score = bd$Score + 1

  for (i in 1:nrow(bd))
    {
    row = bd[i,]
  
    b1 = which(bands$chr == row[['Chr1']] & bands$start <= row[['Pos1']] & bands$end >= row[['Pos1']])
    b2 = which(bands$chr == row[['Chr2']] & bands$start <= row[['Pos2']] & bands$end >= row[['Pos2']])
  
    if (length(b1) > 0)
      bd[i,'Band1'] = bands[ b1,'name'] 
    if (length(b2) > 0)
      bd[i,'Band2'] = bands[ b2,'name'] 
    }

    bd = bd[order(-bd$num_Reads, -bd$Score),]
    head(bd)

    missing = which(is.na(bd$Band1) | is.na(bd$Band2))

    if (length(missing) > 0) bd = bd[-missing,]

    cent = which(grepl("(\\d+|X|Y)(p|q)11", bd$Band1) | grepl("(\\d+|X|Y)(p|q)11", bd$Band2))
    has_centromere = bd[cent,]

    band.scores<-function(df)
      {
      dups = which(duplicated(df$Band1,df$Band2))
      
      scores=vector(mode="numeric")
      for (d in dups)
        {
        sc = df[d,]
        scores[paste(sc$Band1,sc$Band2,sep="-")] = sum(df[which(df$Band1==sc$Band1 & df$Band2==sc$Band2), 'Score'])
        }
      return(scores)
      }
  
    cent_scores = band.scores(bd[cent,])/100
    arm_scores = band.scores(bd[-cent,])/100
  
    scores = band.scores(bd)/100 
  
    for (j in 1:length(scores))
      {
      #if (scores[[j]] >= 0.8)
        regions[names(scores[j]), n] = scores[[j]]
      }
  
    #write.table( c(cent_scores[which(cent_scores >= 1)], arm_scores[which(cent_scores >= 1)]), quote=F, sep="\t")
  
    ## This may be useful shortly
    #write.table(sort(arm_scores, decreasing=T), sep="\t", quote=F, file=paste(dirname(file), "arm_scores.txt", sep="/"))
    #write.table(sort(cent_scores, decreasing=T), sep="\t", quote=F, file=paste(dirname(file), "cent_scores.txt", sep="/"))

    samples[n,] = cbind(
      nrow(bd), 
      length(cent_scores),
      length(arm_scores),
      length(missing),
      max(cent_scores, arm_scores),
      names(which.max(c(cent_scores, arm_scores)))
      )
  
    #print(cor.test(bd$Score,bd$num_Reads))
    }

rownames(samples) = unlist(lapply(rownames(samples), function(x){basename(dirname(x))}))
write.table(samples, quote=F, sep="\t", col.names=NA, file="~/Dropbox/Private/breakdancer.txt")
write.table(regions, quote=F, sep="\t", col.names=NA, file="~/Dropbox/Private/breakdancer2.txt")


bdsc = apply(regions, 2, function(cr){
  rows = which(!is.na(cr) & cr >= 1) 
  bdsc = cr[rows]
  names(bdsc) = rownames(regions)[rows]
  return(bdsc)
})
names(bdsc) = sub(".ctx","", names(bdsc))


palette(c('red','green','blue'))
par(mfrow=c(2,4))
bdtop = list()
for (i in 1:length(bdsc))
  {
  name = names(bdsc)[i]
  scores = sort(bdsc[[name]])
  km = kmeans(scores, quantile(scores,seq(0,1,0.5)))
  plot(scores, col=km$cluster, pch=19, main=sub(".ctx","", name))
  
  bdtop[[name]] = names(which( km$cluster == which.max(km$center) ))
  }


shared = lapply(bdtop, function(x){
  lapply(bdtop, function(y) intersect(x,y))
})


df = data.frame(matrix(data="", ncol=length(bdtop), nrow=max(unlist(lapply(bdtop, length)))))
colnames(df) = names(bdtop)
for (cnc in names(bdtop))
  df[,cnc] = cbind( c(bdtop[[cnc]], rep("", nrow(df)-length(bdtop[[cnc]]))) )

df = df[,sort(colnames(df))]
write.table(df, quote=F, sep="\t", row.names=F, file="~/tmp/breakdancer.txt")


# centromeres
cent = apply(regions, 2, function(cr){
  rows = which(!is.na(cr) & cr >= 1) 
  length(which(grepl("(p|q)(11|12)", rownames(regions)[rows])))
})

# arms
arm = apply(regions, 2, function(cr){
  rows = which(!is.na(cr) & cr >= 1) 
  length(which(!grepl("(p|q)(11|12)", rownames(regions)[rows])))
})









