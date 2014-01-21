require 'yaml'
require 'getoptlong'
require 'fileutils'
require_relative 'lib/fastq_to_tsv'
require_relative 'lib/hadoop_commands'


def print_usage()
  puts "Wrong"
  exit(-1)
end

def hdfs_file(file)

end

def copy_to_hdfs(file)

end

opts = GetoptLong.new(
    ['--hadoop-path', '-h', GetoptLong::REQUIRED_ARGUMENT],
    ['--bwa-path', '-b', GetoptLong::REQUIRED_ARGUMENT],
    ['--picard-path', '-s', GetoptLong::REQUIRED_ARGUMENT],
    ['--reference', '-r', GetoptLong::REQUIRED_ARGUMENT],
    ['--read-pair-dir', '-p', GetoptLong::OPTIONAL_ARGUMENT],
    ['--help', GetoptLong::NO_ARGUMENT]
)


bwa_path, picard_path, picard_sort, picard_merge, reference, hadoop = nil
read_pair = []
opts.each do |opt, arg|
  case opt
    when '--help'
      print_usage
    when '--bwa-path'
      bwa_path = arg
      print_usage unless File.exists? bwa_path
    when '--picard-path'
      picard_path = arg
      picard_merge = "#{picard_path}/MergeSamFiles.jar"
      picard_sort = "#{picard_path}/SortSam.jar"
      print usage unless File.exists? picard_path and File.directory? picard_path and File.exists? picard_sort and File.exists? picard_merge
    when '--hadoop-path'
      hadoop = arg
      print_usage unless File.exists? hadoop
    when '--read-pair-dir'
      if arg =~ /^hdfs:\/\// # hdfs
        read_pair = arg ## TODO
      else
        dir = arg
        print print_usage unless (File.exists? dir and File.directory? dir)
        read_pair = Dir.glob("#{dir}/*.fastq")
      end
    when '--reference'
      reference = arg
  end
end

unless bwa_path and picard_path and reference and hadoop and read_pair
  print_usage
end

puts "BWA: #{bwa_path}"
puts "Picard: #{picard_path}"
puts "Read pair: #{read_pair}"
puts "Indexed reference path: #{reference}"
puts "Hadoop path #{hadoop}"


hdfs_input = "/tmp/igcsa"
input_path = "#{hdfs_input}/reads"
output_path = "#{hdfs_input}/rubystream"


hcmd = HadoopCommands.new(hadoop, hdfs_input)

## Check that the reference has been generated
reference += "/index.tgz"
unless hcmd.list(:path => reference)
  $stderr.puts "Reference tgz (index.tgz) does not exist."
  exit -1
end
## reference check end ##

## Set up bwa tools
tmp_local_path = "#{hdfs_input}/tools"
bwa_path += "/bwa" if File.basename(bwa_path) != "bwa"

FileUtils.rm_r(tmp_local_path) if File.exists? tmp_local_path
FileUtils.mkpath(tmp_local_path)
FileUtils.cp(bwa_path, tmp_local_path)
FileUtils.cp(picard_merge, tmp_local_path)
FileUtils.cp(picard_sort, tmp_local_path)

FileUtils.chdir(tmp_local_path) do
  output = `tar -czvf bwa.tgz *`
  unless $?.success?
    $stderr.puts "Failed to tar bwa/picard executables: #{output}: #{$?}"
  end
end

hcmd.copy_to_hdfs("#{tmp_local_path}/bwa.tgz", :path => "/bwa-tools", :overwrite => true)
## bwa setup end ##

## Set up read files
unless hcmd.list(:path => output_path).nil?
  hcmd.remove_from_hdfs(output_path)
end

ftt = FastqToTSV.new(read_pair[0], read_pair[1])
tsv_file = ftt.write_tsv
hcmd.copy_to_hdfs(tsv_file, :path => "reads", :overwrite => true)
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
-D mapred.text.key.partitioner.options=-k1,4 \
-D mapred.text.key.comparator.options=-k1,4 \
-partitioner org.apache.hadoop.mapred.lib.KeyFieldBasedPartitioner \
-input #{input_path} \
-output #{output_path} \
-mapper "ruby mapper.rb reference/ref/reference.fa tools/bwa" \
-file "/Users/sarah.killcoyne/workspace/IGCSA/bwa-hadoop/mapper.rb"
CMD

print stream_cmd
`#{stream_cmd}`

### Merge sam files
#sam_files = []
#hcmd.list(:path => "#{output_path}").each do |f|
#  hcmd.copy_from_hdfs(f, "/tmp/testmerge")
#  sam_files << "I=/tmp/testmerge/#{File.basename(f)}"
#end
#puts sam_files

## Stream with entire file?
#lib = "../hbase"
#stream_cmd = <<CMD
##{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
#-archives 'hdfs:///bwa-tools/bwa.tgz#tools,hdfs://#{reference}#reference' \
#-D dfs.block.size=16777216 \
#-D mapred.job.priority=NORMAL \
#-D mapred.job.queue.name=default \
#-D mapred.reduce.tasks=0 \
#-D mapred.job.name="test job" \
#-D mapred.output.key.comparator.class=org.apache.hadoop.mapred.lib.KeyFieldBasedComparator \
#-D stream.num.map.output.key.fields=4 \
#-D mapred.text.key.partitioner.options=-k1,4 \
#-D mapred.text.key.comparator.options=-k1,4 \
#-partitioner org.apache.hadoop.mapred.lib.KeyFieldBasedPartitioner \
#-input #{input_path} \
#-output #{output_path} \
#-mapper "ruby mapper.rb reference/ref/reference.fa tools/fastq_tsv/bwa" \
#-mapper "ruby index.rb tools/fastq_tsv/bwa #{output_path}" \
#-file "/Users/sarah.killcoyne/workspace/IGCSA/bwa-hadoop/mapper.rb"
#CMD


#merge = "java -Xmx512m -jar ~/Tools/picard-tools-1.90/picard-tools-1.90/MergeSamFiles.jar #{sam_files.join(' ')} O=/tmp/testmerge/#{File.basename(tsv_file)}.sam"
#puts "Run merge: #{merge}"
#output = `#{merge}`
#puts "#{output}: #{$?}"



