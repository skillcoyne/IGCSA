setwd("~/workspace/IGCSA/R/alignment")
source("lib/read_eval.R")

args <- commandArgs(trailingOnly = TRUE)

args[1] = "~/Analysis/band_genes.txt"
args[2] = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/Random/HCC1954.G31860"

args[3] = c("/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam")

if (length(args) < 2)
  stop("Missing required arguments: <band_genes file> <directory to read in> <original aligned bam: OPTIONAL>")


if (!is.null(args[3]))
  {
  orig = sampleReadLengths(args[3])
  distances = orig$dist
  phred = orig$phred
  mapq = orig$mapq
  cigar = orig$cigar
  
  print(summary(distances))
  print(summary(phred))
  }

savePlot = F

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
#rSpan = rAll

emmodel = list()
for 

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
    #nrow( reads[reads$ppair == TRUE,] )/nrow(reads))
    summary(reads$len)
    #reads = reads[reads$cigar.total >= mean(reads$cigar.total)+sd(reads$cigar.total),] ## not sure about this
    
    model = getMixtures(log(reads$len), "V")
    emmodel[[name]] = model

    png_file=paste(getwd(), name, "read_pair_distance.png", sep="/")
    png(filename=png_file, width=800, height=600)
    counts = log(reads$len)
    hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(min(counts),max(counts)), xlab="log(read-pair distance)", main=name)
    lines(density(counts, kernel="gaussian"), col="blue", lwd=2)

    if (!is.null(distances))
      {
      m = mean(log(distances))
      abline(0,0,v=m, col='red',lwd=2)
      text(m, 0.5, labels=paste("Sampled normal mean:",round(m,2)), pos=4)
      }
    
    for (i in 1:ncol(model$z))
      { 
      m = model$parameters$mean[i]
      v = model$parameters$variance$sigmasq[i] 
      abline(0,0,v=m,lwd=2)
      text(m, 0.1, labels=paste("mean:",round(m,2)), pos=2)
      text(m, 0.05, labels=paste("score:", round( mean(model$z[,i]),3 )), pos=2)
      }
  dev.off()

  rt = as.integer(which(model$parameters$mean == max(model$parameters$mean)))
  lt =  as.integer(which(model$parameters$mean != max(model$parameters$mean)))
  
  png_file=paste(getwd(), name, "sub-dist-read-length.png", sep="/")
  png(filename=png_file, width=800, height=600)
  par(mfrow=(c(2,1)))

  m = model$parameters$mean[lt]
  v = model$parameters$variance$sigmasq[lt] 
  leftD = reads[ counts >= (m-v) & counts <= (m+v) ,]
  hist(leftD$len, breaks=20, main=paste("Left sub-distribution mean=", round(mean(leftD$len), 2), sep=""), xlab="Read insert-distance", col="lightgreen", border=F,)

  m = model$parameters$mean[rt]
  v = model$parameters$variance$sigmasq[rt] 
  rightD = reads[ counts >= (m-v) & counts <= (m+v) ,]
  hist(rightD$len, breaks=20, main=paste("Right sub-distribution mean=", round(mean(rightD$len), 2), sep=""), xlab="Read insert-distance", col="lightgreen", border=F,)
  dev.off()
  
  breakpoint = bands[ which(paste(bands$chr,bands$band,sep="") %in% unlist(strsplit(basename(name), "-"))), 'length'][1]

  cleft = leftD[leftD$pos <= breakpoint & leftD$mate.pos >= breakpoint,]
  cright =rightD[rightD$pos <= breakpoint & rightD$mate.pos >= breakpoint,]
  
  #left = reads[reads$pos <= breakpoint & reads$mate.pos <= breakpoint,]
  #nrow(left)
  #right = reads[reads$pos >= breakpoint & reads$mate.pos >= breakpoint,]
  #nrow(right)
  #crossing = reads[reads$pos <= breakpoint & reads$mate.pos >= breakpoint,]
  #nrow(crossing)

  rAll[name,] = row.gen(reads)
  rLeft[name,] = row.gen(cleft)
  rRight[name,] = row.gen(cright)
  #rSpan[name,] = row.gen(crossing)
  
  save(cleft,cright, emmodel, file=paste(getwd(), name, "reads.Rdata", sep="/"))
  }, error = function(err) {
    print(paste("Failed to read file", file))
    warning(err)
    cat(paste("Failed to read file", file), file="errors.txt", sep="\n", append=T)
    cat(paste(err, collapse="\n"), file="errors.txt", append=T)
  })
  
  }
save(rAll, rLeft, rRight, emmodel, file='reads.Rdata')

subdist_means = as.data.frame(t(sapply(emmodel, sub.dist.means)))

png_file=paste(getwd(), "subdist-read-length.png", sep="/")
png(filename=png_file, width=800, height=600)
par(mfrow=c(2,1))
hist(subdist_means$left, breaks=30,  col="lightblue", main="Left subdistribution means", xlab="log(insert size)")
m = log(median(distances))
abline(0,0,v=m, lwd=2, col='red')
text(m, 20, labels=paste("normal mean:",round(m,2)), pos=3)

m = log(median(distances) + sd(distances)*2)
abline(0,0,v=m, lwd=2, col='red')
text(m, 20, labels=paste("normal mean + 2sd:",round(m,2)), pos=3)

hist(subdist_means$right, breaks=30,  col="lightblue", main="Right subdistribution means", xlab="log(insert size)")
dev.off()

# these would seem to be those that don't have a distinct 2nd distribution and likely don't have very high
# likelihood of being breakpoints this also fits with the pp ratio for all reads
right = unlist(lapply(emmodel, right.dist))
km = kmeans(right, 4)

# top cluster
top_cluster = which(km$centers == max(km$centers))
top_pairs = sort(right[which(km$cluster == top_cluster)], decreasing=T)

## Any of the left side of the top greatder than the normal mean?  Apparently not...
subdist_means[names(top_pairs), 'left'][which(subdist_means[names(top_pairs), 'left'] >= mean(distances))]


png(filename="scores.png", width=1200, height=800)
par(mfrow=c(2,1))
hist(right, breaks=30,
     main="Scores for 'right' distribution", xlab="EM scores per pair", col='blue', border='grey')

plot(sort(right), col=km$cluster[names(sort(right))], pch=19, ylab='scores',xlab='reference pairs')
points(km$centers, col = 1:4, pch=8)
text(km$center, labels=round(km$centers, 3), pos=4)
dev.off()

write.table(top_pairs, quote=F, sep="\t", col.name=F, file="top_pair_scores.txt")
#top = rSpan[names(top_pairs),]
#print(top_pairs)

## Only shows up in the lowest clusters so can be safely ignored
km$cluster[rownames(subdist_means[which(subdist_means$left >= log(mean(distances))),])]

