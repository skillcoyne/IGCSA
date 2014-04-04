#!/bin/bash

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

if [ $# -lt 4 ]; then
  echo "USAGE: $0 <Genome Name: string> <Terminate on failure: true/false> <s3 bucket> <hbase subdir> <number of cores: default=3>"
  exit
fi

NAME=$1

TERM="TERMINATE_JOB_FLOW"
if [ $2 == "false" ]; then
  TERM="CANCEL_AND_WAIT"
elif [ $2 != "true" ]; then
	echo "Terminate on failure, options true/false only."
	exit
fi

BUCKET=$3

CORES=3;
if [ $# -eq 5 ]; then
  CORES=$5
fi

GENOME_DATA="s3://${BUCKET}/hbase/$4"


TIMEOUT="1200000"
if [ $CORES -le 5 ]; then
	TIMEOUT="3600000"
fi

INSTANCE_TYPE="m1.large"
#if [ $CORES -gt 10 ]; then
#  INSTANCE_TYPE="m2.xlarge"
#fi


echo "Running GENERATE pipeline for ${NAME} with ${CORES} core instances (${INSTANCE_TYPE}). On failure: ${TERM}"


JAR="s3://${BUCKET}/HBase-Genomes-1.1.jar"
OUTPUT="s3://${BUCKET}/figg-output"

MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type ${INSTANCE_TYPE} --instance-count $CORES --bid-price 0.07"
HBASE="--hbase --bootstrap-action s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase --args -s,hbase.rpc.timeout=${TIMEOUT},-s,hbase.regionserver.lease.period=${TIMEOUT},-s,hbase.regionserver.handler.count=30"

ruby $EMR_HOME/elastic-mapreduce --create --region eu-west-1 --name "Generate FASTA for ${NAME} (${CORES})" --ami-version 2.4.2  --enable-debugging --log-uri s3://${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE $HBASE \
--jar $JAR --args hbaseutil,-d,$GENOME_DATA,-c,IMPORT --arg "-t" --arg "genome,chromosome,sequence,small_mutations" --step-action ${TERM} --step-name "IMPORT genome db" \
--jar $JAR --args gennormal,-m,$CORES,-g,$NAME,-o,${OUTPUT} --step-action TERMINATE_JOB_FLOW --step-name "Generate FASTA files and index" \


