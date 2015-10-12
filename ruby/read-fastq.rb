require 'yaml'
require 'fileutils'

class FASTQReader
  attr_reader :rd_idx, :num_reads

  def initialize(dir, suffix)

    fq = Dir["#{dir}/*.#{suffix}"]

    @file1 = fq.grep(/\.1\./)[0]
    @file2 = fq.grep(/\.2\./)[0]

    unless File.exists? @file1 and File.exists? @file2
      $stderr.puts "FASTQ for #{dir}/#{prefix} does not exist, exiting"
      exit(1)
    end

    @num_reads = (%x{wc -l < "#{@file1}"}.to_i)/4
    @rd_idx = (0..@num_reads).step(4).to_a

    open()
  end

  def open
    @f1 = File.open(@file1, 'r')
    @f2 = File.open(@file2, 'r')
  end


  def get_read_number(rd)

    line_no = @rd_idx[rd-1]
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

  def get_read
    curr_rd1 = FQRead.new
    curr_rd2 = FQRead.new

    if @f1.eof?
      return nil
    end

    Array(1..4).each do
      curr_rd1.add(@f1.readline.chomp)
      curr_rd2.add(@f2.readline.chomp)
    end

    unless curr_rd1.name.eql? curr_rd2.name
      $stderr.puts "Reads at #{@f1.lineno} don't match, skipping."
      return nil
    end

    return {1 => curr_rd1, 2 => curr_rd2}
  end
end

class FQRead
  attr_accessor :num, :name, :read, :qual

  def initialize
    @str = Array.new
  end

  def add(s)
    @str << s

    if @str.length.eql? 4
      @str[0] =~ /^(.*)\/([1|2])$/
      @name = $1
      @num = $2

      @read = @str[1]
      @qual = @str[3]

      @str.clear
    end
  end

  def phred_score
    return(@qual.split("").inject(0) { |sum, e| sum + (e.ord - 33) })
  end

  def to_s
    return "#{@name}/#{@num}\n#{@read}\n+\n#{@qual}\n"
  end
end


def phred_score(str)
  return(str.split("").inject(0) { |sum, e| sum + (e.ord - 33) })
end


puts phred_score("2ABE#BDDCA+CDD@#EC##FC#=#EDC##C@D#9DC#B#CEC#C#;<CDCB#DF#BDFDEC#ADC##EE##D#C###DDD##E###BF##DEA#A#ADF")

exit


dir="/Volumes/exHD-Killcoyne/patients/PatientBP2/10p14-9q13/FASTQ/kirc.profile"
#dir="/Volumes/exHD-Killcoyne/patients/KIRC-Patient/kirc"

reader = FASTQReader.new(dir, "fq")

phred = Array.new
read_len = Array.new
while ((read = reader.get_read) != nil)
  puts YAML::dump read

  read.each_key { |k|
    phred << read[k].phred_score
    read_len << read[k].read.length
  }
  break if reader.num_reads >= 300
end
puts reader.num_reads
puts phred.reduce(:+).to_f / phred.length

puts read_len.reduce(:+).to_f / read_len.length
