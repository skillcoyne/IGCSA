#!/bin/bash

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

if [ $# -lt 2 ]; then
  echo "USAGE: $0 <New Genome Name: string> <Terminate on failure: true/false> <number of cores: default=3>"
  exit
fi

NAME=$1

TERM="TERMINATE_JOB_FLOW"
if [ $2 == "false" ]; then
  TERM="CANCEL_AND_WAIT"
fi

CORES=3;
if [ $# -eq 3 ]; then
  CORES=$3
fi

echo "Running MUTATE pipeline for ${NAME} with ${CORES} core instances. On failure: ${TERM}"


JAR="s3://insilico/HBase-Genomes-1.1.jar"

MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type m1.large --instance-count $CORES --bid-price 0.07"
HBASE="--bootstrap-action s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase --args -s,hbase.rpc.timeout=90000"

ruby $EMR_HOME/elastic-mapreduce --create --region eu-west-1 --name "Mutate Genome" --ami-version 2.4.2  --enable-debugging --log-uri s3://insilico/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE --hbase $HBASE \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,IMPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT genome db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase/normal-variation,-c,IMPORT --arg "-t" --arg "gc_bin,snv_probability,variation_per_bin" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT variation db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.MutateFragments --args -b,s3://insilico/bwa.tgz,-m,$NAME,-p,GRCh37 --step-action CANCEL_AND_WAIT --step-name "CREATE mutated genome" \
--jar $JAR --main-class org.lcsb.lu.igcsa.GenerateFullGenome --args -g,$NAME,-b,s3://insilico/bwa.tgz --step-action CONTINUE --step-name "Generate FASTA files and index" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://insilico/hbase,-c,EXPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "EXPORT genome db" \


