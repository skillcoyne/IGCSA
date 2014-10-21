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

PRICE="BidPrice=0.03"


GENOME_DATA="s3://${BUCKET}/hbase/$4"


echo "Running IGCSA pipeline with ${CORES} core instances (${INSTANCE_TYPE}). On failure: ${TERM}"

JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"


MASTER="InstanceGroupType=MASTER,InstanceCount=1,InstanceType=${INSTANCE_TYPE}"
CORE="InstanceGroupType=CORE,InstanceCount=${CORES},InstanceType=${INSTANCE_TYPE}"

HBASE="Path=s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase,Args=[-s,hbase.rpc.timeout=${TIMEOUT},-s,hbase.regionserver.lease.period=${TIMEOUT},-s,hbase.regionserver.handler.count=30]"


STEPS="Name=LoadHBASE,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=TERMINATE_JOB_FLOW,Args=[hbaseutil,-d,s3n://${BUCKET}/hbase,-c,IMPORT,-t,genome,-t,chromosome,-t,sequence,-t,small_mutations]"

localsearch_args="-b,s3n://${BUCKET}/tools/bwa.tgz,-o,s3n://${BUCKET}/HCC1954,-g,GRCh37,-r,s3n://${BUCKET}/reads/HCC1954/discordant.tsv"
i=0
locs=("-l,5:66700001-71800001,-l,8:117700001-132032011" "-l,5:66700001-71800001,-l,8:132032011-146364022" "5:71800001-76900001,8:117700001-132032011" "5:71800001-76900001,8:132032011-146364022")
for loc in "${locs[@]}"
do
  p=""
  if [ $i -gt 0 ]; then p="_$i"; fi

  STEPS="${STEPS} Name=LocalSearch,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=TERMINATE_JOB_FLOW,Args=[localsearch,${localsearch_args},${loc}]"
  #STEPS="${STEPS} Name=Score,Type=STREAMING,ActionOnFailure=CONTINUE,Args=[-D,mapred.reduce.tasks=1,\"--files=s3://${BUCKET}/tools/read_sam_map.rb,s3://${BUCKET}/tools/evaluation_reducer.R\",-mapper,read_sam_map.rb,-reducer,evaluation_reducer.R,-input,s3://${BUCKET}/HCC1954/5q13-8q24${p}/aligned/merged.sam,-output,s3://insilico/HCC1954/mini/5q13-8q24${p}/score]"


    ((i=i+1))

done

#aws emr add-steps --cluster-id j-36QD160DZ2MPX --steps $STEPS


aws emr create-cluster --name 'IGCSA localsearch v0.01' --applications Name=HBase --ami-version 3.2.1 --auto-terminate --enable-debugging --log-uri s3://${BUCKET}/logs \
--ec2-attributes KeyName=amazonkeypair \
--bootstrap-actions $HBASE \
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




