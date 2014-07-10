

$HADOOP_HOME/bin/hadoop jar $HADOOP_HOME//contrib/streaming/hadoop-streaming-1.2.1.jar \
-archives 'hdfs:///bwa-tools/bwa.tgz#tools,hdfs:///tmp/Test/index/all.tgz#reference' \
-D dfs.block.size=16777216 \
-D mapred.job.priority=NORMAL \
-D mapred.job.queue.name=default \
-D mapred.reduce.tasks=0 \
-D mapred.job.name="test job" \
-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
-D stream.num.map.output.key.fields=4 \
-D mapred.text.key.comparator.options=-k1,4 \
-input /reads/mini.tsv \
-output /output/mini \
-mapper 'ruby mapper.rb reference/ref/all.fa tools/bwa' \
-file "mapper.rb"

