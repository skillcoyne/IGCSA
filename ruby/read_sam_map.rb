#!/usr/bin/env ruby


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

    return tuples.join(",")
  end

  def phred_score
    return(@phred.split("").inject(0){|sum, e| sum + (e.ord - 33)  })
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
    str = (read_reversed?)? "F": "R"
    str += (mate_reversed?)? "F": "R"
    return str
    #return {:read => (read_reversed?) ? "F" : "R", :mate => (mate_reversed?) ? "F" : "R"}
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

$stdin.each do |line|
  next if line.start_with? "@"
  line.chomp!

  align = Alignment.new(line.chomp)
  if ( align.mapped? and align.mate_mapped? and align.tlen.abs > 0)
    puts align.tlen.abs
    #puts [align.read_name, align.read_pos, align.mate_pos, align.tlen.abs, align.phred_score, align.mapq, align.cigar_to_s, align.orientation].join("\t")
  end
end