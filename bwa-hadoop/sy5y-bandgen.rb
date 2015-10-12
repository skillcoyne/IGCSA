require 'yaml'
require 'json'

def run_cluster(steps)
  instance = "m2.xlarge"
  bid = "0.03"

  stream_cmd = "aws emr create-cluster --name 'Generate pairs for SH-SY5Y' --ami-version 2.4.8 --applications Name=HBase --auto-terminate \
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


# 47,XX,der(1)(1q25;1q11;1q44)
# +7
# der(9)(9q34;7q22)
# der(22)(22q13;17q21)

pairs = [
    ['1q25', '1q11'],
    ['1q11','1q44'],
    ['9q34','7q22'],
    ['22q13','17q21']
]

$base_s3 = "s3://insilico"
steps = Array.new
pairs.each do |pair|
  step = Hash.new

  step["Name"] = "Generate SYS5 bands #{pair.join(';')}"
  step["Type"] = "CUSTOM_JAR"
  step["Jar"] = "#{$base_s3}/HBase-Genomes-1.2.jar"
  step["ActionOnFailure"] = "CONTINUE"
  step["Args"] = ["minichr",
                  "-o", "#{$base_s3}/special_karyotypes",
                  "-n", "Random2",
                  "-g", "GRCh37",
                  "-b", "#{$base_s3}/tools/bwa.tgz",
                  "-band", "#{pair[0]}", "-band", "#{pair[1]}"]

  steps << step
end


  run_cluster(steps)

