require 'yaml'
require 'json'


files = `aws s3 ls s3://insilico/Random/mini --recursive`

files = files.split("\n")

if files.length <= 0
  $stderr.puts "No files found in bucket, exiting."
  exit(1)
end

files.map! { |e|
  es = e.split
  es[-1]
}

pair_regex = /((X|Y|\d+)[q|p]\d+-(X|Y|\d+)[q|p]\d+)/

sam_files = files.select { |e| e[/.*\.sam/] }

scores = files.select { |e| e[/.*\/score\/part.*/] }

# remove the directories that have already scored
scores.each do |scored|
  pair = scored.match(pair_regex).captures[0]
  sam_files.delete_if{|e| e.match(pair) }
end


if sam_files.length <= 0
  $stderr.puts "No SAM files found in bucket, exiting."
  exit(1)
end

cluster_id = NIL


instance = "m2.xlarge"
bid = "0.03"


stream_cmd =
    "aws emr create-cluster --name 'Streaming score' --ami-version 3.2.1 --auto-terminate --enable-debugging --log-uri s3://insilico/logs \
--ec2-attributes KeyName=amazonkeypair --bootstrap-actions Path=s3://insilico/tools/bootstrap_R.sh \
--instance-groups InstanceGroupType=MASTER,InstanceCount=1,InstanceType=#{instance},BidPrice=#{bid} \
InstanceGroupType=CORE,InstanceCount=5,InstanceType=#{instance},BidPrice=#{bid}"

if cluster_id
  stream_cmd = "aws emr add-steps --cluster-id #{cluster_id}"
end


steps = Array.new

sam_files.each do |sam|
  name = sam.match(pair_regex).captures[0]
  out = "#{File.dirname(sam)}/score"

  step = Hash.new
  step["Name"] = "Score #{name}"
  step["Type"] = "STREAMING"
  step["ActionOnFailure"] = "CONTINUE"
  step["Args"] = ["-D", "mapreduce.job.reduces=1", "--files=s3://insilico/tools/read_sam_map.rb,s3://insilico/tools/evaluation_reducer.R",
                  "-mapper", "read_sam_map.rb", "-reducer", "evaluation_reducer.R", "-input", "s3://insilico/#{sam}", "-output", "s3://insilico/#{out}"];

  steps << step
end

puts "Creating STREAMING cluster with #{steps.length} steps..."

steps = JSON.generate(steps)
File.open("/tmp/test.json", 'w') { |f| f.write steps }

## The single quotes are NECESSARY
cmd = "#{stream_cmd} --steps '#{steps}'"
puts cmd
ret = `#{cmd}`

cluster_id = JSON.parse(ret)
puts cluster_id["ClusterId"]
