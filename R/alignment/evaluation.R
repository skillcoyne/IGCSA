
bands = read.table("~/Analysis/band_genes.txt", header=T)
bands$length = bands$end - bands$start

setwd("/Volumes/Spark/Insilico/runs/alignments")

read_files = list.files(pattern="*paired_reads.txt", recursive=T)

read_files = read_files[-grep('HCC1954-1q21', read_files)]


pdf(file="Rplots.pdf", onefile=T)

m = as.data.frame(matrix(ncol=3,nrow=length(read_files),data=0,dimnames=list(dirname(read_files), c('mean.dist','pp.ratio', 'lr.ratio'))))
for (file in read_files)
{
  print(file)
  name = dirname(file)
reads = read.table(file, header=T, sep="\t")
reads$cigar = as.character(reads$cigar)
reads$orientation = as.character(reads$orientation)

# percentage of paired reads with > 0 read distance that are 'proper pairs'
print(nrow( reads[reads$ppair == TRUE,] )/nrow(reads))


summary(reads$len)
#plot(sort(log(reads$len)), type='h')

counts = log(reads$len)
hist(counts, breaks=100, col="lightblue", border=F, prob=T, xlim=c(min(counts),max(counts)), main=name, sub=paste("Ratio of pp:all paired=", round(nrow( reads[reads$ppair == TRUE,] )/nrow(reads),2)))
dens=density(counts, kernel="gaussian")
lines(dens, col="blue")

breakpoint = bands[ which(paste(bands$chr,bands$band,sep="") %in% unlist(strsplit(basename(name), "-"))), 'length'][1]

left = reads[reads$pos <= breakpoint & reads$mate.pos <= breakpoint,]
right = reads[reads$mate.pos >= breakpoint & reads$mate.pos >= breakpoint,]


crossing = reads[reads$pos <= breakpoint & reads$mate.pos >= breakpoint,]
plot(sort(crossing$phred))

#plot(table(crossing$pos), type='h')
#plot(table(crossing$mate.pos), type='h')

m[name,] = cbind(mean(log(reads$len)), nrow( reads[reads$ppair == TRUE,] )/nrow(reads), nrow(left)/nrow(right))

}
dev.off()


#plot(sort(reads$phred))
#summary(reads$phred)
#length( which(reads$phred > mean(reads$phred)-sd(reads$phred)) )


#reads[which(reads$phred > mean(reads$phred)-sd(reads$phred)),]