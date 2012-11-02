class Band

  attr_reader :chromosome, :band

  def initialize(chr, band)
    raise ArgumentError, "Chromomsome and Band are required arguments." unless (chr and band)
    @chromosome = chr
    band.gsub!(/\(|\)/, "")
    @band = band
  end

  def to_s
    return "#{@chromosome}#{@band}"
  end

end