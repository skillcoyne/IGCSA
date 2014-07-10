

hadoop = "/Users/sarah.killcoyne/Tools/hd/hadoop"


stream_cmd = <<CMD
#{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
-archives 'hdfs:///tools/bwa.tgz#tools,hdfs:///tmp/Test/index/all.tgz#reference' \
-D dfs.block.size=16777216 \
-D mapred.job.priority=NORMAL \
-D mapred.job.queue.name=default \
-D mapred.reduce.tasks=0 \
-D mapred.job.name="test job" \
-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
-D stream.num.map.output.key.fields=4 \
-D mapred.text.key.comparator.options=-k1,4 \
-input /reads/ERR002980.tsv \
-output /tmp/output/aln \
-mapper "ruby bwa-mapper.rb reference/ref/all.fa tools/bwa" \
-reducer "ruby direct-output.rb" \
-file "bwa-mapper.rb" -file "direct-output.rb"
CMD



# stream_cmd = <<CMD
# #{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
# -archives hdfs://localhost:9000/tools/art.tgz#tools,hdfs://localhost:9000/genomes/kiss1/derX.tgz#ref \
# -files art-mapper.rb,direct-output.rb \
# -D dfs.block.size=16777216 \
# -D mapred.job.priority=NORMAL \
# -D mapred.job.queue.name=default \
# -D mapred.job.name="ART job" \
# -D stream.num.map.output.key.fields=4 \
# -D mapred.text.key.comparator.options=-k1,4 \
# -input /tmp/art_input.txt \
# -output /tmp/art4 \
# -mapper "ruby art-mapper.rb tools/art_illumina ref/derX.fa" \
# -reducer "ruby direct-output.rb"
# CMD


`#{stream_cmd}`

#-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
#-D mapred.reduce.tasks=0 \