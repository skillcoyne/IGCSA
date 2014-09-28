library('mclust')

row.gen<-function(df)
{
	row = cbind(nrow(df), 
	mean(log(df$len)), 
	sd(log(df$len)),
	mean(df$phred),
	sd(df$phred),
	mean(df$mapq),
	sd(df$mapq),
	nrow( df[df$ppair == TRUE,] )/nrow(df))
	return(row)
}

sort.by<-function(df, col)
	{
	df = df[order(-df[[col]]),]
	return(df)
	}

getMixtures<-function(vv, modelName="E")
  {
  cutoff = (max(vv)-min(vv))/2
  z = matrix(0,length(vv),2) 
  z[,1] = as.numeric(vv >= cutoff)
  z[,2] = as.numeric(vv < cutoff)
  msEst = mstep(modelName, vv, z)
  modelName = msEst$modelName
  parameters = msEst$parameters
  em(modelName, vv, parameters)
  }

cigar.len<-function(cv)
  {
  totals = lapply(cv, function(xs) sum( unlist(
    lapply(strsplit(unlist(strsplit(xs, ",")), ":"), 
           function(x) ifelse ( grepl("S|D", x[2]), as.integer(x[1])*-1, as.integer(x[1]))))
  ))
  return(unlist(totals))
  }


savePlot = T

#setwd("/Volumes/Spark/Insilico/runs")
setwd("~/Analysis/Insilico/runs")
bands = read.table("Analysis/band_genes.txt", header=T)
bands$length = bands$end - bands$start

setwd(paste(getwd(), "alignments/HCC1954", sep="/"))

read_files = list.files(pattern="*paired_reads.txt", recursive=T)

cols = c('nreads', 'mean.dist', 'sd.dist', 'mean.phred', 'sd.phred', 'mean.mapq','sd.mapq', 'pp.ratio')
rAll = as.data.frame(matrix(ncol=length(cols),nrow=length(read_files),data=0,
                            dimnames=list(dirname(read_files), cols)))
rLeft = rAll
rRight = rAll
rSpan = rAll

#cols = c('em.mean.1','em.mean.2') #, 'em.var.1', 'em.var.2')
#emmodel = as.data.frame(matrix(ncol=length(cols),nrow=length(read_files), data=0,
#                               dimnames=list(dirname(read_files), cols)))
emmodel = list()

if (savePlot) pdf(file="Rplots.pdf", onefile=T)
for (file in read_files)
	{
print(file)
name = dirname(file)
reads = read.table(file, header=T, sep="\t")
reads$cigar = as.character(reads$cigar)
reads$cigar.total = cigar.len(reads$cigar)
reads$orientation = as.character(reads$orientation)

#plot(sort(reads$cigar.total), type='h')
length(which(reads$cigar.total < 0))
nrow(reads)
reads = reads[reads$cigar.total > 0,]

# percentage of paired reads with > 0 read distance that are 'proper pairs'
print(nrow( reads[reads$ppair == TRUE,] )/nrow(reads))
summary(reads$len)

model = getMixtures(log(reads$len), "V")
emmodel[[name]] = model

counts = log(reads$len)
hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(3,18),
     main=name, sub=paste("Ratio of pp:all paired=", round(nrow( reads[reads$ppair == TRUE,] )/nrow(reads),2)))
lines(density(counts, kernel="gaussian"), col="blue")

for (i in 1:ncol(model$z))
  { 
  m = model$parameters$mean[i]
  v = model$parameters$variance$sigmasq[i] 
  abline(0,0,v=m); 
  text(m, 0.1, labels=paste("em:",round(m,2)), pos=2)
  text(m, 0.05, labels=round( mean(model$z[,i]),3 ), pos=2)
  }

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

}
dev.off()


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

top_pairs = right[right >= 0.5]
sort(top_pairs, decreasing=T)
#sort(top_pairs[grep('^.*/(8.*|5.*)', names(top_pairs), value=T)])

#hist(right)
#kmeans(right, 4)
write.table(sort(right, decreasing=), quote=F, sep="\t", col.name=F, file="scores.txt")


