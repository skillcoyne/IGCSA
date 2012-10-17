require_relative 'chromosome'

class DerivativeChromosome < Chromosome

  def initialize(chr)
    super(chr)

    @diploid_number = 1
  end

end