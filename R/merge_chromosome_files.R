outdir = "~/Data/Ensembl/Variation/chromosomes"
hapmapdir = "~/Data/Ensembl/Variation/hapmap/chromosomes"
ensdir = "~/Data/Ensembl/Variation/Homo_sapiens/chromosomes"

chrs=c(1:22, 'X', 'Y')
cols = c('chr','start', 'end', 'var.type', 'ref.seq', 'var.seq', 'local.id')
for (c in chrs)
	{
	print(c)
	file = paste("chr", c, ".txt", sep="")
	hm = read.table(paste(hapmapdir, file, sep="/"), header=T, sep="\t")
	ids = hm[, cols]
	ids = unique(ids)
	print(paste("hapmap ids", nrow(ids)))

	ens = read.table(paste(ensdir, file, sep="/"), header=T, sep="\t")
	ids = rbind(ids, ens[,cols])
	ids = unique(ids)
	print(paste("all ids", nrow(ids)))

	ids = ids[order(ids$start),]
	write.table(ids, file=paste(outdir, file, sep="/"), col.names=T, row.names=F, quote=F, sep="\t")	r
  m(hm,ens,ids)
	}





