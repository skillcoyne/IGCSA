hbase=/Users/skillcoyne/Tools/hbase-0.94.13/bin/hbase

#$hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.columns=HBASE_ROW_KEY,info:name \
genome file:///tmp/bulkload/genome

$hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.columns=HBASE_ROW_KEY,info:genome,chr:name,chr:length,chr:segments \
chromosome file:///tmp/bulkload/chr

$hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.columns=HBASE_ROW_KEY,info:genome,loc:chr,loc:start,loc:end,loc:segment,bp:seq sequence file:///tmp/bulkload/seq
