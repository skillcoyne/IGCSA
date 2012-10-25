require_relative 'chromosome_fragment'

class Chromosome
  # type -> autosomal, sex
  attr_reader :chromosome, :type, :deletions, :duplications, :insertions, :inversions

  class << self;
    :struct_var
  end

  def altered?
    if (@deleted_bands.length > 0 or @duplicated_bands.length > 0 or @struct_var.length > 0 or @ins.length > 0)
      return true
    end
  false
  end

  def initialize(chr)
    @chromosome = chr.to_s
    (@chromosome.match(/\d+/))? (@type = "autosomal"): (@type = "sex")
    @struct_var = []
    @deletions = []
    @duplications = []
    @inversion = []
    @insertions = {}
  end

  def delete_band(band)
    @deletions.push(band)
    #puts "#{@chromosome} band deletion #{band}"
  end

  def duplicate_fragment(frag)
    @duplications.push(frag)
    #puts "#{@chromosome} duplicated band #{band}"
  end

  def get_fragments
    @struct_var
  end

  def isochromosome(duparm)

  end

  #def set_fragments(array)
  #  array.each do |e|
  #    add_fragment(e[0], e[1])
  #  end
  #end

  def add_inversion(fragment)
    @inversion.push(fragment)
  end

  def add_insertion(fragment, band)
    @insertions[band] = fragment
    #puts "Insert #{fragment} at #{@chromosome}#{band}"
  end

  def add_fragment(parent, from, to)
    frag = ChromosomeFragment.new(parent, from, to)
    @struct_var.push(frag)
    #puts "#{@chromosome} a add fragment #{from} - #{to}"
  end

end