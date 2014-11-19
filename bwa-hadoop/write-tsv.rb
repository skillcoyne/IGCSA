require 'yaml'

class FastqToTSV

  def initialize(file1, file2, opts = {})

    unless File.exists?file1 and File.exists?file2
      $stderr.puts "Files don't exist or are not readable: #{file1}, #{file2}"
      exit(-1)
    end

    unless File.size(file1).eql?File.size(file2)
      $stderr.puts "FASTQ files are not the same size, reads may be missing (#{file1}, #{file2}"
    end

    @fastq1 = file1
    @fastq2 = file2

    @output_dir = opts[:output] || "/tmp/fastq_tsv"

    unless Dir.exists?@output_dir
      Dir.mkdir(@output_dir)
    end
puts "Output to #{@output_dir}"
  end

  def write_tsv

    f1 = File.open(@fastq1, 'r')
    f2 = File.open(@fastq2, 'r')

    tsv_name = File.basename(@fastq1).sub(/\..*/, '')
    @tsv_file = "#{@output_dir}/#{tsv_name}.tsv"
puts "Writing #{@tsv_file}"
    if File.exists?@tsv_file
      return @tsv_file
    end

    fout = File.open(@tsv_file, 'w')

    count = 0
    while line1 = f1.gets and line2 = f2.gets
      line1.chomp!; line2.chomp!

      if line1 =~ /^@\w+.*\/\d/
        read_name = line1.sub(/\/\d+$/, "")
        # double check
        unless read_name.eql? line2.sub(/\/\d+$/, "")
          $stderr.puts "Reads are not mate-pairs: #{line1}, #{line2}"
          exit(-1)
        end

        read1_info = get_read_info(f1)
        read2_info = get_read_info(f2)

        fout.puts [read_name, read1_info[:sequence], read1_info[:quality], read2_info[:sequence], read2_info[:quality] ].join("\t")
        count += 1
      end

    end
    puts "#{count} read pairs output."
    @tsv_file
  end


  :private
  def get_read_info(fh)
    # sequence
    sequence = fh.gets.chomp
    # skip '+' line
    fh.gets
    # quality info for the sequence
    qual = fh.gets.chomp
    return( {:sequence => sequence, :quality => qual} )
  end


end






dir = ARGV[0]

puts ARGV

puts("Read pair directory #{dir} does not exist or is not a directory") unless (File.exists? dir and File.directory? dir)
read_pair = Dir.glob("#{dir}/*.{fastq,fq}")

ftt = FastqToTSV.new(read_pair[0], read_pair[1], :output => dir)
tsv_file = ftt.write_tsv

unless File.size(tsv_file) > 0
  $stderr.puts "#{tsv_file} has a size of 0. #{read_pair.join(', ')}"
end

