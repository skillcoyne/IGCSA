#!/bin/sh

if [ $# -eq 0 ]; then
  echo "Default cores set to 3."
  CORES=3;
else
  CORES=$1
  echo "Running with ${CORES} core instances."
fi

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

ruby $EMR_HOME/elastic-mapreduce --create --name "Mutate Genome" --ami-version 2.4.2  --enable-debugging --log-uri s3://insilico/logs \
--set-termination-protection false --key-pair amazonkeypair \
--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07 \
--instance-group core --instance-type m1.large --instance-count $CORES --bid-price 0.07 \
--hbase \
--step-name "Import genome db" --jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,IMPORT --step-action TERMINATE_JOB_FLOW \
--step-name "Import variation db" --jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase/normal-variation,-c,IMPORT --step-action TERMINATE_JOB_FLOW \
--step-name "Create mutated genome" --jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.MutateFragments --args -b,s3://insilico/bwa.tgz,-m NormalS1,-p GRCh37 --step-action TERMINATE_JOB_FLOW \
--step-name "Export genome db" --jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,EXPORT --step-action TERMINATE_JOB_FLOW \


