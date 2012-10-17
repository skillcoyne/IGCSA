class ChromosomeFragment

  attr_accessor :from, :to

  def initialize(from_band, to_band)
    @from = from_band
    @to = to_band
  end

  def as_string
    return "#{@from} --> #{@to}"
  end

end