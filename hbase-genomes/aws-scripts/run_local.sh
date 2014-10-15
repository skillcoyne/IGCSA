#!/bin/bash

JAR=$HOME/workspace/IGCSA/hbase-genomes/target/HBase-Genomes-1.2.jar

BWA="-b /tools/bwa.tgz"
OUT="/pipeline/HCC1954"
READS="/reads/HCC1954/discordant.tsv"

MINI_ARGS="${BWA} -o ${OUT} -g GRCh37 -n mini"

                                       #"5:66700001-71800001,8:117700001-132032011"

i=1
locs=( "5:66700001-71800001,8:132032011-146364022" "5:71800001-76900001,8:117700001-132032011" "5:71800001-76900001,8:132032011-146364022")
for loc in "${locs[@]}"
do

  $HOME/Tools/hd/hadoop/bin/hadoop jar $JAR minichr -l $loc $MINI_ARGS
  $HOME/Tools/hd/hadoop/bin/hadoop jar $JAR align $BWA -i $OUT/mini/5q13-8q24_$i/index/all.tgz -n 5q13-8q24 -r $READS -o $OUT/mini/5q13-8q24_$i/align
  $HOME/Tools/hd/hadoop/bin/hadoop jar /Users/sarah.killcoyne/Tools/hd/hadoop/contrib/streaming/hadoop-streaming-1.2.1.jar -D mapred.reduce.tasks=1 -input $OUT/mini/5q13-8q24_$i/align/merged.sam -output $OUT/mini/5q13-8q24_$i/align/score -mapper ~/workspace/IGCSA/ruby/read_sam_map.rb -reducer ~/workspace/IGCSA/R/alignment/evaluation_reducer.R

  ((i=i+1))

done
