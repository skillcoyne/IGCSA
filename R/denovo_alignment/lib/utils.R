require('data.table')

kmeansAIC = function(fit)
  {
  m = ncol(fit$centers)
  n = length(fit$cluster)
  k = nrow(fit$centers)
  D = fit$tot.withinss
  return(D + 2*m*k)
}

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
    #if (file.info(file)$size > 1000000000/2) {
      reads = fread(file,  header=T, sep="\t", showProgress=T, stringsAsFactors=F) #} else {
     #   reads = read.table(file, header=T, sep="\t", comment.char="#", stringsAsFactors=F)
      #}
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



create.score.matrix<-function(dir, type, bands)
  {
  if (is.null(bands) | length(bands) <= 0)
    stop("bands required")
  
  files = list.files(path=dir, pattern="summary.Rdata", recursive=T)
  
  if (length(files) == 0)
    {
    warning(paste("No files found in", dir))
    return(NULL)
    }
  files = files[grep(".*_\\d+/", files, value=F, invert=T)]
  
  print(length(files))
  
  cols = names(create.summary.obj())
  cols = c(cols[-which(cols %in% c('name', 'orientation', 'cigar', 'distance','phred', 'l.orientation','r.orientation', 'scored'))], 
           'prob.sum','right.ratio','max.pos.reads', 'max.pos.prob', 'span','right.in.span')  
  
  m = matrix(ncol=length(cols), nrow=length(files), dimnames=list(gsub("/.*", "", files), cols), data=0)
  or = matrix(ncol=4, nrow=length(files), dimnames=list(gsub("/.*", "", files), c('F:F','F:R','R:F','R:R')))
  
  for (i in 1:length(files))
    {
    load(file=paste(dir, files[i],sep="/"))
    print(paste(dir, files[i], sep="/"))
    
    name = dirname(files[i])
    
    if (is.null(summary[['max.pos.reads']]))
      summary[['max.pos.reads']] = max(summary[['top.pos.clusters']]$ct)
    
    if (is.null(summary[['emr']]))
      {
      summary[['emr']] = summary[['score']]
      summary[['score']] = summary[['emr']]+(summary[['max.pos.reads']]/summary[['n.right.reads']])*10
      }
    #x[['emr']]+(x[['max.pos.reads']]/x[['n.right.reads']])*100
    #summary[['score']] = summary[['emr']]+(summary[['max.pos.reads']]/summary[['n.right.reads']])*100
    
    for (c in cols)
      {
      className = class(summary[[c]])
      #print(paste(c,className, sep="="))
      
      if (is.null(summary[[c]]))
        m[name,c] = NA
      else if (className %in% c('numeric', 'integer'))
        m[name,c] = round(summary[[c]], 4)
      else if (className == 'htest')
        m[name,c] = round(summary[[c]]$p.value, 4)
      else
        print(paste(c, summary[[c]], sep=":"))
      }
    
    m[name,'prob.sum'] = sum(summary$top.pos.clusters$prob)
    m[name,'right.ratio'] = sum(summary$top.pos.cluster$ct)/summary$n.right.reads
    if (is.null(m[name,'max.pos.reads']))
      m[name, 'max.pos.reads'] = max(summary$top.pos.cluster$ct)
    
    m[name,'max.pos.prob'] = max(summary$top.pos.cluster$prob)
    
    if(!is.null(summary$r.orientation))  
      or[i,] = cbind(summary$r.orientation)
  }
  m = as.data.frame(m)
  
  m$bp = unlist(lapply(rownames(m), function(x) sum(bands[which(bands$name %in% unlist(strsplit(x, "-"))), 'len'])  ))
  m$gene.count = unlist(lapply(rownames(m), function(x) sum(bands[which(bands$name %in% unlist(strsplit(x, "-"))), 'gene.count'])  ))
  
  # Below 0.1 can be discarded as not normal, the rest might be normal (though a lot cleary are not)
  #m = m[-which(m$l.shapiro <= 0.1),]
  #m = m[m$estimated.dist == 2,]  # not an issue anymore as I'm only looking for 2 distributions
  
  m$name = rownames(m)
  rownames(m) = c(1:nrow(m))
  
  m$estimated.dist = NULL
  m$l.shapiro = NULL
  m$r.shapiro = NULL
  
  m$type = type
  
  m = m[order(-m$score),]
  
  return(m)
  }

read.breakdancer<-function(file, bands)
  {
  if (is.null(file) | is.null(bands))
    stop("File and bands data table required")
  
  conn = file(file, "r")
  lines = readLines(con=conn,n=6)
  close(con=conn)
  lines = lines[grep("^#", lines)]
  
  cols = unlist(lapply(unlist(strsplit(tail(lines,1), "\t")), function(x) sub("#","", x)))
  
  bd = read.table(file, header=F, col.names=cols, sep="\t", fill=T)
  bd$num_Reads_lib = NULL
  bd$Chr1 = as.character(bd$Chr1)
  bd$Chr2 = as.character(bd$Chr2)
  
  bd$Chr1 = unlist(sub('chr','',bd$Chr1))
  bd$Chr2 = unlist(sub('chr','',bd$Chr2))
  
  bd = bd[bd$Chr1 %in% c(1:22,'X','Y') & bd$Chr2 %in% c(1:22,'X','Y'),]
  bd = bd[bd$Chr1 != bd$Chr2,]  # only inter-chromosomal
  bd = bd[order(-bd$Score),]
  
  total=nrow(bd)
  
  for (i in 1:nrow(bd))
    {
    row = bd[i,]
    
    b1 = which(bands$chr == row[['Chr1']] & bands$start <= row[['Pos1']] & bands$end >= row[['Pos1']])
    b2 = which(bands$chr == row[['Chr2']] & bands$start <= row[['Pos2']] & bands$end >= row[['Pos2']])
    
    if (length(b1) > 0)
      bd[i,'Band1'] = bands[ b1,'name'] 
    if (length(b2) > 0)
      bd[i,'Band2'] = bands[ b2,'name'] 
    }
  
  return(bd)
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

