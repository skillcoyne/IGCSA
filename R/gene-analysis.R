rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")
source("lib/varplots.R")


dir = "~/Data"

chr_info = read.table("~/workspace/IGCSA/ruby/resources/chromosome_gene_info_2012.txt", header=T, sep="\t", row.names='Chromosome')

gene_dir = paste(dir, "/VariationNormal/Genes", sep="")

gene_perc = vector("numeric", length(var_files))
gp_names = vector("character", length(var_files))

chrs = c(1:22)#, 'X', 'Y')
gr = data.frame(chr=chrs, row.names='chr')
#colnames(gr) = c('TF.Dist2', 'FG.Dist2', 'TF.Extreme', 'FG.Extreme', 'Known.Genes')
for (c in chrs)
  {
  overlap_genes = list()
  low_g = list()
  med_g = list()
  high_g = list()
  for (dist in c('low', 'median', 'high'))
    {
    dpath = paste(gene_dir, dist, 'genes', sep="/")
    file = list.files(path=dpath, pattern=paste('chr',c, '-',sep=""))
    gene_d = read.table(paste(dpath, file, sep="/"), header=T, sep="\t")

    # gene files
    genes = unique(unlist(strsplit(as.character(gene_d$Genes), ","))) # Since one gene may overlap multiple consecutive regions
    if (dist == 'low') { low_g = genes } else if (dist == 'median') { med_g = genes } else {high_g = genes}

    overlap_genes = unlist(append(overlap_genes, genes))
    # TF -> total fragments, FG -> Fragment genes
    colnames = c( paste('TF', dist, sep='.'), paste('FG', dist, sep='.')    )
    gr[c, colnames[1]] = nrow(gene_d) - nrow(gene_d[gene_d$Position < 10000,])
    gr[c, colnames[2]] = length(genes)
    }
  gr[c, 'Low.Med.Diff'] = length(setdiff(low_g, med_g))
  gr[c, 'Low.High.Diff'] = length(setdiff(low_g, high_g))
  gr[c, 'High.Med.Diff'] = length(setdiff(high_g, med_g))
  
  
  overlap_genes = unique(overlap_genes)
  gr[c, 'Unique.Genes'] = length(overlap_genes)
  gr[c, 'Known.Genes'] = chr_info[c, 'Confirmed.proteins']
  }



#save(gr, file="Genes.Rdata")

# Proportion of genes in entire chromosome identified within the top of the hill region
# Number of fragments matters

# I *think* I should adjust the number of genes by the number of fragments
# And I should definitely adjust by the number of known genes

frag_adj = gr[,'TF.median']/chr_info[1:22, 'Base.pairs']
m = (gr[, 'FG.median']/gr$Known.Genes)/frag_adj

frag_adj = gr[,'TF.low']/chr_info[1:22, 'Base.pairs']
l = (gr[, 'FG.low']/gr$Known.Genes)/frag_adj

frag_adj = gr[,'TF.high']/chr_info[1:22, 'Base.pairs']
h = (gr[, 'FG.high']/gr$Known.Genes)/frag_adj

# Not sure what I can really say about this or how to mine it further
