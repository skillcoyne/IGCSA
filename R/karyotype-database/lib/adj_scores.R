## Generates the chromosome instability scores that were previously determined to be the starting point for karyotype generation ##
## all tests were done in the CancerCytogenetics scripts for bp-analysis and bp-by-band ##

norm.scores<-function(scores, col)
  {
  adj_scores = as.data.frame(matrix(ncol=3, nrow=nrow(scores), dimnames=list(c(1:nrow(scores)), c('chr','band','scores'))))
  adj_scores$chr = scores$chr
  adj_scores$band = scores$band
  
  length_adj = 0.7
  # Normalize the total for length 
  adj_scores[,'scores'] = scores[[col]]/(scores[,'bp.length']^length_adj)
  
  return(adj_scores)
  }

bp.norm.score<-function(bpinfo, col)
  {  
  adj = list()

  chromosomes = unique(break_info$chr)
  for (chr in chromosomes)
    {
    scores = bpinfo[bpinfo$chr == chr,]
    scores = scores[order(scores$band),]
    scores = na.omit(scores)
    # get the arms in the correct order p -> q
    parm = grep("p", scores$band )
    p = scores[parm,]
    p = p[order(p$band, decreasing=T),]
    scores[parm,] = p
    
    # quick transform
    ns = norm.scores(scores, col)
    adj[[chr]] = ns$scores
    names(adj[[chr]]) = ns$band
    }
  return(adj)
  }

adjust.to.one<-function(p, r=5)
  {
  adjusted = round(p/sum(p), r) 
  
  if (sum(adjusted) > 1)
    adjusted[ which(adjusted == min(adjusted)) ] = adjusted[ which(adjusted == min(adjusted)) ] - (sum(adjusted) - 1)
  
  if (sum(adjusted) < 1)
    adjusted[ which(adjusted == min(adjusted)) ] = adjusted[ which(adjusted == min(adjusted)) ] + (1 - sum(adjusted))
  
  return(adjusted)
  }

