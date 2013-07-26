
roll<-function(probs)
  {
  rand = runif(1,0,1)
  match = probs[probs >= rand]
  row = match[1]
  return(which(probs == row))
  }

set.probs<-function(probs, df)
  {
  for(i in length(probs):1)
    df[i,'p']<-sum(probs[i:1])
  return(df)
  }

dir = "~/Analysis/Database/cancer"
setwd(dir)

chromosomes = c(1:22,'X')
## Select breakpoints directly
d = read.table("all-bp-prob.txt", header=T, sep="\t")
d = d[order(d$bp.prob),]

probs = d$bp.prob
for(i in length(probs):1)
  d[i,'p']<-sum(probs[i:1])

bp_chr_counts = vector("numeric", 23)
names(bp_chr_counts) = chromosomes
bp_samples = matrix(data = 0, ncol=1, nrow=nrow(d), dimnames=list(paste(d$chr,d$band,sep="")))
for (i in 1:1000)
  {
  n = roll(d$p)
  chr = as.character(d[n,'chr']) 
  band = as.character(d[n,'band'])
  key = paste(chr,band,sep="")
  bp_samples[key,] = bp_samples[key,]+1
  bp_chr_counts[chr] = bp_chr_counts[chr] + 1
  }
bp_samples = sort(bp_samples[ which(bp_samples > 0), ])
plot(bp_samples)
text(bp_samples, labels=names(bp_samples), pos=1)
sort(bp_chr_counts)

## Select chromosome then select breakpoints from within that chromosome
c = read.table("chr_instability_prob.txt", header=F, sep="\t")
colnames(c) = c('chr','probs')
c = c[order(c$probs),]

c = set.probs(c[,2], c)

selected = d[,c('chr','band')]
selected$count = 0
for (i in 1:1000)
  {
  chrs = vector("numeric", sample(1:10,1))
  for (jj in 1:length(chrs))
    chrs[jj] = c[roll(c$p),'chr']
  chrs

  for (chr in chrs)
    {
    sub = d[d$chr == chr, c('band','per.chr.prob')]
    sub = sub[order(sub$per.chr.prob),]
    sub = set.probs(sub[,2], sub)
    bp = vector("character", sample(1:nrow(sub)-1, 1))
    for (ii in 1:length(bp))
      {
      band = as.character( sub[roll(sub$p), 'band'] )
      selected[ which(selected$'chr' == chr & selected$'band' == band), 'count'] = selected[ which(selected$'chr' == chr & selected$'band' == band), 'count'] + 1
      }
    }
  }
selected = selected[order(selected$count), ]
plot(selected$count, type='h')
text(selected$count,labels=paste(selected$chr, selected$band, sep=""))

chr_counts = vector("numeric", 23)
names(chr_counts) = chromosomes
for (i in chromosomes)
  chr_counts[i] = sum( selected[ selected$chr == i, 'count']  )

plot(sort(chr_counts), type='o')
text(sort(chr_counts), labels=names(sort(chr_counts)), pos=2)

sort(chr_counts)
sort(bp_chr_counts)