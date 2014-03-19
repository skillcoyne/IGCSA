#!/bin/bash

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

if [ $# -eq 0 ]; then
  echo "Default cores set to 3."
  CORES=3;
else
  CORES=$1
fi

if [ ! $2 ]; then
  echo "No name provided for new genome."
  exit;
fi

echo "Running MUTATE pipeline with ${CORES} core instances."


NAME=$2

JAR="s3://insilico/HBase-Genomes-1.1.jar"

MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type m1.large --instance-count $CORES --bid-price 0.07"

ruby $EMR_HOME/elastic-mapreduce --create --name "Mutate Genome" --ami-version 2.4.2  --enable-debugging --log-uri s3://insilico/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE \
--hbase --step-action TERMINATE_JOB_FLOW \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,IMPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT genome db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase/normal-variation,-c,IMPORT --arg "-t" --arg "gc_bin,snv_probability,variation_per_bin" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT variation db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.MutateFragments --args -b,s3://insilico/bwa.tgz,-m,$NAME,-p,GRCh37 --step-action CANCEL_AND_WAIT --step-name "CREATE mutated genome" \
--jar $JAR --main-class org.lcsb.lu.igcsa.GenerateFullGenome --args -g,$NAME,-b,s3://insilico/bwa.tgz --step-action CONTINUE --step-name "Generate FASTA files and index" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,EXPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "EXPORT genome db" \


