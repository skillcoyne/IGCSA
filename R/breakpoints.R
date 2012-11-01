setwd("C:/Users/LCSB_Student/Data/sky-cgh/ESI/")
data = read.table("breakpoints/breakpoints.txt", header=TRUE, sep="\t")

data = data[ which(data$FromBand != 'p' & data$FromBand != 'q' & data$FromBand != 0), ]
data = data[ which(data$ToBand != 'p' & data$ToBand != 'q' & data$ToBand != 0), ]

# Matrix of breakpoints
freq = table(data$FromBand, data$ToBand) 

