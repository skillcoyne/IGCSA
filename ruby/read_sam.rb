require 'yaml'
require 'fileutils'


class Alignment
  attr_reader :read_name, :flag, :ref_name, :read_pos, :mapq, :cigar, :mate_ref, :mate_pos, :tlen, :seq, :phred, :tags, :cigar_totals

  def initialize(line)
    if line.split("\t").length < 12
      $stderr.puts "This may not be a SAM record: #{line}"
      return nil
    end

    (@read_name, flag, @ref_name, read_pos, mapq, @cigar, @mate_ref, mate_pos, tlen, @seq, @phred, @tags) = line.split("\t")

    @flag = flag.to_i
    @read_pos = read_pos.to_i
    @mapq = mapq.to_i
    @mate_pos = mate_pos.to_i
    @tlen = tlen.to_i

    @cigar_totals = Hash.new
    codes = @cigar.split(/[0-9]+/).reject(&:empty?)
    size = @cigar.split(/[MIDNSHPX=]/)
    codes.each_with_index do |c, i|
      @cigar_totals[c] = 0 unless @cigar_totals.has_key? c

      @cigar_totals[c] += size[i].to_i
    end


    @mate_ref = @ref_name if @mate_ref.eql? "="
  end

  def length
    len = (@read_pos - @mate_pos).abs
    @cigar_totals.each_pair do |c, s|
      len += s if c.match(/[MI=]/)
      len -= s if c.match(/[D]/)
    end
    return len
  end

  def distance
    (@read_pos - @mate_pos).abs
  end

  def cigar_to_s
    codes = @cigar.split(/[0-9]+/).reject(&:empty?)
    size = @cigar.split(/[MIDNSHPX=]/)

    tuples = Array.new
    codes.each_with_index do |c, i|
      tuples << "#{size[i]}:#{c}"
    end

    return tuples
  end

  def phred_score
    @phred.split("").inject(0){|sum, e| sum + (e.ord - 33)  }
    return(sum)
  end


  def is_same_chromosome?
    return (@mate_ref.eql? "=" or @mate_ref.eql? @ref_name)
  end

  def proper_pair?
    (@flag & 2) == 2
  end

  def mapped?
    return (read_mapped? and mate_mapped?)
  end

  def read_mapped?
    (@flag & 4) == 0
  end

  def read_paired?
    (@flag & 1) == 1
  end

  def mate_mapped?
    (@flag & 8) == 0
  end

  def read_reversed?
    (@flag & 16) == 0
  end

  def mate_reversed?
    (@flag & 32) == 0
  end

  def is_dup?
    (@flag & 1024) == 1024
  end

  def failed?
    (@flag & 512) == 512
  end

  def orientation
    return {:read => (read_reversed?) ? "F" : "R", :mate => (mate_reversed?) ? "F" : "R"}
  end

  def is_secondary?
    (@flag & 256) == 256
  end

  def first_in_pair
    (@flag & 64) == 64
  end

  def second_in_pair
    (@flag & 128) == 128
  end

end

class Bands

  def initialize(file)
    unless File.exists? file and File.readable? file
      $stderr.puts "Band file #{file} doesn't exist or isn't readable."
      exit 2
    end

    @chr_hash = Hash.new

    File.open(file, 'r').each_line do |line|
      line.chomp!

      next if line.start_with? 'chr'

      (chr, band, pstart, pend) = line.split("\t")[0..3]

      @chr_hash[chr] = Hash.new unless @chr_hash.has_key? chr

      @chr_hash[chr][band] = Range.new(pstart.to_i, pend.to_i)

    end

    def get_bands(chr)
      chr.sub!("chr", "")
      @chr_hash[chr]
    end

    def has_chr?(chr)
      chr.sub!("chr", "")
      @chr_hash.has_key? chr
    end

    def get_band(chr, loc)
      chr.sub!("chr", "")
      chrms = @chr_hash[chr]
      chrms.each_pair do |band, range|
        return band if range.include?(loc)
      end
    end

    def in_centromere?(chr, loc)
      band = get_band(chr, loc)
      return band =~ /(p|q)(11|12)/
    end

  end
end


if ARGV.length <=0
  $stderr.puts "Usage: #{$0} <output dir> <band text file>"
  exit(1)
end


band_file = ARGV[1]
bands = Bands.new(band_file)


outdir = "#{ARGV[0]}/dist"
puts outdir


FileUtils.rmtree(outdir) if Dir.exists? outdir
FileUtils.mkpath(outdir)


count = 0

files = Hash.new
## set up files
Array(1..22).push("X").push("Y").each do |chr|
  ["arm", "cent"].each do |e|
    f = File.open("#{outdir}/chr#{chr}.#{e}.reads", 'w')
    f.puts ['pos', 'mate.pos', 'length'].join("\t")

    files["#{chr}.#{e}"] = f

  end
    f = File.open("#{outdir}/disc.#{chr}.reads", 'w')
    f.puts ["ref", "pos", "mate", "mate.pos"].join("\t")

    files["disc.#{chr}"] = f
end


$stdin.each do |line|
  #print "." if count%10000 == 0
  #print "\n" if count%1000000 == 0
  next if line.start_with? "@"

  align = Alignment.new(line.chomp)

  unless align.nil?
    next unless bands.has_chr? align.ref_name

    next if align.failed? or align.is_dup? or align.proper_pair? or !align.mapped?

    # filter reads
    if align.read_paired?
      if align.is_same_chromosome?
        if bands.has_chr? align.ref_name and bands.has_chr? align.mate_ref

          # either in centromere
          if (bands.in_centromere?(align.ref_name, align.read_pos) or bands.in_centromere?(align.mate_ref, align.mate_pos))
print YAML::dump align
exit
	    files["#{align.ref_name}.cent"].puts [align.read_pos, align.mate_pos, align.tlen.abs].join("\t")
          end

          # either is in arm
          if  !bands.in_centromere?(align.ref_name, align.read_pos) or !bands.in_centromere?(align.mate_ref, align.mate_pos)
print YAML::dump align
exit
	    files["#{align.ref_name}.arm"].puts [align.read_pos, align.mate_pos, align.tlen.abs].join("\t")
          end
        end
      else
        files["disc.#{align.ref_name}"].puts [align.ref_name, align.read_pos, align.mate_ref, align.mate_pos].join("\t")
      end

    end
  end

  count += 1
end
puts count


