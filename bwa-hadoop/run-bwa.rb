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
    ['--reference', '-r', GetoptLong::REQUIRED_ARGUMENT],
    ['--run_index', '-i', GetoptLong::OPTIONAL_ARGUMENT],
    ['--read-pair-dir', '-p', GetoptLong::OPTIONAL_ARGUMENT],
    ['--help', GetoptLong::NO_ARGUMENT]
)


bwa_path, reference, index, hadoop = nil
read_pair = []
run_index = false
opts.each do |opt, arg|
  case opt
    when '--help'
      print_usage
    when '--bwa-path'
      bwa_path = arg
      print_usage unless File.exists? bwa_path
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
    when
    '--run_index'
      run_index = true
    when '--reference'
      reference = arg
      index = Dir.glob("#{reference}*")
  end
end

unless bwa_path and reference and hadoop and read_pair
  print_usage
end

#puts bwa_path
#puts read_pair
#puts reference
#puts hadoop
#puts index

#FileUtils.copy(bwa_path, "/tmp/fastq_tsv/bwa")
#tarfile = "/tmp/fastq_tsv/igcsa.tgz"
#
#unless File.exists? tarfile
#  tar = "tar -czvf #{tarfile} #{index.join(' ')} /tmp/fastq_tsv/bwa "
#  output = `#{tar}`
#  unless $?.success?
#    $stderr.puts "Failed to create tar file: #{output}.  #{tar}"
#    exit(-1)
#  end
#  puts tar
#end
#
#ftt = FastqToTSV.new(read_pair[0], read_pair[1])
#tsv_file = ftt.write_tsv
#
hdfs_input = "/tmp/igcsa"
#hadoop_reads = "#{hdfs_input}/reads"
#hadoop_output = "#{hdfs_input}/rubystream"
#
### copy input files to hdfs
hcmd = HadoopCommands.new(hadoop, hdfs_input)
#
#unless hcmd.list(:path => hadoop_output).nil?
#  hcmd.remove_from_hdfs(hadoop_output)
#end
#
#tsv_hdfs_path = hcmd.copy_to_hdfs(tsv_file, :path => "reads", :overwrite => true)
#ref_path = hcmd.copy_to_hdfs(tarfile)



#hdfs_index_files = []
#index.each do |f|
#  hdfs_index_files << hcmd.copy_to_hdfs(f, :path => "ref")
#end

#puts tsv_hdfs_path
#puts hdfs_index_files

input_path = "/tmp/kiss35/kiss35.fa"
output_path = "/tmp/kiss35/index"

if (hcmd.list(:path => output_path))
  hcmd.remove_from_hdfs(output_path)
end


lib_jar = "/Users/sarah.killcoyne/workspace/IGCSA/hbase-genomes/target/HBase-Genomes-1.1.jar"
$stderr.puts "#{lib_jar} doesn't exist" unless File.exists?lib_jar

#-archives 'hdfs:///distmap_input/execarch.tgz#execarch,/tmp/distmap/refarch.tgz#refarch' \
stream_cmd = <<CMD
#{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
-archives 'hdfs:///bwa-tools/bwa.tgz#tools' \
-libjars '#{lib_jar}' \
-D dfs.block.size=16777216 \
-D mapred.reduce.tasks=0 \
-D mapred.job.name="test job" \
-input #{input_path} \
-output #{output_path} \
-mapper "ruby index.rb tools/fastq_tsv/bwa #{output_path}" \
-file "/Users/sarah.killcoyne/workspace/IGCSA/bwa-hadoop/index.rb" \
-file "#{lib_jar}"

CMD


##-archives 'hdfs:///distmap_input/execarch.tgz#execarch,/tmp/distmap/refarch.tgz#refarch' \
#stream_cmd = <<CMD
##{hadoop}/bin/hadoop jar #{hadoop}/contrib/streaming/hadoop-streaming-1.2.1.jar \
#-archives 'hdfs://#{hdfs_input}/igcsa.tgz#tmp/fastq_tsv' \
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
#-mapper "ruby mapper.rb refarch/ref/reference.fa execarch/bin/bwa" \
#-file "/Users/sarah.killcoyne/workspace/IGCSA/bwa-hadoop/mapper.rb"
#CMD

print stream_cmd
`#{stream_cmd}`

### Merge sam files
#sam_files = []
#hcmd.list(:path => "#{hadoop_output}/sam").each do |f|
#  sam_files << "I=" + hcmd.move_from_hdfs(f, "/tmp/testmerge")
#end
#
#merge = "java -jar ~/Tools/picard-tools-1.90/picard-tools-1.90/MergeSamFiles.jar #{sam_files.join(' ')} O=#{File.basename(tsv_file)}.sam"
#puts `#{merge}`



