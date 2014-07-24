#!/bin/sh



if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

#if [ $# -lt 3 ]; then
#  echo "USAGE: $0 <New Genome Name: string> <Terminate on failure: true/false> <s3 bucket> <number of cores: default=3>"
#  exit
#fi


TERM="TERMINATE_JOB_FLOW"
#if [ $2 == "false" ]; then
#  TERM="CANCEL_AND_WAIT"
#elif [ $2 != "true" ]; then
#	echo "Terminate on failure, options true/false only."
#	exit
#fi

BUCKET="insilico"

CORES=5;
if [ $# -eq 4 ]; then
  CORES=$4
fi

TIMEOUT="1200000"
if [ $CORES -le 5 ]; then
	TIMEOUT="3600000"
fi

INSTANCE_TYPE="m3.xlarge"
#if [ $CORES -gt 10 ]; then
#  INSTANCE_TYPE="m2.xlarge"
#fi

PRICE="0.05"


GENOME_DATA="s3://${BUCKET}/hbase/$4"


echo "Running ALIGN pipeline with ${CORES} core instances (${INSTANCE_TYPE}). On failure: ${TERM}"

JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"
MASTER="--instance-group master --instance-type ${INSTANCE_TYPE} --instance-count 1 --bid-price $PRICE"
CORE="--instance-group core --instance-type ${INSTANCE_TYPE} --instance-count $CORES --bid-price $PRICE"
TASK="--instance-group task --instance-type ${INSTANCE_TYPE} --instance-count $CORES --bid-price $PRICE"
HBASE="--hbase --bootstrap-action s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase --args -s,hbase.rpc.timeout=${TIMEOUT},-s,hbase.regionserver.lease.period=${TIMEOUT},-s,hbase.regionserver.handler.count=30"



ruby $EMR_HOME/elastic-mapreduce --create --region eu-west-1 --name "Score pipeline" --ami-version 2.4.2  --enable-debugging --log-uri s3://${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE $TASK $HBASE \
--jar $JAR --args hbaseutil,-d,s3n://${BUCKET}/hbase,-c,IMPORT --arg "-t" --arg "genome,chromosome,sequence,small_mutations" --step-action TERMINATE_JOB_FLOW --step-name "IMPORT genome db" \
--jar $JAR --arg pipeline --arg Run1 --arg "17,21" --args s3n://${BUCKET}/tools/bwa.tgz,s3n://${BUCKET}/output/pipeline,s3n://${BUCKET}/reads/HCC1954 \
--step-action TERMINATE_JOB_FLOW --step-name "Run pipeline"


#regex='(j-[A-Z0-9]+)'

#if [[ $ret =~ $regex ]]; then
#  job=$BASH_REMATCH
#  echo "Connecting to job $job"
#  $EMR_HOME/elastic-mapreduce --ssh $job
#fi

