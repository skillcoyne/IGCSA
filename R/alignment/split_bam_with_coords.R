library('rbamtools')

args <- commandArgs(trailingOnly = TRUE)
print(args)

if (length(args) < 3)
  stop("Missing arguments: <bam file> <size in GB> <coordinates file>")


bam = args[1]
size = as.integer(args[2]) * 1073741824 

#sort_prefix = paste(args[4], args[2], sep="-")

bai = paste(bam, "bai", sep=".")
print(paste("Reading bam ", bam, sep=""))
reader = bamReader(bam)
index = load.index(reader, bai)

bamHeader = getHeader(reader)

referenceData = getRefData(reader)
referenceData = referenceData[ referenceData$SN %in% c(1:22, 'X','Y'), ]


dir = dirname(bam)
newBamFile = paste(paste(dir, sub(".bam", "", basename(bam)) , sep="/"), "-", args[2], "gb.bam", sep="")

sort_prefix = sub(".bam", "", basename(newBamFile))

print(newBamFile)
print(sort_prefix)

coords_app=F
writer = bamWriter(bamHeader, newBamFile)

# get coords from file first
if (!is.null(args[3]))
  {
  coords_list = read.table(args[3], header=F)
  for (coords in coords_list)
    {
    range = bamRange(reader, coords)
    print(range)
  
    bamSave(writer, range,refid=chrRef$ID)
    print( file.info(newBamFile)$size )
    }
  coords_app=T
  }

# add more if the file still isn't big enough
while (file.info(newBamFile)$size <= size )
  {
  rangeLen = 80000
  chrRef = referenceData[sample.int(nrow(referenceData), 1), ]
  start = sample.int(chrRef$LN-rangeLen, 1)
  print(paste(chrRef, start, sep="="))
  
  coords = c(chrRef$ID, start, (start+rangeLen))
  write.table(t(coords), sep="\t", quote=F, row.names=F, col.names=F, append=coords_app)
  range = bamRange(reader, coords)
  print(range)
  
  bamSave(writer, range,refid=chrRef$ID)
  print( file.info(newBamFile)$size )
  }
bamClose(writer)
bamClose(reader)

rdr = bamReader(newBamFile)
bamSort(rdr, prefix=sort_prefix)
create.index(rdr)
bamClose(rdr)


