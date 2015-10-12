require 'yaml'
require 'json'

def run_cluster(steps)
  #instance = "m2.xlarge"
  instance = "m3.xlarge"
  bid = "0.045"

  stream_cmd = "aws emr create-cluster --name 'Generate pairs' --ami-version 2.4.8 --applications Name=HBase --auto-terminate \
--enable-debugging --log-uri #{$base_s3}/logs --ec2-attributes KeyName=amazonkeypair \
--bootstrap-actions Path=s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase,Args=[-s,hbase.rpc.timeout=3600000,-s,hbase.regionserver.lease.period=3600000,-s,hbase.regionserver.handler.count=30] \
--instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=#{instance} InstanceGroupType=CORE,InstanceCount=5,InstanceType=#{instance},BidPrice=#{bid}"

  hbase = Hash.new
  hbase["Name"] = "Load HBase"
  hbase["Type"] = "CUSTOM_JAR"
  hbase["Jar"] = "#{$base_s3}/HBase-Genomes-1.2.jar"
  hbase["ActionOnFailure"] = "TERMINATE_JOB_FLOW"
  hbase["Args"] = ["hbaseutil", "-d", "#{$base_s3}/hbase", "-c", "IMPORT", "-t", "genome", "-t", "chromosome", "-t", "sequence"]

  steps.unshift(hbase)

  steps = JSON.generate(steps)
  cmd = "#{stream_cmd} --steps '#{steps}'"
  ret = `#{cmd}`
  puts ret
end


$base_s3 = "s3://insilico"
steps = Array.new

step = Hash.new
step["Name"] = "Generate GA2 bands"
step["Type"] = "CUSTOM_JAR"
step["Jar"] = "#{$base_s3}/HBase-Genomes-1.2.jar"
step["ActionOnFailure"] = "CONTINUE"
step["Args"] = ["gasearch",
                "-o", "#{$base_s3}/special_karyotypes/GA2",
                "-g", "GRCh37",
                "-b", "#{$base_s3}/tools/bwa.tgz",
                "-s", "200",
                "-r", "#{$base_s3}/insilico/reads/HCC1954/discordant.tsv"]

steps << step

run_cluster(steps)