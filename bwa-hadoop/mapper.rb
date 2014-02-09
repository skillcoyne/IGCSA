require 'rubygems'
require 'fileutils'

class BWAAlignment

  def self.fastq_read(name, seq, qual)
    str = "#{name}\n#{seq}\n+\n#{qual}\n"
    return str
  end

  def initialize(reference, bwa)
    @random = Time.now.to_i + rand(1000)
    @ref = reference
    @bwa_path = bwa
  end

  def get_unique_filename
    @random
  end

  def run_alignment(fastq_files)
    @fastq = fastq_files

    fastq_files.each do |file|
      unless File.size(file) > 0
        $stderr.puts "FASTQ file #{file} is empty. Exiting."
        exit(-1)
      end
    end

    aln()
    sampe()
  end

  :private

  def aln
    @sai = []
    @fastq.each_with_index do |fastq, i|
      sai_file = "#{@random}_read#{i+1}.sai"
      cmd = "#{@bwa_path} aln #{@ref} #{fastq} > #{sai_file}"
      $stderr.puts "Running #{cmd}"
      #pid = fork do
        output = `#{cmd}`
        $stderr.puts "aln: #{output} #{$?}"
      #end
      #Process.waitpid(pid)

      unless $?.success?
        $stderr.puts "Failed to run alignment, error #{$?}.  #{cmd}"
        exit(-1)
      end
      @sai << sai_file
    end
  end

  def sampe
    @sam = "#{@random}.sam"

    if File.exists? @sam
      $stderr.puts "#{@sam} file already exists. Exiting."
      exit(-1)
    end

    cmd = "#{@bwa_path} sampe #{@ref} #{@sai.join(' ')} #{@fastq.join(' ')} > #{@sam}"
    $stderr.puts "Running #{cmd}"
    #pid = fork do
      output = `#{cmd}`
      $stderr.puts "sampe: #{output}: #{$?}"
    #end
    #Process.waitpid(pid)
    unless $?.success?
      $stderr.puts "Failed to run sampe, error #{$?}.  #{cmd}"
      exit(-1)
    end
    #$stderr.puts "sampe: #{output}: #{$?}"
    $stderr.puts "#{@sam} written. #{File.size(@sam)}"

    # force it to write to hdfs output
    File.open(@sam, 'r').each_line do |line|
      puts line
    end

    # is this necessary or does hadoop clean up after itself?
    @sai.each { |f| FileUtils.rm_f(f) }
    @fastq.each { |f| FileUtils.rm_f(f) }
    FileUtils.rm_f(@sam)
  end

end


### In Hadoop Streaming this script will get a single line of the TSV to process ###
# 1. turn that line into a pair of reads again
# 2. run bwa aln on that pair
# 3. run bwa sampe on the resulting sai files
# After all the mappers finish the final step would need to merge all of the files into a single bam.  That will not happen in this script.

#reference = "/Users/sarah.killcoyne/Data/FASTA/tmp/GRCh37.fa"
reference = ARGV[0]
bwa_path = ARGV[1]


bwaalign = BWAAlignment.new(reference, bwa_path)

random = bwaalign.get_unique_filename

$stderr.puts "Random #{random}"

fastq_filename_1 = "#{random}_read1.fastq";
fastq_filename_2 = "#{random}_read2.fastq"


$stderr.puts <<IN
bwa: #{bwa_path}
reference: #{reference}
fastq files: #{fastq_filename_1}, #{fastq_filename_2}
IN

fh1 = File.open(fastq_filename_1, 'w')
fh2 = File.open(fastq_filename_2, 'w')

count = 0
$stdin.each do |line|
  line.chomp!

  (name, seq1, qual1, seq2, qual2) = line.split("\t")

  # regenerate fastq lines
  fh1.write BWAAlignment.fastq_read("#{name}/1", seq1, qual1)
  fh2.write BWAAlignment.fastq_read("#{name}/2", seq2, qual2)
  count += 1
end
fh1.close
fh2.close

$stderr.puts "LINES: #{count} lines output to fastq files."
$stderr.puts "File sizes: #{File.size(fastq_filename_1)} #{File.size(fastq_filename_2)}"

bwaalign.run_alignment([fastq_filename_1, fastq_filename_2])


FileUtils.rm_f(fastq_filename_1)
FileUtils.rm_f(fastq_filename_2)



