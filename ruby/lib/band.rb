class Band

  attr_reader :chromosome, :band

  def initialize(chr, band)
    @chromosome = chr
    @band = band
  end

  def to_s
    return "#{@chromosome}#{@band}"
  end

end