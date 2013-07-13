## use this to uniquely merge variations from multiple sources

args = commandArgs(trailingOnly = TRUE)
indir = args[1]
mergedirs = args[2] 
type = args[3]


#indir = "~/Data/Ensembl/Variation/20130701"
#type = "Normal"
#mergedirs = "hapmap,1000Genomes,homo_sapiens"

dirs = unlist(strsplit(mergedirs, ","))
outdir = paste(indir, type, "chromosomes", sep="/") 

print(indir)
print(dirs)
print(type)
print(outdir)


if (!file.exists(outdir)) dir.create(outdir, recursive=T)

chrs = c(1:22, 'X', 'Y')
cols = c('chr','start', 'end', 'var.type', 'ref.seq', 'var.seq', 'local.id')
for (c in chrs)
	{
	print(c)
	file = paste("chr", c, ".txt", sep="")
  
  for (d in dirs)
    {
    print(d)
    d = read.table(paste(indir, d, "chromosomes", file, sep="/"), header=T, sep="\t")
    ids = unique(d[,cols])
    if (exists("all_ids")) all_ids = rbind(all_ids, ids)
    else all_ids = ids
    all_ids = unique(all_ids)
    print(nrow(all_ids))
    rm(ids)
    }
  
	all_ids = all_ids[order(all_ids$start),]
	write.table(all_ids, file=paste(outdir, file, sep="/"), col.names=T, row.names=F, quote=F, sep="\t")	
	}





