# Based on Mitelman database which uses ISCN
# Variations http://www.radford.edu/~rsheehy/cytogenetics/Cytogenetic_Nomeclature.html
# http://cgap.nci.nih.gov/Chromosomes/ISCNSymbols

class Variations

  def Variations.chromosome_abnormalities
    {
        :deletion => 'del', :duplication => 'dup', :insertion => 'ins', :inversion => 'inv', :translocation => 't',
        :quadruplication => 'qpd', :frame_shift => 'fs', :gene_conversion => 'con',

        :isochromosome => 'i', :maternal => 'mat', :paternal => 'pat', :ring => 'r',
        :fission_at_centromere => 'fis', :marker => 'mar', :tricentric => 'trc',
        :derivative => 'der', :dicentric => 'dic', :terminus => 'ter', :trisomy => 'tri',

        :centromere => 'cen', :fragile => 'fra', :reciprocal => 'rcp', :robertsonian => 'rob',

        :gain => '+', :loss => '-', :multiple => 'x', #:intervals => '**',
    }
  end

  def Variations.abnormality_regex
    regex = {}
    Variations.chromosome_abnormalities.each_pair do |k, v|
      regex[k.to_sym] = "(^|\\W)#{v}\\W"
    end

    # special cases
    regex[:t] = "t\\("
    regex[:der] = "der\\("
    regex[:gain] = "^\\+(\\d|X|Y|der)"
    regex[:loss] = "^-(\\d|X|Y)"
    regex[:multiple] = "x\\d"
    regex[:intervals] = "\\*\\*"

    regex
  end

  # ':' separates reference sequence and variant description
  # ';' separates changes in one allele or between two alleles
  def Variations.band_description
    {
        :short_arm => 'p', :long_arm => 'q',
        #:structural_breakpoints => ';',
        #:break => ':', :break_join => '::', :mosaic => '/',
        #:aa_substitution => '>',
        :uncertain => '?'
    }
  end

  # these are all special cases
  def Variations.band_regex
    regex = {}
    regex[:short_arm] = "(\\(|;|:)p"
    regex[:long_arm] = "(\\(|;|:)q"
    #regex[:mosaic] = "\\/"
    regex[:uncertain] = "\\?"
    #regex[:break] = ":\w"
    #regex[:aa_substitution] = ">\w"
    #regex[:break_join] = band_description[:break_join]
    #regex[:structural_breakpoints] = band_description[:structural_breakpoints]
    return regex
  end


  def Variations.all
    all = Hash.new
    all = all.merge(chromosome_abnormalities)
    all = all.merge(band_description)
  end




end