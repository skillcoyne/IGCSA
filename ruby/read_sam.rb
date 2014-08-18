require 'yaml'
require 'fileutils'


class SimpleSAMReader

  attr_reader :sam, :header


  def initialize(file)
    unless File.exists? file and File.readable? file
      $stderr.puts "#{file} does not exist or is not readable."
      exit(1)
    end

    @sam = file
    if File.extname(@sam).eql? ".bam"
      $stderr.puts "Script cannot currently read bam."
      exit(1)
    else
      @fin = File.open(@sam, 'r')
    end

    @header = Array.new
    @header_read = false
  end

  def read
    return nil if @fin.eof?

    unless @header_read
      while (line = read_line).start_with? "@"
        @header << line.chomp
        @header_read = true
      end
      return parse_alignment(line)
    end
    return parse_alignment(@fin.readline)
  end


  :private

  def read_line
    return @fin.readline unless @fin.binmode?
    # no work, dunno don't care
    # chars = Array.new
    # b = nil
    # while (b == @fin.readbyte)
    #   c = b.chr
    #   chars << c
    #   break if c =~ /\n|\r|\f/
    # end
    #
    # return chars.join('')
  end

  def parse_alignment(line)
    return Alignment.new(line.chomp)
  end

end

class Alignment
  attr_reader :read_name, :flag, :ref_name, :read_pos, :mapq, :cigar, :mate_ref, :mate_pos, :tlen, :seq, :phred, :tags

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

    @mate_ref = @ref_name if @mate_ref.eql? "="
  end

  def is_same_chromosome?
    return (@mate_ref.eql? "=" or @mate_ref.eql? @ref_name)
  end

  def proper_pair?
    (@flag & 2) == 2
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

  def orientation
    return {:read => (read_reversed?) ? "F" : "R", :mate => (mate_reversed?) ? "F" : "R"}
  end

  def is_secondary?
    (@flag & 256) == 256
  end

end

class Bands

  def initialize(file)
    @chr_hash = Hash.new

    File.open(file, 'r').each_line do |line|
      line.chomp!

      next if line.start_with?'chr'

      (chr, band, pstart, pend)  = line.split("\t")[0..3]

      @chr_hash[chr] = Hash.new unless @chr_hash.has_key?chr

      @chr_hash[chr][band] = Range.new( pstart.to_i, pend.to_i )

    end

    def get_band(chr, loc)
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


# if ARGV.length <=0
#   $stderr.puts "Usage: #{$0} <output dir> <band text file>"
#   exit(1)
# end


band_file = ARGV[1]
#band_file = "/Users/sarah.killcoyne/workspace/IGCSA/R/alignment/band_genes.txt"
bands = Bands.new(band_file)


outdir = "#{ARGV[0]}/dist"
puts outdir


FileUtils.rmtree(outdir) if Dir.exists? outdir
FileUtils.mkpath(outdir)

File.open("#{outdir}/disc.reads", 'a') {|f|
  f.puts ["ref", "pos", "mate", "mate.pos"].join("\t")
}


count = 0

$stdin.each do |line|
  print "." if count%10000 == 0
  print "\n" if count%1000000 == 0

  next if line.start_with? "@"

  align = Alignment.new(line.chomp)

  unless align.nil?

    if align.read_paired? and !align.is_dup? and !align.proper_pair?

      if align.is_same_chromosome?
        ca = (bands.in_centromere?(align.ref_name, align.read_pos))? "centromere": "arm"

        File.open("#{outdir}/chr#{align.ref_name}.reads", 'a') {|f|
          f.puts [align.tlen.abs, ca].join("\t")
        }
      else
        File.open("#{outdir}/disc.reads", 'a') {|f|
          f.puts [align.ref_name, align.read_pos,  align.mate_ref, align.mate_pos].join("\t")
        }
      end

    end

  end

  count += 1
end
puts count

#puts YAML::dump algn
# puts algn.flag
# puts algn.proper_pair?
# puts algn.tags
# # puts algn.read_mapped?
# # puts algn.read_paired?
# # puts algn.mate_mapped?
# #puts algn.read_reversed?
#
# puts (algn.flag & 1)
#
# puts (613 & 4)
