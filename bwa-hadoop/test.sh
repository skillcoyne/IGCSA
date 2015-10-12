#!/bin/sh




$HADOOP_HOME/bin/hadoop jar $HADOOP_HOME/contrib/streaming/hadoop-streaming-1.2.1.jar \
-D dfs.block.size=16777216 \
-D mapred.job.priority=NORMAL \
-D mapred.job.queue.name=default \
-D mapred.job.name="test job" \
-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
-D stream.num.map.output.key.fields=1 \
-D mapred.text.key.comparator.options=-k1n \
-D map.output.key.field.separator=\t \
-input /tmp/pileup.txt \
-output /tmp/pileout \
-mapper "ruby pileup-mapper.rb" \
-file "pileup-mapper.rb" \

