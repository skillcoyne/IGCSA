#!/bin/sh

emr="/Users/skillcoyne/Tools/elastic-mapreduce-ruby/elastic-mapreduce"

ruby $emr --create --alive --name "Load FASTA" --ami-version 2.4.2  --key-pair amazonkeypair \
--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07 \
--instance-group core --instance-type m1.large --instance-count 3 --bid-price 0.07 \
--hbase --hbase-schedule-backup --consistent --backup-dir s3://insilico/hbase --full-backup-time-interval 2 --full-backup-time-unit minutes \
--jar s3://insilico/HBase-Genomes-1.1.jar --main-class org.lcsb.lu.igcsa.LoadFromFASTA --args GRCh37 s3://insilico/FASTA --step-action TERMINATE_JOB_FLOW 
