class EasySkyRecord
  attr_accessor :case, :diagnosis, :stage, :normal_chr, :abnormal_chr, :fragments, :frag_aggr

  def initialize(skycase)
    @case = skycase
    @normal_chr = {}
    @abnormal_chr = []
    @fragments = {}
  end

  def add_normal(chr, count)
    (@normal_chr.has_key?(chr)) ? (@normal_chr[chr] += count) : (@normal_chr[chr] = count)
  end

  def add_abnormal(chr, occurance)
    @abnormal_chr.push([chr, occurance])
  end

  def add_fragment(primary, parent, fragment)
    raise ArgumentError, "Fragment should be a 'ChromosomeFragment'." unless fragment.class.eql?('ChromosomeFragment')
    @fragments[primary].push( [parent, fragment] )
  end

end