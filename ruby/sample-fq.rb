require 'yaml'
require 'fileutils'

class FASTQReader
  attr_reader :rd_idx

  def initialize(dir)

    fq = Dir["#{dir}/*.fq"]

    @file1 = fq.grep( /\.1\./ )[0]
    @file2 = fq.grep( /\.2\./ )[0]

    unless File.exists? @file1 and File.exists? @file2
      $stderr.puts "FASTQ for #{dir}/#{prefix} does not exist, exiting"
      exit(1)
    end

    num_reads = (%x{wc -l < "#{@file1}"}.to_i)/4
    @rd_idx = (0..num_reads).step(4).to_a

    open()
  end

  def open
    @f1 = File.open(@file1, 'r')
    @f2 = File.open(@file2, 'r')
  end

  def get_read_number(rd)

    line_no = @rd_idx[rd]
    #puts "Read #{rd} at line number #{line_no}"
    #puts @rd_idx.length

    if (line_no < @f1.lineno)
      close()
      open()
    end

    while line_no > @f1.lineno
      @f1.readline
      @f2.readline
    end

    return get_read
  end

  def close
    @f1.close
    @f2.close
  end

  :private
  def get_read
    curr_rd1 = FQRead.new
    curr_rd2 = FQRead.new
    Array(1..4).each do
      curr_rd1.add(@f1.readline.chomp)
      curr_rd2.add(@f2.readline.chomp)
    end

    unless curr_rd1.name.eql?curr_rd2.name
      $stderr.puts "Reads at #{@f1.lineno} don't match, skipping."
      return nil
    end

    return {1 => curr_rd1, 2 => curr_rd2}
  end
end

class FQRead
  attr_accessor :num, :name, :read, :qual

  def add(str)
    return if (str.eql?"+")

    if (str =~ /^(.*)\/([1|2])$/)
      @name = $1
      @num = $2
    elsif (str =~ /^[A|T|G|C|N]+$/)
      @read = str
    else
      @qual = str
    end
  end

  def to_s
    str = "#{@name}/#{@num}\n#{@read}\n+\n#{@qual}\n"
    return str
  end
end

class FASTQWriter
  attr_accessor :read_pair

  def initialize(output_dir)
    unless File.exists?output_dir
      FileUtils.mkpath output_dir
    end

    file = "#{output_dir}/reads"
    warn("Overwriting #{file}") if File.exists?"#{file}.1.fq"

    @fout1 = File.open("#{file}.1.fq", 'w')
    @fout2 = File.open("#{file}.2.fq", 'w')
  end

  def write(fqread1, fqread2)
    unless fqread1.class.eql?FQRead and fqread2.class.eql?FQRead
      $stderr.puts "Requires a FQRead object"
      return
    end

    @fout1.write(fqread1.to_s)
    @fout2.write(fqread2.to_s)
  end

  def flush
    @fout1.flush
    @fout2.flush
  end

  def close
    flush()
    @fout1.close
    @fout2.close
  end

end


if ARGV.length < 2
  $stderr.puts "Usage #{$0} <FASTQ directory> <number of reads>"
  exit(1)
end

#dir = "/tmp/PatientBPs/10p14-9q13/FASTQ"
#num_reads = 1000

dir = ARGV[0]
num_reads = ARGV[1].to_i


fq = Dir["#{dir}/*.fq"]
max_reads = (%x{wc -l < "#{fq[0]}"}.to_i)/4
if max_reads < num_reads
  $stderr.puts "The maximum possible reads is #{max_reads}"
  exit(1)
end


reader = FASTQReader.new(dir)

puts "Selecting #{num_reads} reads from #{max_reads}"
rds = Hash.new
random = Random.new
while rds.length <= num_reads
  rds[random.rand(reader.rd_idx.length)] = 0

  print "." if rds.length % 100 == 0
  puts ".#{rds.length}" if rds.length % 10000 == 0
end

rds = rds.keys.sort

outdir = "#{File.dirname(dir)}/part"
if File.exists?outdir
  FileUtils.rm_f outdir
end
#n = 1
#while File.exists?outdir
#  outdir = "#{File.dirname(dir)}/part_#{n}"
#  n += 1
#end
puts "Write to #{outdir}"

writer = FASTQWriter.new(outdir)
rds.each_with_index do |rd, i|

  #puts "Read #{rd}"
  reads =  reader.get_read_number(rd)
  #puts YAML::dump reads
  writer.write(reads[1], reads[2]) unless reads.nil?

  writer.flush if i % 100 == 0
end

writer.close
reader.close
