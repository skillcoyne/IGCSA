require_relative 'variations'
require_relative 'chromosome_fragment'

class Chromosome
  # type -> autosomal, sex
  attr_reader :chromosome

  class << self;
    :type
    :diploid_number
    :deleted_bands
    :struct_var
    :ins
    :del
  end

  def initialize(chr)
    @chromosome = chr.to_s
    (@chromosome.match(/\d+/))? (@type = "autosomal"): (@type = "sex")
    @struct_var = []
    @deleted_bands = []

    @ins = {}
    @del = []

    @diploid_number = 2
    @diploid_number = 1 if @chromosome.eql?'Y'
  end

  def gain
    @diploid_number += 1
    puts "Chr#{@chromosome} gaim: #{@diploid_number}"
  end

  def loss
    @diploid_number -= 1
    puts "Chr#{@chromosome} loss: #{@diploid_number}"
  end

  def delete_band(band)
    @deleted_bands.push(band)
    puts "Band deletion #{band}"
  end

  def get_fragments
    @struct_var
  end

  def set_fragments(array)
    array.each do |e|
      add_fragment(e[0], e[1])
    end
  end

  def add_insertion(fragment, band)
    @ins[band] = fragment
    puts "Insert #{fragment} at #{@chromosome}#{band}"
  end

  def delete_fragment(range = [])
    @del.push(range)
    puts "Delete #{range.join('-')} from #{@chromosome}"
  end

  def add_fragment(from, to)
    frag = ChromosomeFragment.new(from, to)
    @struct_var.push(frag)
  end




end