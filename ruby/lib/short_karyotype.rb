require_relative 'variations'
# ISCN terminology
# Variations http://www.radford.edu/~rsheehy/cytogenetics/Cytogenetic_Nomeclature.html
# http://cgap.nci.nih.gov/Chromosomes/ISCNSymbols

# Utility class for reading the short version of a karyotype
class ShortKaryotype

  def ShortKaryotype.indecies_of(char, str)
    i = -1
    all = []
    while i = str.index(/#{char}/, i+1)
      all << i
    end
    all
  end

  def ShortKaryotype.index_abn(str)
    indecies = {}
    Variations.chromosome_abnormalities.each_key do |key|
      ShortKaryotype.indecies_of(Variations.abnormality_regex[key], str).each do |i|
        indecies[i] = key
      end
    end
    indecies
  end

  def ShortKaryotype.index_bands(str)
    indecies = {}
    Variations.band_description.each_key do |key|
      ShortKaryotype.indecies_of(Variations.band_regex[key], str).each do |i|
        indecies[i] = key
      end
    end
  end

  def ShortKaryotype.index_all(str)
    indecies = {}
    ShortKaryotype.all_keywords.each_key do |key|
      indecies[key] = ShortKaryotype.indecies_of(ShortKaryotype.all_regex[key], str)
    end
    indecies.delete_if { |k, v| v.empty? }
    return indecies
  end

  def ShortKaryotype.all_keywords
    all = Variations.chromosome_abnormalities.merge(Variations.band_description)
    return all
  end

  def ShortKaryotype.all_regex
    all = Variations.abnormality_regex.merge(Variations.band_regex)
    return all
  end


end