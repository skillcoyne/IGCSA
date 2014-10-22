#!/bin/sh

clusterID=$1

JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"

STEPS="--steps Name=Minichr,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=CONTINUE,Args=[minichr,${random_search_args}]"


echo $STEPS
#aws emr add-step
