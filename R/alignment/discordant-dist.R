
bands=read.table("/Users/sarah.killcoyne/Analysis/Database/cancer/band_genes.txt", header=T)
bands=bands[which(bands$chr == '1'),]

chr_length = max(bands$end)
centromere_range = range(bands[which(bands$band %in% c('p11', 'q11')), c('start','end')])
cent_len = centromere_range[2]-centromere_range[1]

dir="/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/inter-reads"

data = read.table(paste(dir, 'chr1.reads', sep='/'), header=F)
colnames(data) = c("read", "ref", "pos", "dir", "mate", "mpos", "mdir", "dist")
data[1:10,]

# often the reverse complement of the same read is listed -- duplicated does leave the singlet around
data = data[ !duplicated(data$read), ]
data = data[ order(data$pos),]

# distances are sometimes listed as negative due to strand
data$dist = abs(data$dist)

crows = which( (data$pos >= centromere_range[1] & data$pos <= centromere_range[2]) &
                 (data$mpos >= centromere_range[1] & data$mpos <= centromere_range[2])   )
centromere = data[crows,]
nrow(centromere)
nrow(centromere)/nrow(data)

arms = data[-crows,]
nrow(arms)/nrow(data)

#distance is a continuous variable, which means I need a density probability function...?  Also means it isn't modeled as poisson unless I turn that into counts

hist(arms$dist, probability=T, breaks="FD", col = "darkslategray4", border = "seashell3")
lines(density(arms$dist - 0.5), col='red', lwd=2)

density(arms$dist)

hist(centromere$dist, probability=T, breaks="FD", col = "darkslategray4", border = "seashell3")
lines(density(centromere$dist - 0.5), col='red', lwd=2)

density(centromere$dist)

# so then the question is what is the probability x will fall within a given range of values...how do I calculate that?




## If I adjust the distances for the maximum possible distance then I get a number between 0,1...what can I do with it?  
# Doesn't change the probability density obviously.  So not sure it makes any sense
arms$dist/chr_length
centromere$dist/cent_length



#useless 
#ks.test(arms$dist, pnorm, mean(arms$dist), sd(arms$dist))
#ks.test(centromere$dist, pnorm, mean(centromere$dist), sd(arms$dist))



