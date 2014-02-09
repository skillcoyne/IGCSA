require 'yaml'
require 'getoptlong'
require 'fileutils'
require_relative 'lib/fastq_to_tsv'
require_relative 'lib/hadoop_commands'


# TODO  This will only work with a local installation.  So if I were running it on a local cluster it would be fine.
# It will NOT work with Amazon.
# For Amazon I need to work out if this script can run on EC2 or if I have to create a separate EC2 script.

def print_usage(msg = "")
  puts msg if msg

  puts <<MSG
All options are required.
Usage: $0
    --hadoop-path [Hadoop directory],
    --bwa-path [Directory to compiled bwa]
    --hadoop-bam-path [Directory that contains hadoop-bam jar and associated jars]
    --reference [HDFS directory that contains the reference genome with index]
    --read-pair-dir [Directory that contains read files]
    --hdfs-path [HDFS directory for outputs]


    --help [See this message]
MSG
  exit(-1)
end

def create_tool_archive(tools, tmp_dir)
  FileUtils.rm_r(tmp_dir) if File.exists? tmp_dir
  FileUtils.mkpath(tmp_dir)
  tools.each do |tool|
    FileUtils.cp(tool, tmp_dir)
  end

  FileUtils.chdir(tmp_dir) do
    output = `tar -czvf bwa.tgz *`
    unless $?.success? and File.size("bwa.tgz") > 0
      $stderr.puts "Failed to tar executables: #{output}: #{$?}"
    end
  end
end


opts = GetoptLong.new(
    ['--hadoop-path', GetoptLong::REQUIRED_ARGUMENT],
    ['--bwa-path', GetoptLong::REQUIRED_ARGUMENT],
    ['--hadoop-bam-path', GetoptLong::REQUIRED_ARGUMENT],
    ['--reference', GetoptLong::REQUIRED_ARGUMENT],
    ['--read-pair-dir', GetoptLong::REQUIRED_ARGUMENT],
    ['--hdfs-path', GetoptLong::REQUIRED_ARGUMENT],
    ['--help', GetoptLong::NO_ARGUMENT]
)


bwa_path, picard_path, sam_path, hadoop_bam, reference, hadoop, hdfs_input = nil
read_pair = []
opts.each do |opt, arg|
  case opt
    when '--help'
      print_usage
    when '--hdfs-path'
      hdfs_input = arg
    when '--bwa-path'
      bwa_path = arg
      print_usage("BWA: #{bwa_path} does not exist.") unless File.exists? bwa_path
    when '--hadoop-bam-path'
      path = arg
      print_usage("#{path} does not exist or is not a directory.") unless File.exists? path and File.directory? path
      picard_path = "#{path}/picard-1.93.jar"
      sam_path = "#{path}/sam-1.93.jar"
      hadoop_bam = "#{path}/hadoop-bam-6.0.jar"
      print_usage("HADOOP-BAM: Missing dependency (#{picard_path}, #{sam_path}, #{hadoop_bam})") unless File.exists? picard_path and File.exists? sam_path and File.exists? hadoop_bam
    when '--hadoop-path'
      hadoop = arg
      print_usage("HADOOP: #{hadoop} does not exist.") unless File.exists? hadoop
    when '--read-pair-dir'
      if arg =~ /^hdfs:\/\// # hdfs
        read_pair = arg ## TODO
      else
        dir = arg
        print print_usage("Read pair directory #{dir} does not exist or is not a directory") unless (File.exists? dir and File.directory? dir)
        read_pair = Dir.glob("#{dir}/*.fastq")
      end
    when '--reference'
      reference = arg
  end
end

unless bwa_path and picard_path and reference and hadoop and read_pair and hdfs_input
  print_usage
end

puts "BWA: #{bwa_path}"
puts "Picard: #{picard_path}"
puts "Read pair: #{read_pair}"
puts "Indexed reference path: #{reference}"
puts "Hadoop path #{hadoop}"


reads_dir = "/tmp/igcsa/reads"
output_path = "/tmp/igcsa/sam"

hcmd = HadoopCommands.new(hadoop, hdfs_input)

## Check that the reference has been generated
unless hcmd.list(:path => "#{reference}/index.tgz")
  $stderr.puts "Reference tgz (index.tgz) does not exist at #{reference}."
  exit -1
end
reference += "/index.tgz"
## reference check end ##

## Set up tool archive (currently just bwa)
tmp_local_path = "/tmp/igcsa/tools"
bwa_path += "/bwa" if File.basename(bwa_path) != "bwa"
create_tool_archive([bwa_path], tmp_local_path)
hcmd.copy_to_hdfs("#{tmp_local_path}/bwa.tgz", :path => "/bwa-tools", :overwrite => true)

## Set up read files
unless hcmd.list(:path => output_path).nil?
  hcmd.remove_from_hdfs(output_path)
end

ftt = FastqToTSV.new(read_pair[0], read_pair[1])
tsv_file = ftt.write_tsv

unless File.size(tsv_file) > 0
  $stderr.puts "#{tsv_file} has a size of 0. #{read_pair.join(', ')}"
end

hcmd.copy_to_hdfs(tsv_file, :path => reads_dir, :overwrite => true)
## read file end ##

## Few important notes here.  In the 'archives' option the part after the # sign is a shortcut reference to be used when passing that as an argument
## to mappers/reducers.
stream_cmd = <<CMD
#{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
-archives 'hdfs:///bwa-tools/bwa.tgz#tools,hdfs://#{reference}#reference' \
-D dfs.block.size=16777216 \
-D mapred.job.priority=NORMAL \
-D mapred.job.queue.name=default \
-D mapred.reduce.tasks=0 \
-D mapred.job.name="test job" \
-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
-D stream.num.map.output.key.fields=4 \
-D mapred.text.key.comparator.options=-k1,4 \
-input #{reads_dir} \
-output #{output_path} \
-mapper "ruby mapper.rb reference/ref/reference.fa tools/bwa" \
-file "mapper.rb"
CMD

#-D mapred.text.key.partitioner.options=-k1,4 \
#-partitioner org.apache.hadoop.mapred.lib.KeyFieldBasedPartitioner \


output = `#{stream_cmd}`
unless $?.success?
  $stderr.puts "#{stream_cmd}\nStreaming command failed: #{output}"
  exit $?
end

exit


## TODO In reality all I would need to do in order to merge the resulting files is read them all in, grab the headers and treat them as keys
## to the rest of the file.  Then write out the header and all values to a single sam file.  But this works for the moment so I'll just use it...
## also easier as it takes care of bam conversion
#hadoop jar hadoop-bam-6.0.jar -libjars picard-1.93.jar,sam-1.93.jar sort -F BAM -o /tmp/testmerge/test.sam /tmp/testmerge /tmp/igcsa/testsam/part-00000 /tmp/igcsa/testsam/part-00001
sam_files = hcmd.list(:path => output_path)
merged_sam = "#{output_path}/test.bam"
sam_merge_cmd = <<CMD
#{hadoop}/bin/hadoop jar #{hadoop_bam} \
-libjars '#{picard_path},#{sam_path}' \
sort -o #{merged_sam} -F BAM \
#{output_path} #{sam_files.join(' ')}
CMD

output = `#{sam_merge_cmd}`
unless $?.success?
  $stderr.puts "hadoop-bam command failed: #{output}"
  exit $?
end


