library('rbamtools')

source("lib/bam_funcs.R")

get_sampling_range<-function(rdr, coords, window=5000)
  {
  if (class(rdr)[1] != "bamReader")
    stop("First argument should be a bamReader object")
  
#  rg = bamRange(rdr, coords ) 
#  print(rg)
#  print(size(rg))
#  stop("")
  
  start = as.integer(sample( c(coords[2]:coords[3]), 1))
  end = as.integer(start+window)
  range = bamRange(rdr, c(as.numeric(coords[1]), start, end) )
  
#  iters = 1;
#  while (size(range) <= 100) #probably that's even too small
#    {
#    start = sample( c(coords[2]:coords[3]), 1)
#    end = start+window
#    range = bamRange(rdr, c(coords[1], start, end) )
#    iters = iters+1
#print(paste("range size ", size(range), sep="  "))
#  }

  return(range)
  }

disc_locations<-function(rng)
  {
  if (class(rng)[1] != "bamRange")
    stop("First argument should be a bamRange object")
  
	dlocs = matrix(ncol=4,nrow=0, dimnames=list(c(), c('start','mate.refid','mate.pos','length')))
  if (size(rng) <= 0) return(dlocs)
  
  
  rewind(rng)
  if (size(rv) > 0) 
    {
	  align = getNextAlign(rng)
    while(!is.null(align))
		  {  			    
      message( align )

		  if (!unmapped(align) & !mateUnmapped(align) & insertSize(align) != 0)
			  {
		  	#message( paste(name(align), position(align), sep=" ") )

	      if ( paired(align) & !failedQC(align) & !pcrORopt_duplicate(align) )
				  {
				  if (!properPair(align) )
				    {
				    message( paste(name(align), position(align), sep=" ") )
            dlocs = rbind(dlocs, c(position(align), mateRefID(align), matePosition(align),insertSize(align)))
				    }
				  }
          if (mateRefID(align) != refID(align))
            {
            message( paste(name(align), position(align), mateRefID(align), sep=" ") )
            }
			  }
		  align = getNextAlign(rng)
      }
  	} 
	return(dlocs)
	}

overlaps<-function(n, range)
  {
  if (n >= range[1] & n <= range[2])
    return(TRUE)
  
  return(FALSE)
  }

`%nin%` <- Negate(`%in%`) 

bam = "/work/projects/synthetic-cancer/data/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam" 
bam = "/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam" 
bai = paste(bam, "bai", sep=".")

bands=read.table("band_genes.txt", header=T)
arms = bands[ which(bands$chr == '1' & bands$band %nin% c('p11','q11')), ]

reader = bamReader(bam)
load.index(reader, bai)

rfd = getRefData(reader)
chr = rfd[1,]

## test range
#chr 1 128900001-142600000

#rv = bamRange(reader, c(chr$ID, 128900001, 142600000))
#print(rv)
#dl = disc_locations(rv)
#print(dl)

arms = arms[arms$band == 'q12',]

coords_l = sapply(arms$band, function(b){
  coords = c(chr$ID, range(get_band_range(bands, chr$SN, b)[c('start','end')]))
  range_iters = round(((coords[3]-coords[2])/5000)/100)
  return(c(range_iters, coords))
}, simplify=F)

for (i in 1:length(coords_l))
  {
  xic = coords_l[[i]]
  iters = xic[1]
  coords = xic[2:4]

  print( paste("chr", rfd[rfd$ID == coords[1], 'SN'], paste(coords[2], coords[3], sep="-"),sep=" ") )

  range_vector = vector(mode="complex")
  # avoid reusing range
  for (j in 1:iters)
    {
    print(j)
    range = get_sampling_range(reader, coords, 10000)
    if (length(range_vector) > 0)
      {
      print(range)
      print(range_vector)
      
      while ( (length(range_vector[sapply( range_vector, function(x) getAlignRange(range)[1]>=getAlignRange(x)[1] & getAlignRange(range)[1]<=getAlignRange(x)[2] )]) +
             length(range_vector[sapply( range_vector, function(x) getAlignRange(range)[2]>=getAlignRange(x)[1] & getAlignRange(range)[2]<=getAlignRange(x)[2] )])) > 0 )
        { range = get_sampling_range(reader, coords, 10000) }
      }
    range_vector = append(range_vector, range)
    }
  
  #for (rv in range_vector)
  for (n in 1:length(range_vector))
    {
    rv = range_vector[[n]]
    print( getAlignRange(rv) )
    rloc = disc_locations(rv)
    if (length(rloc) > 0)
      {
      if (exists("locs")) locs = rbind(locs, rloc) else locs = rloc 
      save(locs, file="t-disc_locs.RData")
      print(nrow(locs))
      }
    }
  }

rdata_file = "disc_locs.RData"
save(locs, file=rdata_file)

unlink("t-disc_locs.RData")

stop("")

bamClose(reader)
stop("")

load(rdata_file)
nrow(load("t"))

locs = as.data.frame(locs)
locs = locs[order(locs$start),]

#bins = as.data.frame(matrix(nrow=0,ncol=ncol(locs)))
#colnames(bins) = colnames(locs)
bins = vector(mode="integer")
counts = vector(mode="integer")
win = 500
start = locs[1, 'start']
while (start < locs[nrow(locs), 'start'])
  {
  end = start+win
  mean = mean(abs(locs[ which(locs$start >= start & locs$start < end), 'length']))
  if (!is.na(mean)) 
    {
    bins[as.character(start)] = mean
    counts[as.character(start)] = nrow(locs[ which(locs$start >= start & locs$start < end), ])
    }

  start = start+win
  print(start)
  }

plot(sort(bins))
summary(bins)

ks.test(bins, ppois, mean(bins))
ks.test(bins, pnorm, mean(bins), sd(bins)) # normal apparently

probs = pnorm(bins, mean(bins), sd(bins))

cnts = counts[counts > 2]
plot(cnts, type='l')
#ks.test(cnts, ppois, mean(cnts))
ks.test(cnts, pnorm, mean(cnts), sd(cnts))


high = locs[locs$start >= (2585080-500) & locs$start <= (2585080+500),]

lengths = log2(abs(high$length))
#lengths = lengths[ lengths<= mean(lengths)+sd(lengths)*2  & lengths>= mean(lengths)-sd(lengths)*2  ] 


ks.test(lengths, pnorm, mean(lengths), sd(lengths))
#ks.test(lengths, ppois, mean(lengths))

plot( sort(lengths) )

