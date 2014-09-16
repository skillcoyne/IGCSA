require 'yaml'
require 'mysql2'

def get_breakpoint_pos(chr, band)
  results = @client.query("SELECT * FROM chromosome_bands WHERE chromosome = '#{chr}' AND band = '#{band}' ORDER BY start")
  results.each do |row|
    return row['end'].to_i - row['start'].to_i
  end
end

@client = Mysql2::Client.new(:host => 'localhost', :username => 'root', :password => '', :database => 'karyotypes')

dir = ARGV[0]
#dir = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954"
fout = File.open("#{dir}/#{File.basename(dir)}.stats", 'w')
fout.puts ["chr", "total", "ppairs", "scores", "lengths", "side", "direction"].join("\t")


mapped_candidates = Hash.new
Dir["#{dir}/*"].each do |dchr|

  next unless File.directory? dchr

  bams = Dir["#{dchr}/*.bam"]
  if bams.size > 1
    $stderr.puts "Multiple bam files in #{dchr}"
  end
  bam = bams[0]
  puts bam
  mapped_candidates[bam] = {}

  unless  File.size(bam) > 0
    $stderr.puts "bam file empty #{dchr}: #{File.size(bam[0])}"
    #`samtools view -hFb 4 #{bam[0]} > #{dchr}/mapped-reads.bam`
    next
  end

  sorted = false
  reference = ""
  length, pos = 0
  `samtools view -H #{bam}`.each_line do |line|
    cols = line.split("\t")
    sorted = true if line.start_with?"@HD" and line.match(/SO:coordinate/)
    if line.start_with? "@SQ"
      reference = cols[1]
      length = cols[-1].sub("LN:", "").to_i
      bands = cols[1].split("|")[-1].sub("trans.", "").split(";")

      bands[0] =~ /^(\d+|X|Y)([q|p]\d+)/
      pos = get_breakpoint_pos($1, $2)
    end
  end
  puts [length, pos].join("\t")

  if !sorted
    $stderr.puts "WARNING #{bam} file may not be sorted."
  end


  if length.eql? 0 or pos.eql? 0
    $stderr.puts "Failed to determine length or bp position"
    next
  end

  stats = `samtools flagstat #{bam}`.split("\n")
  total_reads = stats[0].split(/\s/)[0].to_f
  total_pp = stats[6].split(/\s/)[0].to_f

  properly_paired = (total_pp/total_reads)
  puts properly_paired.round(5)

  mapped_candidates[bam]["ppair"] = properly_paired.round(5)
  mapped_candidates[bam]["total_r"] = total_reads

  # Create a hash with 5 entries, 2 for each band, 1 around the bp.  Count the depth for each entry
  left = pos/2
  right = (length-pos)/2
  mid = length/5


  left_band = [Range.new(1, 1+left), Range.new(1+left, pos)]
  right_band = [Range.new(pos, pos+right), Range.new(pos+right, length)]
  mid_band = Range.new(pos-mid, pos+mid)

  scores = Hash.new

  [left_band, mid_band, right_band].flatten.each { |r| scores[r] = 0 }

  `samtools depth #{bam}`.each_line do |line|
    line.chomp!
    (ref, loc, count) = line.split("\t")
    keys = scores.keys.select { |r| r.include? loc.to_i }
    keys.each { |k| scores[k] += count.to_i }
  end
  puts scores

  total = scores.map{|k,v| v }.reduce(:+)
  if total <= 0
    $stderr.puts "depth failed, skipping."
    next
  end

  mapped_candidates[bam]["scores"] = scores

  ls = scores.map { |k,v|  (left_band.include?k) ? v: 0 }.reduce(:+)
  rs = scores.map { |k,v|  (right_band.include?k) ? v: 0 }.reduce(:+)

  # plus minus 10% perhaps
  ms = Range.new(scores[mid_band]-(scores[mid_band]*0.1),  scores[mid_band]+(scores[mid_band]*0.1))

  band = ""
  direction = ""
  if ms.min > ls and ms.min > rs
    band = 0
    direction = "."
    puts "*** Good bp... #{reference}"
  elsif ls < rs
    direction = (scores[left_band[0]] > scores[left_band[1]])? "upstream": "downstream"
    band = 'left'
  else
    direction = (scores[right_band[0]] > scores[right_band[1]])? "upstream": "downstream"
    band = 'right'
  end

  #fout.puts ["chr", "total", "ppairs", "scores", "side", "direction"].join("\t")
  fout.puts [File.basename(File.dirname(bam)), total_reads, properly_paired, scores.values.join(","), scores.map{|k,v| (k.max-k.min).to_f }.join(","), band, direction ].join("\t")
  #fout.puts [File.basename(File.dirname(bam)), total_reads, properly_paired, scores.map{ |k,v| (v.to_f/(k.max-k.min).to_f).round(5) }.join(","), band, direction ].join("\t")

  # fout.puts [reference, band, direction, ls, rs, scores[mid_band]].join("\t")
   fout.flush


end


puts YAML::dump mapped_candidates

fout.close
