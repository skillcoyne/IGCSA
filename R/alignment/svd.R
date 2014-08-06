library('rbamtools')
source("lib/bam_funcs.R")

'%nin%' = Negate(`%in%`)

bands=read.table("/Users/sarah.killcoyne/Analysis/Database/cancer/band_genes.txt", header=T)
bands=bands[which(bands$chr == '1'),]

chr_length = max(bands$end)
centromere_range = range(bands[which(bands$band %in% c('p11', 'q11')), c('start','end')])

dir="/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860"

#id = read.table(paste(dir, "disc-depth", 'chr1.reads', sep='/'), header=F)
#colnames(id) = c("read", "ref", "pos", "dir", "mate", "mpos", "mdir", "dist")

print("Reading SVD file")
bd = read.table(paste(dir, "SVD", "discordant.bam.all.links", sep="/"), header=F)
ncol(bd)
colnames(bd) = c('chr.1', 'chr.1.start','chr.1.end','chr.2','chr.2.start','chr.2.end',
                 'read.count', 'read.names','strand.1','strand.2','rank.pos.1', 'rank.pos.2',
                 'order.1','order.2','seq.start.1','seq.start.2')

# just look at those supported with > 1 read
bd = bd[bd$read.count > 1,]
print(nrow(bd))

m = as.data.frame(matrix(ncol=3,nrow=0))
colnames(m) = c('count','length','bam.count')
# centromere 
band = range(bands[bands$band %in% c('q11','p11'), c('start','end')])
m['centromere','length'] = band[2]-band[1]

start = bd[ bd$chr.1.start >= band[1] & bd$chr.1.end <= band[2], c('chr.1','chr.1.start','chr.1.end','chr.2','chr.2.end','read.count', 'read.names')  ]
print(nrow(start))
ureads = unique(unlist(strsplit( as.character(start$read.names), ',') ))


arms = bands[ which(bands$band %nin% c('p11','q11')), 'band']
files = list.files(dir, "G31860")
bam = files[grep("bam$", files)]
bai = paste(bam, "bai", sep=".")

print("Reading bam file")
reader = bamReader(paste(dir, bam, sep="/"))
load.index(reader, paste(dir, bai, sep="/"))

rdata = getRefData(reader)
chr_id =  grep("^(chr)?1$", rdata$SN)

# bands in order based on karyotype probability...
m['centromere', 'count'] = length(ureads)  #sum(start$read.count)   #nrow(start)

for (b in arms)
#for (b in c('q21', 'p13', 'p22', 'p36','q23','q44','q41'))
  {
  print(b)
  band = range(bands[ which(bands$band == b), c('start','end')])
  length = band[2]-band[1]
  
  start = bd[ bd$chr.1.start >= band[1] & bd$chr.1.end <= band[2], c('chr.1','chr.1.start','chr.1.end','chr.2','chr.2.end','read.count', 'read.names')  ]
  
  ureads = unique(unlist(strsplit( as.character(start$read.names), ',') ))
  
  m[b,'count'] = length(ureads)
  m[b,'length'] = length    #sum(start$read.count)   #nrow(start)
  
  bands[ which(bands$chr == '1' & bands$band == b), c('start','end')   ]
  
  cm = range(get_band_range(bands, rdata[chr_id,'SN'], c(b))[,c('start','end')])
  st = run_test(10, reader, c(rdata[chr_id, 'ID'],cm), 10000)
  m[b, 'bam.count'] = sum(st$counts[,'disc'])/nrow(st$counts)
  }
print(m)
save(m, "svd.RData")
bamClose(bam)
stop("")
# of course, counts correlate to length - is that necessarily wrong though?
cor.test(m$count, m$length)

# adjusted for a normalized length
adj = m$count/(m$length^1.2)
names(adj) = rownames(m)
cor.test(adj, m$length)

par(mfrow=c(1,2))
m = m[order(m$count, decreasing=T),]
plot( m$count, type='n', main='Raw counts')
text( m$count,labels=rownames(m))

plot( sort(adj, decreasing=T), type='n', main='Length adjusted counts')
text(sort(adj, decreasing=T),labels=rownames(m))



