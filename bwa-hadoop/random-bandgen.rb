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


exists = File.open("/tmp/gen-bands.txt", 'r').readlines.map { |e| e.chomp! }


bands = File.open("#{Dir.home}/Analysis/band_genes.txt", 'r').readlines

bands.map! { |e| e.split("\t")[0..1].join("") }
bands.delete_if { |e| e[/(q|p)11/] }


to_gen = Array.new
to_copy = Array.new

pairs = Array.new
while pairs.length < 300
  sample = bands.sample(2)

  if exists.include? sample.join("-") or exists.include? sample.reverse.join("-")
    pairs << sample
    to_copy << sample
  else
    pairs << sample
    to_gen << sample
  end

end

pairs.uniq!

to_gen.uniq!

puts to_copy.length
puts to_gen.length

manual_copy = Array.new
to_copy.each do |pair|
  mini = "#{pair.join('-')}"

  url = "s3://insilico/special_karyotypes"
  ["Random", "Random2"].each do |path|

    ls = `aws s3 ls #{url}/#{path}/#{mini}`
    if ls.eql? ""
      mini = "#{pair.reverse.join('-')}"
      ls = `aws s3 ls #{path}/#{mini}`
    end

    unless ls.eql?""
      cp = `aws s3 cp --recursive #{url}/#{path}/#{mini} #{url}/Random3/#{mini}`
      puts cp

      manual_copy.delete("Random/#{mini}")
    else
      manual_copy << "#{path}/#{mini}"
    end


  end

end


File.open("/tmp/copy-pairs.txt", 'a'){|f|
  manual_copy.each{|e| f.puts e}
}




pairs = to_gen

$base_s3 = "s3://insilico"
steps = Array.new
pairs.each do |pair|
  step = Hash.new

  step["Name"] = "Generate bands #{pair}"
  step["Type"] = "CUSTOM_JAR"
  step["Jar"] = "#{$base_s3}/HBase-Genomes-1.2.jar"
  step["ActionOnFailure"] = "CONTINUE"
  step["Args"] = ["minichr",
                  "-o", "#{$base_s3}/special_karyotypes",
                  "-n", "Random3",
                  "-g", "GRCh37",
                  "-b", "#{$base_s3}/tools/bwa.tgz",
                  "-band", "#{pair[0]}", "-band", "#{pair[1]}"]

  steps << step

  if steps.length.equal? 150
    run_cluster(steps)
    steps.clear
  end

end

if steps.length > 0
  run_cluster(steps)
end
