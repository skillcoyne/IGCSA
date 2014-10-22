#!/bin/sh

CORES=5
INSTANCE_TYPE="m2.xlarge"
PRICE="BidPrice=0.03"

TIMEOUT="1200000"
if [ $CORES -le 5 ]; then
	TIMEOUT="3600000"
fi

AMI="3.2.1"
BUCKET="insilico"
JAR="s3://${BUCKET}/HBase-Genomes-1.2.jar"

MASTER="InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m1.large"
CORE="InstanceGroupType=CORE,InstanceCount=${CORES},InstanceType=${INSTANCE_TYPE},$PRICE"
#TASK="InstanceGroupType=Task,InstanceCount=3,InstanceType=${INSTANCE_TYPE},$PRICE"

HBASE="Path=s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase,Args=[-s,hbase.rpc.timeout=${TIMEOUT},-s,hbase.regionserver.lease.period=${TIMEOUT},-s,hbase.regionserver.handler.count=30]"

HADOOP="Path=s3://elasticmapreduce/bootstrap-actions/configure-hadoop,Args=[-c,fs.s3n.multipart.uploads.enabled=true,-m,mapreduce.task.timeout=12000000]"

STEPS="Name=LoadHBASE,Jar=$JAR,Type=CUSTOM_JAR,ActionOnFailure=TERMINATE_CLUSTER,Args=[hbaseutil,-d,s3n://${BUCKET}/hbase,-c,IMPORT,-t,genome,-t,chromosome,-t,sequence]"

aws emr create-cluster --name 'Interactive cluster' --applications Name=HBase --ami-version $AMI --no-auto-terminate \
--enable-debugging --log-uri s3://${BUCKET}/logs \
--ec2-attributes KeyName=amazonkeypair --bootstrap-actions $HADOOP $HBASE \
--instance-groups $MASTER $CORE --steps $STEPS
