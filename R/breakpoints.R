setwd("C:/Users/LCSB_Student/Data/")
data = read.table("breakpoints-mm-chr.txt", header=TRUE, sep="\t")

data = data[order(data$Chr),]
write.table(table(data$Breakpoint), "breakpoint-freq.txt", row.name=FALSE,quote=FALSE, sep="\t")

freq = log(table(data$Breakpoint))
ymax = max(freq)

# Too many bps, plot by chr - output to PDF at some point
par(mfrow=c(4,6))
for (i in c(1:22, c("X", "Y")))
  {
  print(i)
  nd = subset(data, data$Chr == i)
  freq = table(as.vector(nd$Breakpoint))
print(freq)
  title = paste("Chr", i, sep=" ")
#  dev.new()
  plot(log(freq), ylim=c(0, ymax), main=title, xlab="breakpoints", ylab="log(freq)")
  }

