require 'yaml'
require_relative 'logging'
require_relative 'chromosome'
require_relative 'derivative_chromosome'
require_relative 'chromosome_fragment'
require_relative 'karyotype_error'


class SkyKaryotype

  attr_reader :modal, :karyotype, :log


  def initialize(logger)
    @log = logger
    @karyotype = {}
    (Array(1..23)).each { |c| @karyotype[c.to_s] = [Chromosome.new(c.to_s), Chromosome.new(c.to_s)] }
  end

  def parse(karyotype)
    karyotype.split(",").each_with_index do |k, i|
      # strip cell numbers
      k = k.gsub(/\[\d+\]/, "")
      @modal = k if i == 0
      if i == 1
        unless k.match(/X|Y/)
          @log.error("Definition of gender incorrect, fix parser #{karyotype}")
          return
        end

        sex_chr = k.split(//)

        sex_chr.each { |c| @karyotype[c.to_s] = [Chromosome.new(c)] }
        # assume this was an XY karyotype that may have lost the Y, have only seen this in
        # severely affected karyotypes
        @karyotype['Y'] = [Chromosome.new('Y')] if (sex_chr.length.eql?(1) and sex_chr[0].eql?('X'))
      end
      parse_abnormalities(k) if i > 1
    end
  end

  # Look for whole chromosomes gained or lost in a karyotype
  # These are indicated with a +/-CHR  e.g. -5  or  +X
  def find_polyploidy(abnormality)
    ## Add/subtract chromosome
    abnormality.chars.each_with_index do |c, i|
      if c.match(/^\+|-/) and abnormality[i+1].match(/\d+|X|Y/)
        chr = abnormality[i+1..abnormality.length]
        raise KaryotypeError, "#{abnormality} is not a known polyploidy or sex chromosome was not indicated in karyotype." unless @karyotype.has_key?(chr)
        polychr = Chromosome.new(chr)
        (c.eql?('-')) ? (@karyotype[chr].delete_at(-1)) : (@karyotype[chr].push(Chromosome.new(chr)))
        parse_warning("find_polyploidy", abnormality) if abnormality.length.eql?(i+1)
      end
    end
  end

  def find_inversion(abnormality)
    cbi = find_chr_bands(abnormality)
    bands = cbi[:band_i][:bands]
    fragment = fragment_split(bands, cbi[:chr_i][:chr])
  end


  def find_fragment(abnormality)
    #chr_bands = find_chr_bands(abnormality)
    raise KaryotypeError, "Fragments currently not handled by chromosome object. -- #{abnormality} --"
  end

  def find_derivatives(abnormality)
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
        derivative_chr = t_frags[:dchr] unless t_frags.nil?
        last_fragment = t_frags[:last_break] unless t_frags.nil?
      else
        if chr.eql? primary_chr
          case
            when abn_type =~ /del/
              cb[:band_i][:bands].split(/:|;/).each { |e| derivative_chr.delete_band(e) } if cb[:band_i]
            when abn_type =~ /dup/
              cb[:band_i][:bands].split(/:|;/).each { |e| derivative_chr.duplicate_band(e) } if cb[:band_i]
            else
              raise KaryotypeError, "Derivative for #{abnormality} currently unhandled"
          end
        else
          raise KaryotypeError, "#{chr} does not match #{primary_chr}, #{abn_type} is not possible. #{f}"
        end
      end
    end
    raise KaryotypeError, "#{primary_chr} is not defined in the karyotype." unless @karyotype.has_key?(primary_chr)
    @karyotype[primary_chr].push(derivative_chr)
  end


  def find_insertions(abnormality)
    chr_band = find_chr_bands(abnormality)
    chrs = chr_band[:chr_i][:chr].split(/;|:/)
    bands = chr_band[:band_i][:bands].split(/;|:/)
    ins_chr = Chromosome.new(chrs[0])

    raise KaryotypeError, "Insertion needs a breakpoint currently: #{abnormality}" if bands.length < 2

    fragment = []
    if bands[1].match(/((q|p)\d+){2,}/) # two or more bands
      fragment = fragment_split(bands[1], chrs[1])
    else # one band
      fragment.push("#{chrs[1]}#{bands[1]}")
    end
    ins_chr.add_insertion(fragment.join('-'), bands[0])
    @karyotype[chrs[0]].push(ins_chr)
    parse_warning("find_insertions", abnormality) unless abnormality.length.eql?(chr_band[:band_i][:index_e]+1)
  end

  def find_deletions(abnormality)
    chr_band = find_chr_bands(abnormality)
    del_chr = Chromosome.new(chr_band[:chr_i][:chr])

    if chr_band[:band_i]
      bands = chr_band[:band_i][:bands]

      fragment = []
      if bands.match(/((q|p)\d+){2,}/) # two or more bands
        fragment = fragment_split(bands, "")
      else # one band
        fragment.push(bands)
      end
      del_chr.delete_fragment(fragment)
      @karyotype[chr_band[:chr_i][:chr]].push(del_chr)
    else # no bands
      raise KaryotypeError, "Deletion has no bands specified: #{abnormality}"
    end
  end

  def find_duplications(abnormality)
    # dup(19)(q13.3q13.1)
    chr_band = find_chr_bands(abnormality)
    chr = chr_band[:chr_i][:chr]

    dup_chr = Chromosome.new(chr)

    bands = chr_band[:band_i][:bands].split(/;|:/)

    fragment = []
    if bands[1].match(/((q|p)\d+){2,}/) # two or more bands
      fragment = fragment_split(bands[1], "")
    else # one band
      fragment.push(bands[1])
    end

    #dup_chr.add_duplication(???)
    #@karyotype[chr].push(dup_chr)
    raise KaryotypeError, "Duplications not currently handled by chromosome object. -- #{abnormality} --"
  end

### ---- PRIVATE METHODS ---- ###
  private

  def fragment_split(str, chr)
    fragments = str.scan(/([p|q]\d+[\.\d]?)/).flatten!
    fragments.map! { |e| e.to_s.prepend(chr) }
    return fragments
  end

  def find_chr_bands(str)
    #puts str
    chr = find_chr(str)
    bands = find_bands(str, chr)
    return {:chr_i => chr, :band_i => bands}
  end

  def find_chr(str)
    chr_s = str.index(/\(/, 0)
    chr_e = str.index(/\)/, chr_s)
    chr = str[chr_s+1..chr_e-1]
    raise KaryotypeError, "No chromosome parsed from #{str}." unless chr.match(/\d+|X|Y/)
    return {:index_s => chr_s, :index_e => chr_e, :chr => chr}
  end

  def find_bands(str, chr = {})
    raise KaryotypeError, "No bands defined in #{str}" if str.length.eql?(chr[:index_e]+1)
    ei = str.index(/\(/, chr[:index_e])
    if str.match(/(q|p)(\d+|\?)/) and str[ei-1..ei].eql?(")(") # has bands and is not a translocation
      band_s = str.index(/\(/, chr[:index_e])
      band_e = str.index(/\)/, band_s)
      return {:index_s => band_s, :index_e => band_e, :bands => str[band_s+1..band_e-1]}
    end
  end

  def parse_warning(method, str)
    raise KaryotypeError, "#{method}: Abnormality needs to be further parsed. #{str}"
  end

  def parse_abnormalities(abn)
    begin
      case
        when abn =~ /(\+|-)(\d+|X|Y)/ # polyploidy
          find_polyploidy(abn)
        when abn =~ /^\W?ins\(/ # insertion
          find_insertions(abn)
        when abn =~ /^\W?del\(/ # deletion
          find_deletions(abn)
        #when abn =~ /^\W?dup\(/ # duplication
        #  find_duplications(abn)
        when abn =~ /^\W?frag\(/ # fragment
          find_fragment(abn)
        when abn =~ /^\W?der\(/ # derivative...this is a special case as it will contain other elements as well
          find_derivatives(abn)
        #when abn =~ /^\W?inv\(/
        #  find_inversion(abn)
        else
          @log.error("No method for parsing #{abn}")
      end
    rescue KaryotypeError => error
      @log.error("Cannot handle abnormality: '#{abn}'. #{error.message}")
    end
  end

  def parse_translocation(derivative_chr, abnormality, frag)
    (frag.nil?) ? (last_break = nil) : (last_break = frag[:last_break])

    cbi = find_chr_bands(abnormality)
    # in cases where the bands do not exist it is due to a separate issue of "unknown" translocations?
    raise KaryotypeError, "Translocation in #{abnormality} missing band information." if cbi[:band_i].nil?

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

### ------- End private --------- ###

end

