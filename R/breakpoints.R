setwd("C:/Users/LCSB_Student/Data/")
data = read.table("breakpoints-mm-chr.txt", header=TRUE, sep="\t")
data = data[order(data$Chr),]
allfreq = table(data$Breakpoint)
ymax = max(log(allfreq))
allevents = length(allfreq)

fm = as.matrix(allfreq)

# simple probability
fm = cbind(fm, log(fm[,1]), round((fm[,1]/allevents), digits=4))
colnames(fm) = c('freq', 'log.freq', 'prob')
fm = as.data.frame(fm)
write.table(fm, "ind-breakpoint-freq.txt", row.name=TRUE,quote=FALSE, sep="\t")

# p/q 12 appear to be centromeric regions
# guessing that the regions nearest the centromeres
# are unstable
cen = vector(mode="character")
for (i in c(1:22, c("X", "Y")))
  {
  #cen = c(cen, paste(i,"q12", sep=""))
  #cen = c(cen, paste(i,"p12", sep=""))

  for (b in c(10:13))
    {
    cen = c(cen, paste(i, "q", b, sep=""))
    cen = c(cen, paste(i, "p", b, sep=""))
    }
  }
cent = fm[c(cen),]
arms = fm[rownames(fm) %in% cen == FALSE,]

a = sum(cent$freq, na.rm=TRUE)
b = sum(arms$freq, na.rm=TRUE)
t=a+b
a/t
b/t


#quarts = fivenum(fm$prob)
## drop everyting in the bottom quartile as these have a freq below 2
#fm = fm[fm$prob > quarts[2],]


## min/max prob
# fm[fm$prob == max(fm$prob),]
# fm[fm$prob == min(fm$prob),]





# Too many bps, plot by chr - output to PDF at some point
par(mfrow=c(4,6))
for (i in c(1:22, c("X", "Y")))
  {
  nd = subset(data, data$Chr == i)
  freq = table(as.vector(nd$Breakpoint))
pn = qnorm(log(freq))
  title = paste("Chr", i, sep=" ")
#  dev.new()
  plot(pn, ylim=c(0, 1), main=title, xlab="breakpoints", ylab="log(freq)")

#  plot(log(freq), ylim=c(0, ymax), main=title, xlab="breakpoints", ylab="log(freq)")
  }




