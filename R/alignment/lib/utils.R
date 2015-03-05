read.normal.txt<-function(file, reqCols=NULL)
  {
  nm = as.data.frame(t(read.table(file, header=F, row.names=1)))
  if (is.null(nm) )
    stop("Missing object that includes normal values.")
  
  '%nin%' = Negate(`%in%`)
  if (!is.null(reqCols) & length(which(reqCols %nin% colnames(nm))) > 0 )
    stop(paste("Normal value object missing columns:", paste(reqCols, collapse=',')))
    
  return(nm)  
  }

read.file<-function(file)
  {
  reads = NULL
  tryCatch({
    if (file.info(file)$size > 1000000000/2) {
      require('data.table')
      reads = fread(file,  header=T, sep="\t", showProgress=T, stringsAsFactors=F) } else {
        reads = read.table(file, header=T, sep="\t", comment.char="", stringsAsFactors=F)
      }
    print(paste(nrow(reads), "loaded"))
    #reads$orientation = as.character(reads$orientation)
    #reads$cigar = as.character(reads$cigar)
    
    return(reads) 
  }, error = function(err) {
    print(paste("Failed to read file", file))
    warning(err)
    cat(paste("Failed to read file", file), file="errors.txt", sep="\n", append=T)
    cat(paste(err, collapse="\n"), file="errors.txt", append=T)
  }, finally = {
    return(reads)
  })
}

create.summary.obj<-function()
  {
  summary = list()
  for (n in c("name", "score", 'scored', 'total.reads', "cigar", "distance", "phred", "filtered.reads", "orientation", "estimated.dist", 
              "sum.l.prob", "sum.r.prob", "l.orientation", "r.orientation", "l.kurtosis", "r.kurtosis", "n.left.reads", "n.right.reads",
              "l.dens","r.dens",'l.shapiro', 'r.shapiro', 'emr'))
    summary[[n]] = NA
  
  return(summary)
}


row.gen<-function(df)
{
  if (nrow(df) <= 0)
    return(matrix(nrow=1,ncol=12,data=NA))
  
  row = cbind(nrow(df), 
              mean(log(df$len)), 
              sd(log(df$len)),
              mean(df$phred),
              sd(df$phred),
              mean(df$mapq),
              sd(df$mapq),
              nrow( df[df$ppair == TRUE,] )/nrow(df),
              length(which(df$orientation == 'F:F')),
              length(which(df$orientation == 'F:R')),
              length(which(df$orientation == 'R:R')),
              length(which(df$orientation == 'R:F'))
  )
  return(row)
}

