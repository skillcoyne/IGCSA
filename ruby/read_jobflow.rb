require 'rubygems'
require 'json'
require 'yaml'


info = {1 => [], 3 => [], 5 => [], 7 => [], 10 => [], 15 => []}

ignore = ['j-3MOKNUQH6O69E']


file = open("#{Dir.home}/workspace/IGCSA/jobflows.json", 'r')
json = JSON.parse(file.read)
puts json

json['JobFlows'].each do |e|
  date_created = Time.at(e['ExecutionStatusDetail']['CreationDateTime']).to_date

  has_timeout = false
  e['BootstrapActions'].each do |b|
    has_timeout = true if b['BootstrapActionConfig']['ScriptBootstrapAction']['Path'] =~ /configure-hbase/
  end

  next if ignore.include? e['JobFlowId']
  next unless has_timeout
  next unless date_created.to_date.month >= 3

  count = e['Instances']['InstanceCount'].to_i - 1
  e['Steps'].each do |step|
    if  step['StepConfig']['Name'].match "CREATE" and step['ExecutionStatusDetail']['State'].match "COMPLETED"

      #puts step['ExecutionStatusDetail']['State'].match("COMPLETED")
      start_step = Time.at(step['ExecutionStatusDetail']['StartDateTime'])
      end_step = Time.at(step['ExecutionStatusDetail']['EndDateTime'])

      info[count] << ((end_step - start_step)/60).round(3) if info.has_key? count

      puts "#{e['JobFlowId']}: #{count}"
      puts start_step
      puts end_step
    end
  end

end


fout = File.open("#{Dir.home}/Dropbox/Private/runtimes.txt", 'w')
info.each_pair do |cores, v|
  fout.write "#{cores}\t"
  fout.write v.join("\t") + "\n"
end
fout.flush
fout.close

