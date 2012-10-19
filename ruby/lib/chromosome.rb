require_relative 'chromosome_fragment'

class Chromosome
  # type -> autosomal, sex
  attr_reader :chromosome, :type, :deleted_bands, :duplicated_bands


  class << self;
    :struct_var
    :ins
    :del
  end

  def initialize(chr)
    @chromosome = chr.to_s
    (@chromosome.match(/\d+/))? (@type = "autosomal"): (@type = "sex")
    @struct_var = []
    @deleted_bands = []
    @duplicated_bands = []

    @ins = {}
    @del = []
  end

  def gain
    @diploid_number += 1
    #puts "Chr#{@chromosome} gaim: #{@diploid_number}"
  end

  def loss
    @diploid_number -= 1
    #puts "Chr#{@chromosome} loss: #{@diploid_number}"
  end

  def delete_band(band)
    @deleted_bands.push(band)
    #puts "#{@chromosome} band deletion #{band}"
  end

  def duplicate_band(band)
    @duplicated_bands.push(band)
    #puts "#{@chromosome} duplicated band #{band}"
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
    #puts "Insert #{fragment} at #{@chromosome}#{band}"
  end

  def delete_fragment(range = [])
    @del.push(range)
    #puts "Delete #{range.join('-')} from #{@chromosome}"
  end

  def add_fragment(from, to)
    frag = ChromosomeFragment.new(from, to)
    @struct_var.push(frag)
    #puts "#{@chromosome} a add fragment #{from} - #{to}"
  end

end