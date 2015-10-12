

bwa_path = ARGV[0]
output = ARGV[1]

$stderr.puts "-- Path: #{bwa_path} --"

ref_filename = "reference.fa"
tmp_file = File.open(ref_filename, 'w')

$stdin.each do |line|
  tmp_file.puts(line)
end

tmp_file.close

cmd = "#{bwa_path} index -a bwtsw #{ref_filename}"#"-p #{output}/#{File.basename(ref_filename, '.fa')}"
$stderr.puts "** Cmd: #{cmd} **"

output = `#{cmd}`

$stderr.puts "****  #{output}  ****"