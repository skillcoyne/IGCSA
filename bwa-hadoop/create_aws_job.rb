require 'yaml'
require 'json'


if ARGV.length < 1
  $stderr.puts "At least on comma separated pair of chromosomes is required"
  exit
end

base_s3 = "s3n://insilico"

steps = []

step = Hash.new
step["Name"] = "Load HBase"
step["Type"] = "CUSTOM_JAR"
step["Jar"] = "#{base_s3}/HBase-Genomes-1.2.jar"
step["ActionOnFailure"] = "TERMINATE_JOB_FLOW"
step["Args"] = ["hbaseutil","-d","#{base_s3}/hbase","-c","IMPORT","-t","genome","-t","chromosome","-t","sequence"]

steps << step

ARGV.each do |cp|
  step = Hash.new

  step["Name"] = "Generate for #{cp}"
  step["Type"] = "CUSTOM_JAR"
  step["Jar"] = "#{base_s3}/HBase-Genomes-1.2.jar"
  step["ActionOnFailure"] = "CONTINUE"
  step["Args"] = ["chrpair","-o","#{base_s3}/special_karyotypes/HCC1954","-c","#{cp}","-g","GRCh37","-b", "#{base_s3}/tools/bwa.tgz"]

  steps << step
end



instance = "m2.xlarge"
bid = "0.03"


stream_cmd = "aws emr create-cluster --name 'Generate pairs' --ami-version 2.4.8 --applications Name=HBase --auto-terminate \
--enable-debugging --log-uri #{base_s3}/logs --ec2-attributes KeyName=amazonkeypair \
--bootstrap-actions Path=s3://eu-west-1.elasticmapreduce/bootstrap-actions/configure-hbase,Args=[-s,hbase.rpc.timeout=3600000,-s,hbase.regionserver.lease.period=3600000,-s,hbase.regionserver.handler.count=30] \
--instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=#{instance},BidPrice=#{bid} InstanceGroupType=CORE,InstanceCount=5,InstanceType=#{instance},BidPrice=#{bid}"


pairs = steps.length-1
steps = JSON.generate(steps)

cmd = "#{stream_cmd} --steps '#{steps}'"

#File.open("/tmp/test.json", 'w') { |f| f.write steps }

ret = `#{cmd}`
puts ret
