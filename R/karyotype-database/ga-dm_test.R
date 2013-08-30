source("lib/selection.R")

## this is just something to play with at the moment
# The cost is being calculated based on the sum of adjusted band probabilities multipled by the number of bands/number of selected bands
# this results in more diversity than a cost function based directly on the probability of each band 
## ...this is currently biasing towards fewer breakpoints in a genome, this may be correct but I think the result is to aim for individuals with fewer breakpoints 
# so the "best" genomes trend towards fewer breakpoints...erm...
obj_func<-function(ind, per_band_e)
  {
  r = per_band_e[names(ind[which(ind > 0)])]
  cost = sum(r)#*(length(per_band_e)/length(r))  # commenting out the length adjustment drops the number of breakpoints per individual  
  if (cost == 0) cost = 100  # while technically a genome with no mutations is perfectly fit that is not what I'm looking for
  return(cost)
  }

random_ind_p<-function(bands, mean)
  {
  rpop = vector("numeric", nrow(bands))
  names(rpop) = bands$bands
  
  count = sample(rpois(200, mean), 1, replace=F)
  for (i in 1:count)
    rpop[ bands[roll(bands$p), 'bands'] ] = 1
  
  return(rpop)
  }

random_ind<-function(bands, mean)
  {
  rpop = vector("numeric", length(bands))
  names(rpop) = bands
  
  count = sample(rpois(200, mean), 1, replace=F)
  #count = sample(c(min:max), 1, replace=T)
  
  rpop[ sample(bands, count)] = 1
  return(rpop)  
  }

# ----------------------------------- #

df = read.table("~/Analysis/Database/cancer/all-bp-prob.txt", header=T, sep="\t")
df = df[order(-df$bp.prob),]
df = set.probs(df$bp.prob, df)

df$bands = paste(df[,'chr'], df[,'band'], sep="")

chr = read.table("~/Analysis/Database/cancer/chr_instability_prob.txt", sep="\t", col.names=c('chr','prob'))
chr = chr[order(-chr$prob),]
chr = set.probs(chr$prob, chr)

bands = paste(df[,'chr'], df[,'band'], sep="")

## INITIAL POPULATION
## Could select bands completely randomly 
initial_pop = matrix(0, ncol=length(bands), nrow=100)
colnames(initial_pop) = bands

## for now, will randomly decide how many breakpoints should be selected
for (i in 1:nrow(initial_pop))
  {
  initial_pop[i,] = random_ind_p(df[,c('bands','p')], 10)
  #initial_pop[i,] = random_ind(bands, 10)
  }

## evaluate the first population
initial_fitness = vector("numeric", nrow(initial_pop)) 
names(initial_fitness) = 1:nrow(initial_pop)
for (i in 1:nrow(initial_pop))
  initial_fitness[i] = obj_func(initial_pop[i,], band_eval)

sort(initial_fitness)

pop = initial_pop
fit = initial_fitness
repeat
  {

  # for each individual
  for (i in 1:nrow(pop))
    {
  

    }

  break # generations
  }








