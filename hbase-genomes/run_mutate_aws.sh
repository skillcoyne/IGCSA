#!/bin/bash

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

if [ $# -lt 3 ]; then
  echo "USAGE: $0 <New Genome Name: string> <Terminate on failure: true/false> <s3 bucket> <number of cores: default=3>"
  exit
fi

NAME=$1

TERM="TERMINATE_JOB_FLOW"
if [ $2 == "false" ]; then
  TERM="CANCEL_AND_WAIT"
fi

BUCKET=$3

CORES=3;
if [ $# -eq 4 ]; then
  CORES=$4
fi



echo "Running MUTATE pipeline for ${NAME} with ${CORES} core instances. On failure: ${TERM}"


JAR="s3://${BUCKET}/HBase-Genomes-1.1.jar"

MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type m1.large --instance-count $CORES --bid-price 0.07"
HBASE="--hbase --bootstrap-action s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase --args -s,hbase.rpc.timeout=1200000,-s,hbase.regionserver.lease.period=120000,-s,hbase.regionserver.handler.count=30"

#,hbase.hregion.memstore.flush.size=134217728

ruby $EMR_HOME/elastic-mapreduce --create --region eu-west-1 --name "Mutate Genome" --ami-version 2.4.2  --enable-debugging --log-uri s3://${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE $HBASE \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://${BUCKET}/hbase,-c,IMPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT genome db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://${BUCKET}/hbase/normal-variation,-c,IMPORT --arg "-t" --arg "gc_bin,snv_probability,variation_per_bin" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT variation db" \
--jar $JAR --main-class org.lcsb.lu.igcsa.MutateFragments --args -b,s3://${BUCKET}/bwa.tgz,-m,$NAME,-p,GRCh37 --step-action ${TERM} --step-name "CREATE mutated genome" \
--jar $JAR --main-class org.lcsb.lu.igcsa.GenerateFullGenome --args -g,$NAME,-b,s3://${BUCKET}/bwa.tgz --step-action CONTINUE --step-name "Generate FASTA files and index" \
--jar $JAR --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --args -d,s3://${BUCKET}/hbase,-c,EXPORT --arg "-t" --arg "genome,chromosome,sequence,karyotype_index,karyotype,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "EXPORT genome db" \


