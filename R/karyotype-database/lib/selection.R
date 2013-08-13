
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

select.bp<-function(bpd, s=1000, col=3)
  {
  bpd = bpd[order(bpd[,col]),]
  bpd = set.probs(bpd[,col], bpd)
  
  bp_chr_counts = vector("numeric", length(unique(bpd[,'chr'])))
  names(bp_chr_counts) = unique(bpd[,'chr'])
  
  bp_selected = bpd[,c('chr','band')]
  bp_selected$count = 0
  for (i in 1:s)
    {
    n = roll(bpd$p)
    chr = as.character(bpd[n,'chr']) 
    band = as.character(bpd[n,'band'])
    bp_selected[which(bp_selected$chr == chr & bp_selected$band == band), 'count'] = bp_selected[which(bp_selected$chr == chr & bp_selected$band == band), 'count']+1
    bp_chr_counts[chr] = bp_chr_counts[chr] + 1
    }
  
  bp_selected = bp_selected[ order(bp_selected$count),]
  bp_selected = bp_selected[ bp_selected$count > 0, ]
  
  bp_chr_counts = bp_chr_counts[bp_chr_counts > 0]
  
  return(list("bp" = bp_selected, "chr.counts" = bp_chr_counts))
  }

select.chr<-function(cdd, cbpd, s=100, plot=F)
  {
  cdd = cdd[order(cdd[,'probs']),]
  cdd = set.probs(cdd[,'probs'], cdd)
  
  selected = cbpd[,c('chr','band')]
  selected$count = 0
  
  # select some chromosomes
  chrs = vector("numeric", s)
  for (jj in 1:length(chrs))
    chrs[jj] = cdd[roll(cdd$p),'chr']
  chrs = unique(chrs)
    
  # select bps from that chromosome
  for (chr in chrs)
    {
    sub = cbpd[cbpd$chr == chr, ]
    sub = sub[order(sub[,3]),]
      
    bps = select.bp(sub, s=sample(1:2,1))$bp
    for(r in 1:nrow(bps))
      {
      band = which(selected[,'chr'] == chr & selected[,'band'] == bps[r,'band'])
      selected[ band, 'count'] = selected[ band, 'count'] + 1
      }
    }
    
  selected = selected[order(selected$count), ]
  
  chr_counts = vector("numeric", nrow(cdd))
  names(chr_counts) = cdd[,'chr']
  for (chr in cdd[,'chr'])
    chr_counts[[as.character(chr)]] = sum( selected[ selected[,'chr'] == chr, 'count']  )
  
  selected = selected[ selected$count > 0, ]
  chr_counts = chr_counts[chr_counts > 0]
  
  return(list("bp" = selected, "chr.counts" = chr_counts))
  }
