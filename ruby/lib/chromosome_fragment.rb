require_relative 'band'

class ChromosomeFragment

  attr_accessor :start, :end, :gene, :parent

  def initialize(*args)
    #def initialize(from, to)
    if args.size == 2
      raise ArgumentError, "Arguments should be 'Band'." unless (args[0].kind_of?(Band) and args[1].kind_of? Band)
      @start = args[0]
      @end = args[1]
    elsif args.size == 3
      @start = Band.new(args[0], args[1])
      @end = Band.new(args[0], args[2])
    else
      raise ArgumentError, "Incorrect number of arguments, expected 2 (from, to) or 3 (chromosome, from, to)"
    end
  end

  def add_gene(gene)
    @gene = gene
  end

  def as_string
    return "#{@start.to_s} --> #{@end.to_s}"
  end

end