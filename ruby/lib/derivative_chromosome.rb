require_relative 'chromosome'

class DerivativeChromosome < Chromosome

  attr_reader :translocations

  def initialize(chr)
    @translocations = []
    super(chr)
  end

  def add_translocation(bp1, bp2)
    @translocations.push(ChromosomeFragment.new(bp1, bp2))
  end

end