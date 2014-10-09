setwd("~/workspace/IGCSA/R/alignment")
source("lib/read_eval.R")


args <- commandArgs(trailingOnly = TRUE)

#args[1] = "~/Analysis/band_genes.txt"
#args[2] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/LUAD-NB"
#args[2] = "/Volumes/Spark/Insilico/runs/alignments/LUAD-NB"


if (length(args) < 2)
  stop("Missing required arguments: <band_genes file> <directory to read in>")

savePlot = T

bands = read.table(args[1], header=T)
bands$length = bands$end - bands$start

setwd(args[2])
print(getwd())
#setwd(paste(getwd(), "alignments/HCC1954", sep="/"))

read_files = list.files(pattern="*paired_reads.txt", recursive=T)

if (length(read_files) <= 0)
  stop(paste("No paired_reads.txt files found in directory:", getwd()))

cols = c('nreads', 'mean.dist', 'sd.dist', 'mean.phred', 'sd.phred', 'mean.mapq','sd.mapq', 'pp.ratio', 'FF','FR','RR','RF')
rAll = as.data.frame(matrix(ncol=length(cols),nrow=length(read_files),data=0,
                            dimnames=list(dirname(read_files), cols)))
rLeft = rAll
rRight = rAll
rSpan = rAll

emmodel = list()

for (file in read_files)
	{
  print(file)
  name = dirname(file)
  result = tryCatch({
    reads = read.table(file, header=T, sep="\t", comment.char="")
  
    reads$cigar = as.character(reads$cigar)
    reads$cigar.total = cigar.len(reads$cigar)
    reads$orientation = as.character(reads$orientation)

    length(which(reads$cigar.total < 0))
    nrow(reads)
    reads = reads[reads$cigar.total > 0,]

    # percentage of paired reads with > 0 read distance that are 'proper pairs'
    print(nrow( reads[reads$ppair == TRUE,] )/nrow(reads))
    summary(reads$len)

    model = getMixtures(log(reads$len), "V")
    emmodel[[name]] = model

    png_file=paste(getwd(), name, "read_pair_distance.png", sep="/")
    png(filename=png_file, width=800, height=600)
    counts = log(reads$len)
    hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(3,18), xlab="log(read-pair distance)", main=name)
    lines(density(counts, kernel="gaussian"), col="blue")

    for (i in 1:ncol(model$z))
      { 
      m = model$parameters$mean[i]
      v = model$parameters$variance$sigmasq[i] 
      abline(0,0,v=m)
      text(m, 0.1, labels=paste("mean:",round(m,2)), pos=2)
      text(m, 0.05, labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2)
      }
  dev.off()

  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))

  m = model$parameters$mean[lt]
  v = model$parameters$variance$sigmasq[lt] 
  leftD = reads[ counts >= (m-v) & counts <= (m+v) ,]
  #hist(leftD$cigar.total)

  m = model$parameters$mean[rt]
  v = model$parameters$variance$sigmasq[rt] 
  rightD = reads[ counts >= (m-v) & counts <= (m+v) ,]
  #hist(rightD$cigar.total)

  #plot(sort(rightD$cigar.total), col='red', type='l', ylab='cigar total')
  #lines(sort(leftD$cigar.total), col='blue', type='l')

  breakpoint = bands[ which(paste(bands$chr,bands$band,sep="") %in% unlist(strsplit(basename(name), "-"))), 'length'][1]

  left = reads[reads$pos <= breakpoint & reads$mate.pos <= breakpoint,]
  nrow(left)
  right = reads[reads$pos >= breakpoint & reads$mate.pos >= breakpoint,]
  nrow(right)

  crossing = reads[reads$pos <= breakpoint & reads$mate.pos >= breakpoint,]
  nrow(crossing)

  rAll[name,] = row.gen(reads)
  rLeft[name,] = row.gen(left)
  rRight[name,] = row.gen(right)
  rSpan[name,] = row.gen(crossing)
  
  }, error = function(err) {
    print(paste("Failed to read file", file))
    warning(err)
    cat(paste("Failed to read file", file), file="errors.txt", sep="\n", append=T)
    cat(paste(err, collapse="\n"), file="errors.txt", append=T)
  })
  
  save(rAll[name,], rLeft[name,], rRight[name,], rSpan[name,], model, file=paste(paste(dirname(file), basename(dirname(file)), sep="/"), "Rdata", sep="."))
  }


# inverse correlation between ratio and mean length
#cor.test(rAll$pp.ratio, rAll$mean.dist)
#cor.test(rLeft$pp.ratio, rLeft$mean.dist)
#cor.test(rRight$pp.ratio, rRight$mean.dist)
# but not in the reads spanning the breakpoint which is actually good, these reads should have a really poor pp.ratio to begin with 
#cor.test(rSpan$pp.ratio, rSpan$mean.dist)

save(rAll, rLeft, rRight, rSpan, emmodel, file='reads.Rdata')

# these would seem to be those that don't have a distinct 2nd distribution and likely don't have very high
# likelihood of being breakpoints this also fits with the pp ratio for all reads
right.dist<-function(model)
  {
  rightside = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  mean(model$z[, rightside]) 
  }
right = unlist(lapply(emmodel, right.dist))

#plot(sort(unlist(lapply(emmodel, function(x) x$parameters$mean))))

# perfect inverse correlation between a high pp ratio and a low mean for the right distribution
#cor.test(right, rAll$'pp.ratio')
#summary(right)
#summary(right[right < 0.3])
#rSpan[names(right[right >= 0.5]),]

km = kmeans(right, 4)

# top cluster
top_cluster = which(km$centers == max(km$centers))
top_pairs = sort(right[which(km$cluster == top_cluster)], decreasing=T)

png(filename="scores.png", width=1200, height=800)
par(mfrow=c(2,1))
hist(right, breaks=30,
     main="Scores for 'right' distribution", xlab="EM scores per pair", col='blue', border='grey')

plot(sort(right), col=km$cluster[names(sort(right))], pch=19, ylab='scores',xlab='reference pairs')
points(km$centers, col = 1:4, pch=8)
text(km$center, labels=round(km$centers, 3), pos=4)
dev.off()

write.table(top_pairs, quote=F, sep="\t", col.name=F, file="top_pair_scores.txt")
top = rSpan[names(top_pairs),]
print(top_pairs)
