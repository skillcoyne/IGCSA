require 'yaml'
require 'getoptlong'

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
    ['--read-pair-dir', '-p', GetoptLong::OPTIONAL_ARGUMENT],
    ['--help', GetoptLong::NO_ARGUMENT]
)


bwa_path, reference, hadoop = nil
read_pair = []
opts.each do |opt, arg|
  case opt
    when '--help'
      print_usage
    when '--bwa-path'
      bwa_path = arg
      print_usage unless File.exists?bwa_path
    when '--hadoop-path'
      hadoop = arg
      print_usage unless File.exists?hadoop
    when '--read-pair-dir'
      if arg =~ /^hdfs:\/\//  # hdfs
        read_pair = arg ## TODO
      else
        dir = arg
        print print_usage unless (File.exists?dir and File.directory?dir)
        read_pair = Dir.glob("#{dir}/*.fastq")
      end
    when '--reference'
      if arg =~ /^hdfs:\/\//  # hdfs
        reference = arg ## TODO
      else # local
        if File.directory?arg
          # concat all chr files then set reference to concatenated file
        else
          reference = arg
        end
      end
  end
end

unless bwa_path and reference and hadoop and read_pair.length.eql?2
  print_usage
end

puts bwa_path
puts read_pair
puts reference
puts hadoop


