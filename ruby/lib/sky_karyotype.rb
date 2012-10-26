require 'yaml'
require_relative 'logging'
require_relative 'chromosome'
require_relative 'derivative_chromosome'
require_relative 'chromosome_fragment'
require_relative 'karyotype_error'


class SkyKaryotype
  @@haploid = 23

  attr_reader :ploidy, :karyotype, :log, :normal_chr, :abnormal_chr


  def initialize(logger)
    @log = logger
    @abnormal_chr = []
    @normal_chr = {}

    (Array(1..23)).each { |c| @normal_chr[c.to_s] = 0 }
  end

  def breakpoints
    bp = []
    @abnormal_chr.each { |c| bp = bp|c.breakpoints }
    return bp.uniq
  end

  # Seen in several ways:
  # single number:  46
  # number with range: 46(45-49)
  # range: 65-71
  def calculate_ploidy(str)
    @log.info("#{__method__} #{str}")
    diploid = @@haploid*2
    triploid = @@haploid*3
    quadraploid = @@haploid*4

    # typically see di- tri- quad- if more than that it should be noted
    min = diploid
    max = diploid
    if str.match(/<\+(\d)n>/)
      @ploidy = 3
    elsif str.match(/(\d+-\d+)/) # num and range or just range
      (min, max) = $1.split(/-/).map { |e| e.to_i }
    elsif str.match(/^(\d+)/) # single num
      min = $1.to_i
      max = $1.to_i
    end

    if @ploidy.nil?
      case
        when (min.eql? diploid and max.eql? diploid)
          @log.info("Normal ploidy")
          @ploidy = 2
        when (min >= @@haploid and max <= diploid)
          log.info("Relatively normal ploidy #{str}")
          @ploidy = 2
        when (min >= @@haploid and max < quadraploid)
          @log.info("Triploid #{str}")
          @ploidy = 3
        when (min >= diploid and max >= quadraploid)
          @log.info("Quadraploid #{str}")
          @ploidy = 4
        else
          #@log.error("Failed to determine ploidy from #{str}")
          raise KaryotypeError, "Failed to determine ploidy for #{@karyotype}"
      end
    end
    @normal_chr.each_key { |c| @normal_chr[c] = @ploidy } # sex chromosomes handled separately
  end

  def determine_sex(str)
    @log.info("#{__method__} #{str}")

    unless str.match(/X|Y/)
      @log.error("Definition of gender incorrect (#{str}), fix parser: #{@karyotype}")
      return
    end

    # ploidy number makes no difference since this string will tell us how many or at least what the gender should be
    ['X', 'Y'].each { |c| @normal_chr[c] = 0 }

    sex_chr = str.match(/([X|Y]+)/).to_s.split(//)
    sex_chr.each { |c| @normal_chr[c] += 1 }

    # assume this was an XY karyotype that may have lost the Y, have only seen this in
    # severely affected karyotypes
    @normal_chr['Y'] += 1 if (sex_chr.length.eql?(1) and sex_chr[0].eql?('X'))
  end


  def parse(kt)
    @log.info("Parsing karyotype: #{kt}")
    @karyotype = kt
    @karyotype.split(",").each_with_index do |k, i|
      # strip cell numbers and whitespace
      k = k.gsub(/\[\d+\]/, "").gsub(/\s/, "")
      calculate_ploidy(k) if i == 0
      determine_sex(k) if i == 1
      parse_abnormalities(k) if i > 1
    end
  end

  # Look for whole chromosomes gained or lost in a karyotype
  # These are indicated with a +/-CHR  e.g. -5  or  +X
  def find_polyploidy(abnormality)
    @log.info("#{__method__} #{abnormality}")
    ## Add/subtract chromosome
    abnormality.chars.each_with_index do |c, i|
      if c.match(/^\+|-/) and abnormality[i+1].match(/\d+|X|Y/)
        chr = abnormality[i+1..abnormality.length]
        raise KaryotypeError, "#{abnormality} is not a known polyploidy or sex chromosome was not indicated in karyotype." unless @normal_chr.has_key?(chr)
        polychr = Chromosome.new(chr)
        (c.eql?('-')) ? (@normal_chr[chr] -= 1) : @normal_chr[chr] += 1
        parse_warning("find_polyploidy", abnormality) if abnormality.length.eql?(i+1)
      end
    end
  end

  def find_inversion(abnormality)
    @log.info("#{__method__} #{abnormality}")
    cbi = find_chr_bands(abnormality)
    bands = cbi[:band_i][:bands]
    frag = fragment_split(bands)
    dchr = Chromosome.new(cbi[:chr_i][:chr])
    dchr.add_inversion(ChromosomeFragment.new(cbi[:chr_i][:chr], frag[0], frag[1]))
    @normal_chr[dchr.chromosome] -= 1 unless abnormality.start_with?("+")
    @abnormal_chr.push(dchr)
  end


  def find_fragment(abnormality)
    @log.info("#{__method__} #{abnormality}")
    #chr_bands = find_chr_bands(abnormality)
    raise KaryotypeError, "Fragments currently not handled by chromosome object. -- #{abnormality} --"
  end

  def find_derivatives(abnormality)
    @log.info("#{__method__} #{abnormality}")

    chr_i = find_chr(abnormality)
    ## It is rare that multiple chromosomes are indicated, but primary derivative can be either so take first
    primary_chr = chr_i[:chr].split(/;|:/)[0]

    unless @normal_chr.has_key?(primary_chr)
      @log.warn("No such chromosome #{primary_chr} for #{abnormality}, skipping.")
      return
    end

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
            when (abn_type =~ /dup/ and cb[:band_i])
              fragments = fragment_split(cb[:band_i][:bands])
              derivative_chr.duplicate_fragment(ChromosomeFragment.new(cb[:chr_i][:chr], fragments[0], fragments[1]))
            else
              raise KaryotypeError, "Derivative for #{abnormality} currently unhandled"
          end
        else
          raise KaryotypeError, "#{chr} does not match #{primary_chr}, #{abn_type} is not possible. #{f}"
        end
      end
    end
    @normal_chr[derivative_chr.chromosome] -= 1 unless abnormality.start_with?("+")
    @abnormal_chr.push(derivative_chr)
  end

  def find_insertions(abnormality)
    @log.info("#{__method__} #{abnormality}")
    chr_band = find_chr_bands(abnormality)
    chrs = chr_band[:chr_i][:chr].split(/;|:/)
    bands = chr_band[:band_i][:bands].split(/;|:/)
    ins_chr = Chromosome.new(chrs[0])

    raise KaryotypeError, "Insertion syntax not handled: #{abnormality}" if (bands.length < 2 or chrs.length > 1)

    fragments = []
    if bands[1].match(/((q|p)\d+){2,}/) # two or more bands
      fragments = fragment_split(bands[1])
    else # one band
      fragments.push(bands[1])
    end
    # again not handled quite correctly, also won't handle more than 2 bands
    ins_chr.add_insertion(ChromosomeFragment.new(chrs[0], fragments[0], fragments[1]))

    @abnormal_chr.push(ins_chr)
    parse_warning("find_insertions", abnormality) unless abnormality.length.eql?(chr_band[:band_i][:index_e]+1)
  end

  def find_deletions(abnormality)
    @log.info("#{__method__} #{abnormality}")
    chr_band = find_chr_bands(abnormality)
    del_chr = Chromosome.new(chr_band[:chr_i][:chr])

    if chr_band[:band_i]
      bands = chr_band[:band_i][:bands]

      fragments = []
      if bands.match(/((q|p)\d+){2,}/) # two or more bands
        fragments = fragment_split(bands)
      else # one band
        fragments.push(bands)
      end
      fragments.each { |f| del_chr.delete_band(f) }
      @abnormal_chr.push(del_chr)
    else # no bands
      raise KaryotypeError, "Deletion has no bands specified: #{abnormality}"
    end
  end

  def find_duplications(abnormality)
    @log.info("#{__method__} #{abnormality}")
    # dup(19)(q13.3q13.1)
    chr_band = find_chr_bands(abnormality)
    if (chr_band[:chr_i].nil? or chr_band[:band_i].nil?)
      log.error("Duplication cannot be parsed from #{abnormality}")
      return
    end

    chr = chr_band[:chr_i][:chr]
    bands = chr_band[:band_i][:bands]

    dchr = Chromosome.new(chr)
    if bands.match(/([q|p]\d+){2,}/) # two or more bands
      fragment = fragment_split(bands)
      @log.warn("Duplication had more than 2 bands, only the first two handled: #{abnormality}") if fragment.length > 2
      dchr.duplicate_fragment(ChromosomeFragment.new(chr, fragment[0], fragment[1]))
    else # one band
      dchr.duplicate_fragment(ChromosomeFragment.new(chr, bands[1], "#{$1}ter"))
    end
    @abnormal_chr.push(dchr)
    @normal_chr[dchr.chromosome] -= 1 unless abnormality.start_with?("+")
  end

  def find_isochromosome(abnormality)
    @log.info("#{__method__} #{abnormality}")
    cb = find_chr_bands(abnormality)

    band = cb[:band_i][:bands]
    frags = fragment_split(band)
    if frags.length > 1
      @log.error("Isochromosome should not have multiple bands: #{abnormality}")
      return
    end

    # delete one arm and duplicate other
    derivative_chr = Chromosome.new(cb[:chr_i][:chr])
    #@normal_chr[derivative_chr.chromosome] -= 1 unless abnormality.start_with?("+")
    raise KaryotypeError, "Isochromosomes are not current handled by the chromosome object"
  end

### ---- PRIVATE METHODS ---- ###
  private

  def fragment_split(str)
    str.scan(/[q|p](\?)\d+/).map { |e| str.sub!(e[0], "") } # if fragment includes band number and ? strip ?: q?13
    fragments = str.scan(/([p|q][\d+|\?][\.\d]?)/).flatten! # allows for a fragment that may be unknown: q?
    return fragments
  end

  def find_chr_bands(str)
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
      band_e = str.length-1 if band_e.nil?
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
        when abn =~ /^\+?ins\(/ # insertion
          find_insertions(abn)
        when abn =~ /^\+?del\(/ # deletion
          find_deletions(abn)
        when abn =~ /^\+?dup\(/ # duplication
          find_duplications(abn)
        when abn =~ /^\+?frag\(/ # fragment
          find_fragment(abn)
        when abn =~ /^\+?der\(/ # derivative...this is a special case as it will contain other elements as well
          find_derivatives(abn)
        when abn =~ /^\+?inv\(/ # inversion
          find_inversion(abn)
        #when abn =~ /^\+?i\(/  # isochromosome, duplicate arm around centromere q10 or p10  (e.g. q10 means q arms are duplicated)
        #  find_isochromosome(abn)
        #when abn =~ /^\+?ider\(/ # isoderivative, an isochromosome derivative around the centromere p10 or q10
        #  find_ider(abn)
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
    chrs.each_with_index { |c, i| breaks[c] = bands[i] }

    # probably a cleaner way to do this but the basic rules to a translocation...
    # t(2;6)(q12;p12)t(1;6)(p22;q21) == 2pter-->2q12::6p12-->6q21::1p22-->1pter
    # the first fragment always starts from a terminal of an arm
    # the middle translocations are either breakpoint joins (2 different chromosomes) or fragments from the same chromosome
    # final fragment needs to end on the terminal of an arm
    last_chr = derivative_chr.chromosome # not actually last chromosome, primary derivative chr
    if last_break
      derivative_chr.add_translocation(last_break, Band.new(last_break.chromosome, breaks[frag[:last_chr]]))
      chrs.delete_if { |e| e.eql?(frag[:last_chr]) }
      last_break = Band.new(frag[:last_chr], breaks[frag[:last_chr]])
    end

    chrs.each_with_index do |c, i|
      from = last_break
      from = Band.new(c, "#{bands[i].match(/(q|p)/)}ter") if last_break.nil?
      derivative_chr.add_translocation(from, Band.new(c, bands[i]))

      last_chr = c
      last_break = Band.new(c, bands[i])
    end

    if chrs.length.eql?(1)
      derivative_chr.add_translocation(Band.new(chrs[0], bands[0]), Band.new(chrs[0], "#{bands[0].match(/q|p/)}ter"))
    end

    return {:dchr => derivative_chr, :last_chr => last_chr, :last_break => last_break}
  end

### ------- End private --------- ###

end

