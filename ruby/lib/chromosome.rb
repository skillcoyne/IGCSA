require_relative 'chromosome_fragment'

class Chromosome

  # type -> autosomal, sex
  attr_reader :chromosome, :type, :deletions, :duplications, :insertions, :inversions, :fragments, :breakpoints

  def altered?
    if (@deleted_bands.length > 0 or @duplicated_bands.length > 0 or @fragments.length > 0 or @insertions.length > 0)
      return true
    end
  false
  end

  def initialize(chr)
    @chromosome = chr.to_s
    (@chromosome.match(/\d+/))? (@type = "autosomal"): (@type = "sex")
    @fragments = []
    @deletions = []
    @duplications = []
    @inversion = []
    @insertions = []
    @breakpoints = []
  end

  def delete_band(band)
    @deletions.push(band)
    @breakpoints.push("#{@chromosome}#{band}")
  end

  def duplicate_fragment(frag)
    raise ArgumentError, "#{__method__} requires object of type 'ChromosomeFragment'" unless frag.kind_of?ChromosomeFragment
    @duplications.push(frag)
    @breakpoints.push(frag.start.to_s)
    @breakpoints.push(frag.end.to_s)
  end

  def isochromosome(duparm)

  end

  def add_inversion(frag)
    raise ArgumentError, "#{__method__} requires object of type 'ChromosomeFragment'" unless frag.kind_of?ChromosomeFragment
    @inversion.push(frag)
    @breakpoints.push(frag.start.to_s)
    @breakpoints.push(frag.end.to_s)
  end

  def add_insertion(fragment)
    raise ArgumentError, "#{__method__} requires object of type 'Band'" unless fragment.kind_of?Band
    @insertions.push(fragment)
    @breakpoints.push(fragment.to_s)
  end


  def add_fragment(from, to)
    frag = ChromosomeFragment.new(from, to)
    @fragments.push(frag)
    [from, to].each { |e| @breakpoints.push(e) }
  end


end