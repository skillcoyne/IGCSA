#!/bin/sh

emr="/Users/skillcoyne/Tools/elastic-mapreduce-ruby/elastic-mapreduce"

ruby $emr --create --name "Load FASTA" --ami-version 2.4.2  --enable-debugging --log-uri s3://insilico/logs \
--set-termination-protection false --key-pair amazonkeypair \
--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07 \
--instance-group core --instance-type m1.large --instance-count 3 --bid-price 0.07 \
--hbase \
--jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.LoadFromFASTA --arg Mini --arg s3n://insilico/MiniFASTA --step-action TERMINATE_JOB_FLOW \
--jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.hbase.HBaseUtility --arg Mini --arg s3n://insilico/MiniFASTA --step-action TERMINATE_JOB_FLOW \


