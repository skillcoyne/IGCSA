require 'yaml'
require_relative 'chromosome'
require_relative 'derivative_chromosome'
require_relative 'chromosome_fragment'
require_relative 'variations'
require_relative 'cytogenetic_variation'


class SkyKaryotype
  attr_accessor :modal, :gender, :chromosomes

  def fragment_split(str, chr)
    fragments = []
    frags = str.split(/q|p/).delete_if { |c| c.empty? }
    frags.each do |frag|
      str.match(/(q|p)#{frag}/)
      fragments.push("#{chr}#{$1}#{frag}")
    end
    fragments
  end

  def find_chr_bands(str)
    chr = find_chr(str)
    bands = find_bands(str, chr)
    return {:chr_i => chr, :band_i => bands}
  end

  def find_chr(str)
    chr_s = str.index(/\(/, 0)
    chr_e = str.index(/\)/, chr_s)
    return {:index_s => chr_s, :index_e => chr_e, :chr => str[chr_s+1..chr_e-1]}
  end

  def find_bands(str, chr = {})
    ei = str.index(/\(/, chr[:index_e])
    if str.match(/(q|p)(\d+|\?)/) and str[ei-1..ei].eql?(")(") # has bands and is not a translocation
      band_s = str.index(/\(/, chr[:index_e])
      band_e = str.index(/\)/, band_s)
      return {:index_s => band_s, :index_e => band_e, :bands => str[band_s+1..band_e-1]}
    end
  end


  def parse_warning(method, str)
    warn "#{method}: Abnormality needs to be further parsed. #{str}"
  end

  def initialize
    @chromosomes = []
  end

  def parse(karyotype)
    karyotype.split(",").each_with_index do |k, i|
      @modal = k if i == 0
      @gender = k if i == 1

      if i > 1
        # strip cell numbers
        k = k.gsub(/\[\d+\]/, "")
        parse_abnormalities(k)
      end
    end
  end

  def parse_abnormalities(abn)
    case
      when abn =~ /(\+|-)(\d+|X|Y)/ # polyploidy
        find_polyploidy(abn)
      when abn =~ /^\W?ins\(/ # insertion
        find_insertions(abn)
      when abn =~ /^\W?del\(/ # deletion
        find_deletions(abn)
      when abn =~ /^\W?dup\(/ # duplication
        find_duplications(abn)
      when abn =~ /^\W?frag\(/ # fragment
        find_fragment(abn)
      when abn =~ /^\W?der\(/ # derivative...this is a special case as it will contain other elements as well
        find_derivatives(abn)
      else
        puts abn
    end
  end

  def find_polyploidy(abnormality)
    puts "POLYPLOIDY #{abnormality}"
    ## Add/subtract chromosome
    abnormality.chars.each_with_index do |c, i|
      if c.match(/^\+|-/) and abnormality[i+1].match(/\d+|X|Y/)
        chr = abnormality[i+1..abnormality.length]
        polychr = Chromosome.new(chr)
        (c.eql?('-')) ? (@chromosomes.push(polychr.loss)) : (@chromosomes.push(polychr.gain))
        parse_warning("find_polyploidy", abnormality) if abnormality.length.eql?(i+1)
      end
    end
  end

  def find_fragment(abnormality)
    puts "FRAGMENT #{abnormality}"
    chr_bands = find_chr_bands(abnormality)

    warn "Fragments currently not handled by chromosome object. -- #{abnormality} --"
  end

  def find_derivatives(abnormality)
    puts "DERIVATIVES #{abnormality}"
    chr_i = find_chr(abnormality)
    primary_chr = chr_i[:chr]
    derivative_chr = DerivativeChromosome.new(primary_chr)

    # translocation, dupilication, insertion, etc
    der_abn = abnormality[chr_i[:index_e]+1..abnormality.length]

    # separate different abnormalities within the derivative chromosome and clean it up to make it parseable
    fragments = der_abn.scan(/([^\(\)]+\(([^\(\)]|\)\()*\))/).collect { |a| a[0] }

    last_fragment = nil
    t_frags = nil
    fragments.each_with_index do |f, i|
      abn_type = f[0..f.index(/\(/)-1]
      cb = find_chr_bands(f)
      chr = cb[:chr_i][:chr]

      if abn_type.eql? 't'
        last_fragment = t_frags if t_frags
        t_frags = parse_translocation(derivative_chr, f, last_fragment)
        last_fragment = t_frags[:last_break]
      else
        if chr.eql? primary_chr
          puts "TODO:  SAME"
        else
          warn "#{chr} does not match #{primary_chr}, #{abn_type} is not possible. #{f}"
        end
      end

    end
    @chromosomes.push(derivative_chr)
  end

  def parse_translocation(derivative_chr, abnormality, frag)
    puts "TRANSLOCATION #{abnormality}"
    (frag.nil?) ? (last_break = nil) : (last_break = frag[:last_break])

    cbi = find_chr_bands(abnormality)
    chrs = cbi[:chr_i][:chr].split(/;|:/)
    bands = cbi[:band_i][:bands].split(/;|:/)
    breaks = Hash.new
    chrs.each_with_index do |c, i|
      breaks[c] = bands[i]
    end

    # probably a cleaner way to do this but the basic rules to a translocation...
    # t(2;6)(q12;p12)t(1;6)(p22;q21) == 2pter-->2q12::6p12-->6q21::1p22-->1pter
    # the first fragment always starts from a terminal of an arm
    # the middle translocations are either breakpoint joins (2 different chromosomes) or fragments from the same chromosome
    # final fragment needs to end on the terminal of an arm

    last_chr = derivative_chr.chromosome # not actually last chromosome, primary derivative chr
    if last_break
      derivative_chr.add_fragment(last_break, "#{frag[:last_chr]}#{breaks[frag[:last_chr]]}")
      chrs.delete_if { |e| e.eql?(frag[:last_chr]) }
      last_break = "#{frag[:last_chr]}#{breaks[frag[:last_chr]]}"
    end

    chrs.each_with_index do |c, i|
      from = last_break
      from = "#{c}#{bands[i].match(/(q|p)/)}ter" if last_break.nil?
      derivative_chr.add_fragment("#{from}", "#{c}#{bands[i]}")
      last_chr = c
      last_break = "#{c}#{bands[i]}"
    end

    if chrs.length.eql?(1)
      arm = "#{bands[0].match(/q|p/)}ter"
      derivative_chr.add_fragment("#{chrs[0]}#{bands[0]}", "#{chrs[0]}#{arm}")
    end

    return {:dchr => derivative_chr, :last_chr => last_chr, :last_break => last_break}
  end

  def find_insertions(abnormality)
    puts "INSERTIONS #{abnormality}"

    chr_band = find_chr_bands(abnormality)

    chrs = chr_band[:chr_i][:chr].split(/;|:/)
    bands = chr_band[:band_i][:bands].split(/;|:/)

    fragment = []
    if bands[1].match(/((q|p)\d+){2,}/) # two or more bands
      fragment = fragment_split(bands[1], chrs[1])
    else # one band
      fragment.push("#{chrs[1]}#{bands[1]}")
    end
    @chromosomes.push(Chromosome.new(chrs[0]).add_insertion(fragment.join('-'), bands[0]))
    parse_warning("find_insertions", abnormality) unless abnormality.length.eql?(chr_band[:band_i][:index_e]+1)
  end

  def find_deletions(abnormality)
    puts "DELETIONS #{abnormality}"
    chr_band = find_chr_bands(abnormality)

    if chr_band[:band_i]
      bands = chr_band[:band_i][:bands]

      fragment = []
      if bands.match(/((q|p)\d+){2,}/) # two or more bands
        puts bands
        fragment = fragment_split(bands, "")
      else # one band
        fragment.push(bands)
      end
      @chromosomes.push(Chromosome.new(chr_band[:chr_i][:chr]).delete_fragment(fragment))
    else # no bands
      warn "Deletion has no bands specified: #{abnormality}"
    end
  end

  def find_duplications(abnormality)
    # dup(19)(q13.3q13.1)
    puts "DUPLICATIONS #{abnormality}"

    chr_band = find_chr_bands(abnormality)
    chr = chr_band[:chr_i][:chr]

    bands = chr_band[:band_i][:bands].split(/;|:/)

    fragment = []
    if bands[1].match(/((q|p)\d+){2,}/) # two or more bands
      fragment = fragment_split(bands[1], "")
    else # one band
      fragment.push(bands[1])
    end

    #@chromosomes[chr].add_duplication(fragment.join('-')) ?????
    parse_warning("find_duplications", abnormality) unless abnormality.length.eql?(chr_band[:band_i][:index_e]+1)
    warn "Duplications not currently handled by chromosome object. -- #{abnormality} --"
  end


end

