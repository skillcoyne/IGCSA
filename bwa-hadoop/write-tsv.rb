require_relative 'lib/fastq_to_tsv'




dir = ARGV[0]
print print_usage("Read pair directory #{dir} does not exist or is not a directory") unless (File.exists? dir and File.directory? dir)
read_pair = Dir.glob("#{dir}/*.fastq")

ftt = FastqToTSV.new(read_pair[0], read_pair[1])
tsv_file = ftt.write_tsv

unless File.size(tsv_file) > 0
  $stderr.puts "#{tsv_file} has a size of 0. #{read_pair.join(', ')}"
end

