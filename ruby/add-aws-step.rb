
if ARGV.length < 3
  $stderr.puts "Missing args"
  exit
end

clusterID = ARGV[0]
name = ARGV[1]

stepArgs = ARGV[1..ARGV.length].join(",")

jar = "s3://insilico/HBase-Genomes-1.2.jar"

steps = "Name=#{name},Jar=#{jar},Type=CUSTOM_JAR,ActionOnFailure=CONTINUE,Args=[#{stepArgs}]"

puts steps

ret = `aws emr add-steps --cluster-id #{clusterID} --steps #{steps}`
puts ret
