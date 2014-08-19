library('rbamtools')

source("lib/bam_funcs.R")

load_files<-function(files, dir)
{
  for (f in files)
  {
    f = paste(dir, f, sep="/")
    load(f)
    if (exists("means_var"))
      means_var = rbind(means_var, means)
    else
      means_var = means
    
    rownames(means_var) = c(1:nrow(means_var))
  }
  rm(means)
  return(means_var)
}


read_disc_alignment<-function(brg, inter=NULL, disc=NULL)  # takes bamRange
  {
  if (is.null(inter) | is.null(disc))
    stop("File names required")

  rewind(brg)
  align <- getNextAlign(brg)
  while(!is.null(align))
    {
    if (!unmapped(align) & !mateUnmapped(align) ) 
      {
      if ( paired(align) & !failedQC(align) & !pcrORopt_duplicate(align) )
        {
        if (!properPair(align) || insertSize(align) > 650)
          {
          if ( refID(align) == mateRefID(align) && insertSize(align) != 0) 
            write.table( paste(position(align), matePosition(align), abs(insertSize(align)), sep="\t"), quote=F, append=T, col.names=F, row.names=F, file=inter)
           if ( refID(align) != mateRefID(align) )
             write.table( paste(refID(align), position(align), mateRefID(align), matePosition(align), sep="\t"), quote=F, append=T, col.names=F, row.names=F, file=disc)
          }
        }
      }
    align = getNextAlign(brg)
    }
  }

create_file<-function(filename, cols, remove=F)
  {
  file.create(filename, overwrite=remove)
  write.table(cols, file=filename, quote=F, col.names=F, row.names=F)
  }

args <- commandArgs(trailingOnly = TRUE)
print(args)

#args = c('/Volumes/Spark/Data/TCGA/sequence/HCC1954.G31860/chr17.bam')
args = c('/Volumes/exHD-Killcoyne/TCGA/sequence/cell_lines/HCC1954.G31860/G31860.HCC1954.6.bam')

print(getwd())

`%nin%` <- Negate(`%in%`) 

bands=read.table("band_genes.txt", header=T)
if (is.null(bands))
  stop("band_genes.txt file not found")

for (bam in args)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  
  print(isOpen(reader))
  
  out_dir = paste(dirname(bam), "dist", sep="/")
  if (file.exists(out_dir))
    file.remove(out_dir, recursive=T)
  dir.create(out_dir)
  
  disc_file = paste(out_dir, paste("discordant", "txt", sep="."), sep="/")
  create_file(disc_file, paste("ref.chr", "ref.pos", "mate.ref", "mate.pos", sep="\t"), remove=T)
  
  referenceData = referenceData[ grep("^(chr)?(\\d+|Y|X)$", as.vector(referenceData$SN), perl=T ), ]
  for (i in 1:nrow(referenceData))
    {
    chr_id = referenceData[i,]

    print("Centromeres")
    cm = range(get_band_range(bands, chr_id['SN'], c('p11','q11'))[,c('start','end')])
    centromere = c(chr_id['ID'],cm)
    range = bamRange(reader, unlist(centromere))
    
    inter_file = paste(out_dir, paste("centromere_dist", chr_id['SN'], "txt", sep="."), sep="/")
    create_file(inter_file, paste("pos", "mate.pos", "length", sep="\t") )
    
    read_disc_alignment(range, inter=inter_file, disc=disc_file)
    
    arms = bands[ which(bands$chr == chr_id$SN & bands$band %nin% c('p11','q11')), ]
    inter_file = paste("arm_dist",chr_id['SN'], "txt", sep=".")          
    create_file(inter_file, paste("pos", "mate.pos", "length", sep="\t") )
    
    for (band in arms$band)
      {
      print( paste("Band", band, sep=" "))
      cm = range(get_band_range(bands, chr_id['SN'], c(band))[,c('start','end')])
      
      read_disc_alignment(range, inter=inter_file, disc=disc_file)
      }
  
    }
  bamClose(reader)
  }


