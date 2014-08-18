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


read_disc_alignment<-function(brg)  # takes bamRange
  {
  rewind(brg)
  ic = vector(mode="integer")
  dc = vector(mode="character")
  
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
            ic = append( ic, abs(insertSize(align)) ) # inter-chr
           if ( refID(align) != mateRefID(align) )
            dc = append( dc, mateRefID(align) ) # inter-chr
          }
        }
      }
    align = getNextAlign(brg)
    }
  return( list("disc" = dc, "inter" = ic)  )
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

rdata_dirs = vector(mode="character", length=length(args))
for (bam in args)
  {
  bai = paste(bam, "bai", sep=".")
  print(paste("Reading bam ", bam, sep=""))
  reader = bamReader(bam)
  load.index(reader, bai)
  
  referenceData = getRefData(reader)
  
  print(isOpen(reader))
  
  current_dir = dirname(bam)
  rdata_dirs = append(rdata_dirs, current_dir)
  
  referenceData = referenceData[ grep("^(chr)?(\\d+|Y|X)$", as.vector(referenceData$SN), perl=T ), ]
  for (i in 1:nrow(referenceData))
    {
    chr_id = referenceData[i,]

    print("Centromeres")
    cm = range(get_band_range(bands, chr_id['SN'], c('p11','q11'))[,c('start','end')])
    centromere = c(chr_id['ID'],cm)
    range = bamRange(reader, unlist(centromere))
    filename = paste("centromere_dist", chr_id['SN'], "txt", sep=".")
    distances = read_disc_alignment(range)
    
    write.table(distances$inter, file=paste(current_dir, filename, sep="/")
                
    arms = bands[ which(bands$chr == chr_id$SN & bands$band %nin% c('p11','q11')), ]
    filename = paste("arm_dist",chr_id['SN'], "txt", sep=".")          
    if ( file.exists(paste(current_dir, filename, sep="/")) ) 
      file.remove(paste(current_dir, filename, sep="/"))
    
    for (band in arms$band)
      {
      print( paste("Band", band, sep=" "))
      cm = range(get_band_range(bands, chr_id['SN'], c(band))[,c('start','end')])
      
      distances = read_disc_alignment(range)
      write.table( distances$inter, file=paste(current_dir, filename, sep="/"), append=T)
      }
  
    }
  bamClose(reader)
  }


