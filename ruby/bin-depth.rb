require 'yaml'
require 'fileutils'

bam = ARGV[0]
bam = "/Volumes/exHD-Killcoyne/TCGA/sequence/cell-line/HCC1954.G31860/discordant.bam"
#bam = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.6.4/1p36-10p15/FASTQ.bam"

dir = "#{File.dirname(bam)}/depth"
FileUtils.mkpath(dir)


current_chr = nil
start = 1
window = 100

depth = 0
`samtools depth #{bam}`.each_line do |line|
  line.chomp!
  (ref, loc, count) = line.split("\t")
  if current_chr.nil? # first line only
    current_chr = ref
    start = loc.to_i
  end


  loc = loc.to_i
  count = count.to_i

  unless current_chr.eql? ref
    start = 1
    depth = 0

    pchrd.keys.sort.each do |k|
      puts "#{k}\t#{pchrd[k]}"
    end

    pchrd.clear
    break
  end

  if loc >= start+window
    pchrd[start] = depth
    start += window
    depth = 0
  end

  depth += count

end


