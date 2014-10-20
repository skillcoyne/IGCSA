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

INSTANCE_TYPE="m2.xlarge"

PRICE="0.03"


GENOME_DATA="s3://${BUCKET}/hbase/$4"


echo "Running IGCSA pipeline with ${CORES} core instances (${INSTANCE_TYPE}). On failure: ${TERM}"

JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"


MASTER="InstanceGroupType=MASTER,InstanceCount=1,InstanceType=${INSTANCE_TYPE},BidPrice=${PRICE}"
CORE="InstanceGroupType=CORE,InstanceCount=${CORES},InstanceType=${INSTANCE_TYPE},BidPrice=${PRICE}"

HBASE="Path=s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase,Args=[-s,hbase.rpc.timeout=${TIMEOUT},-s,hbase.regionserver.lease.period=${TIMEOUT},-s,hbase.regionserver.handler.count=30]"


STEPS="Name=LoadHBASE,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=TERMINATE_CLUSTER,Args=[hbaseutil,-d,s3n://${BUCKET}/hbase,-c,IMPORT,-t,genome,-t,chromosome,-t,sequence,-t,small_mutations]"

random_search_args="-b,s3n://${BUCKET}/tools/bwa.tgz,-o,s3n://${BUCKET}/Random,-g,GRCh37,-r,s3n://${BUCKET}/reads/HCC1954/discordant.tsv,-s,200"
  STEPS="${STEPS} Name=RandomSearch,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=CONTINUE,Args=[randomsearch,${random_search_args}]"

aws emr create-cluster --name 'IGCSA randomsearch v0.01' --applications Name=HBase --ami-version 2.4.8 --auto-terminate --enable-debugging --log-uri s3://${BUCKET}/logs \
--ec2-attributes KeyName=amazonkeypair \
--bootstrap-actions $HBASE Path=s3://insilico/tools/bootstrap_R.sh \
--instance-groups $MASTER $CORE --steps $STEPS


# [
#            {
#              "Name": "string",
#              "Args": ["string", ...],
#              "Jar": "string",
#              "ActionOnFailure": "TERMINATE_CLUSTER"|"CANCEL_AND_WAIT"|"CONTINUE",
#              "MainClass": "string",
#              "Type": "CUSTOM_JAR"|"STREAMING"|"HIVE"|"PIG"|"IMPALA",
#              "Properties": "string"
#            }
#            ...
#          ]




