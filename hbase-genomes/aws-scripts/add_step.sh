#!/bin/bash

if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

if [ $# -lt 5 ]; then
  echo "USAGE: $0 
	<mutate|export|generate> 
	<comma sep args: '-m,foo,-d,bar'> 
	<s3 bucket> 
	<terminate on failure: true/false> 
	<job id>"  
	exit
fi



JAR="s3://${3}/HBase-Genomes-1.1.jar"

STEP=$1
ARGS=$2
TERM="TERMINATE_JOB_FLOW"
if [ $4 == "false" ]; then
  TERM="CANCEL_AND_WAIT"
fi

if [ $STEP == 'mutate' ]; then
  ARGS="mutate,${ARGS}"
	STEP="--jar ${JAR} --args ${ARGS} --step-action ${TERM} --step-name \"CREATE mutated genome\" "
elif [ $STEP == 'export' ]; then
  ARGS="hbaseutil,${ARGS}"
	STEP="--jar $JAR --args {ARGS} --step-action ${TERM} --step-name \"EXPORT genome db\" "
elif [ $STEP == 'generate' ]; then
  ARGS="gennormal,${ARGS}"
	STEP="--jar $JAR --args ${ARGS} --step-action ${TERM} --step-name \"Generate FASTA files and index\" "
else
	echo "Step must be one of: mutate|export|generate"
  exit
fi


echo "Adding step $STEP to $5"

echo $STEP

ruby $EMR_HOME/elastic-mapreduce ${STEP} ${5}


