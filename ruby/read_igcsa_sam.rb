require 'yaml'
require 'fileutils'


class Alignment
  attr_reader :read_name, :flag, :ref_name, :read_pos, :mapq, :cigar, :mate_ref, :mate_pos, :tlen, :seq, :phred, :tags, :cigar_totals

  def initialize(line)
    if line.split("\t").length < 11
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

  def cigar_length
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
    return(@phred.split("").inject(0) { |sum, e| sum + (e.ord - 33) })
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

  def orientation_to_s
    return "#{orientation[:read]}:#{orientation[:mate]}"
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


class Array
  def sum
    inject(0.0) { |result, el| result + el }
  end

  def mean
    sum / size
  end

  def sample_variance
    m = self.mean
    sum = self.inject(0){|accum, i| accum +(i-m)**2 }
    sum/(self.length - 1).to_f
  end

  def standard_deviation
    return Math.sqrt(self.sample_variance)
  end

end



distances = Array.new



count = 0
$stdin.each do |line|

  next if line.start_with? "@"

  align = Alignment.new(line.chomp)

  unless align.nil?
    next if align.failed? or align.is_dup? or !align.mapped?

    # filter reads
    if align.proper_pair? and
      distances << align.tlen.abs
    end
  end
  count += 1

  break if count >= 100000
end

puts distances
puts "Total #{count} reads"
puts "Mean: #{distances.mean} SD: #{distances.standard_deviation}"
puts "Range: #{distances.min} - #{distances.max}"



