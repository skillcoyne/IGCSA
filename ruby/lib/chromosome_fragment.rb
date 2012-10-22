class ChromosomeFragment

  attr_accessor :from, :to, :gene, :parent

  def initialize(parent_chr, from_band, to_band)
    @parent = parent_chr
    @from = from_band
    @to = to_band
  end

  def add_gene(gene)
    @gene = gene
  end

  def as_string
    return "#{@from} --> #{@to}"
  end

end