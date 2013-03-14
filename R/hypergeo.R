rm(list=ls())
setwd("~/workspace/IGCSA/R")
source("lib/gc_functions.R")

dir = "~/Data"
cpg_dir = paste(dir, "/VariationNormal/CpG/1000", sep="")
cpg_files = list.files(path=cpg_dir, pattern="*.txt")

file = 'chr1-varCpG.txt'
cpg_file = paste(cpg_dir, file, sep="/")
cg = read.table(cpg_file, header=T, sep="\t")
cg$Pred.CpGI[ which(is.na(cg$CpGI.Meth)) ] = NA  

rm(cpg_dir, cpg_file, dir, file)

vector("numeric", 100)->vv
for(i in 1:length(vv))
  {
  vv[i]  =  sum(cg$SNV == i)
  }
plot(vv)

#bump occurs in SNVs betwwn 2:28 (roughly)

snvCutoff = 1
snvCutoffUpper = 29
N = nrow(cg) # all

snvBump = cg[cg$SNV > snvCutoff & cg$SNV < snvCutoffUpper ,]

R = sum(!is.na(cg$CpGI.Meth)) # only methylated
n = nrow(snvBump)  # only snp region 
r = sum(!is.na(snvBump$CpGI.Meth) ) # only those methylated in the snp region

phyper(r,R,N-R,n)

# so is this unusually lower than expected
expected_value = R/N *n
expected_value

#so in regions that have methylation do *not* account for the bump
#or to put it another way - regions that have btw 2-28 SNPs are less methylated than other regions
# --> I would expect this since methylation seems to promote SNP variation. This should also correspond to the lower GC rich regions  = -

# and how does this compare to GC

# lets take high GC as being 2 std from mean

gclower = mean(cg$GC) + 2*sd(cg$GC)
gcupper = max(cg$GC)

R = sum(cg$GC > gclower)
n = nrow(snvBump)

r = sum(snvBump$GC > gclower ) 

phyper(r,R,N-R,n)
expected_value = R/N *n
expected_value

#the fragments that have SNPs in the 2-29 bump have a lot lower proporiton of high GC content then they should
#like really lower

#interestingly though - lower GC is also not well represented

gclower = mean(cg$GC) - 2*sd(cg$GC)
gcupper = max(cg$GC)
R = sum(cg$GC < gclower) 
n = nrow(snvBump)
r = sum(snvBump$GC < gclower ) 

phyper(r,R,N-R,n)

#so extreme (low or high) GC values are less likely to be found in the bump

#so we can check again by looking at coorelations in the regions where the majority of the low/high GC are
onesToCheck = cg$SNV > snvCutoff & cg$SNV < snvCutoffUpper   

cor.test(cg[!onesToCheck,"SNV"], cg[!onesToCheck,"GC"], m="s") ## ???


cor.test(cg[!onesToCheck,"SNV"], cg[!onesToCheck,"CpGI.Meth"], m="s") ## ???

#these have a good correlation 
#so think u could justify doing some sorta mixture modelling if you wanted to
#althoguh no idea why you would as you could just sample based on GC

#basically
gclower = mean(cg$GC) - 2*sd(cg$GC)
gcLowRows = cg$GC < gclower
gchigher = mean(cg$GC) + 2*sd(cg$GC)
gcHighRows = cg$GC > gchigher

mean(cg[gcLowRows,"SNV"])
mean(cg[!(gcLowRows+gcHighRows),"SNV"])
mean(cg[gcHighRows,"SNV"])


## Break up the gc content by some sd's and t-test each variation to see what works better for each 
## variation type.  After cutoffs are selected this way, can do mann-whitney to support it (for pub)
sum(cg[,"SNV"] > 0)
t.test( cg[gcLowRows,"SNV"], cg[gcLowRows, "GC"]  )

summary(cg[,'GC'])
ranges = c(
  max(cg[,'GC']),
  mean(cg[,'GC']) + 4*sd(cg[,'GC']),
  mean(cg[,'GC']) + 3*sd(cg[,'GC']),
  mean(cg[,'GC']) + 2*sd(cg[,'GC']),
  mean(cg[,'GC']) + sd(cg[,'GC']),
  mean(cg[,'GC']),
  mean(cg[,'GC']) - sd(cg[,'GC']),
  mean(cg[,'GC']) - 2*sd(cg[,'GC']),
  mean(cg[,'GC']) - 3*sd(cg[,'GC']),
  0)
ranges = rev(ranges)

tests = data.frame(t.statistic=0, n.rows=0, min=0, max=0)
for (i in 1:length(ranges))
  {
  if (i == length(ranges)) break
  min = ranges[i]; max = ranges[i+1]
  rows = cg$GC > min & cg$GC <= max
  #test = vector("numeric", length=4)
  
  for (var in colnames(cg[,1:7]))
    {
    tt = t.test(cg[rows, "GC"], cg[rows, var])
    tests[i, paste(var,'t','statistic', sep='.')] = tt$statistic
    }
  tests[i, 'n.rows'] = nrow(cg[rows,])
  tests[i, 'min'] = min
  tests[i, 'max'] = max
  
  #tests = cbind(tests, test)
  
  
  }

tests



