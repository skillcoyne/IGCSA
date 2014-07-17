#!/bin/bash

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

CORES=10;
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

PRICE="0.04"


echo "Running ALIGN pipeline with ${CORES} core instances (${INSTANCE_TYPE}). On failure: ${TERM}"

JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"
MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price $PRICE"
CORE="--instance-group core --instance-type ${INSTANCE_TYPE} --instance-count $CORES --bid-price $PRICE"

ruby $EMR_HOME/elastic-mapreduce --create  --alive --region eu-west-1 --name "Align Genome ${CORES}" --ami-version 2.4.2  --enable-debugging --log-uri s3://${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE  \
--jar $JAR --args align,-b,s3n://${BUCKET}/tools/bwa.tgz,-i,s3n://${BUCKET}/genomes/GRCh37/all.tgz,-n,Test,-r,s3n://${BUCKET}/genomes/PatientOne/FASTQ,-o,s3://insilico/align --step-action ${TERM} --step-name "Align genome"

#regex='(j-[A-Z0-9]+)'

#if [[ $ret =~ $regex ]]; then
#  job=$BASH_REMATCH
#  echo "Connecting to job $job"
#  $EMR_HOME/elastic-mapreduce --ssh $job
#fi

