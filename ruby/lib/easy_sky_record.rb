require 'yaml'

class EasySkyRecord
  attr_accessor :case, :diagnosis, :stage, :normal_chr, :fragments, :breakpoints, :genes, :karyotype

  def initialize()
    @case = "Unknown"
    @normal_chr = {}
    @fragments = {}
    @breakpoints = []
    @genes = {}
  end

  def add_normal(chr, count)
    (@normal_chr.has_key?(chr)) ? (@normal_chr[chr] += count) : (@normal_chr[chr] = count)
  end

  def add_fragment(primary, fragment)
    raise ArgumentError, "Fragment should be a 'ChromosomeFragment'." unless fragment.kind_of? ChromosomeFragment
    @fragments[primary] = [] unless @fragments.has_key? primary
    @fragments[primary].push(fragment)

    @breakpoints.push("#{fragment.parent}#{fragment.from}") if valid_breakpoint(fragment.from)
    @breakpoints.push("#{fragment.parent}#{fragment.to}") if valid_breakpoint(fragment.to)

    if fragment.gene
      @genes[fragment.gene] = [] unless @genes.has_key?(fragment.gene)
      @genes[fragment.gene].push(fragment.parent)
    end
  end

  def genes
    @genes.each_pair do |k, v|
      if v.kind_of? Array
        v.uniq!
        @genes[k] = v.join(", ")
      end
    end
    @genes
  end

  def breakpoints
    @breakpoints.uniq!
    @breakpoints
  end

  private
  def valid_breakpoint(bp)
    return true unless bp.nil? or bp.eql?('?') or bp.eql?('0') or bp.match(/[q|p]ter/)
    false
  end

end