class Bands

  attr_reader :chr_hash

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