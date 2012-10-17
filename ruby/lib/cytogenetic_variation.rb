require 'yaml'
require_relative 'variations'
require_relative 'short_karyotype'
require_relative 'chromosome'

class CytogeneticVariation
  class << self;
    attr_accessor :modal, :karyotype_str, :chromosomes, :karyotype_indecies, :gender
  end


  def initialize(karyotype_string)
    @karyotype_str = karyotype_string.gsub(" ", "")

    @chromosomes = Hash.new
    (Array(1..23)|['X', 'Y']).each do |c|
      @chromosomes[c] = Chromosome.new(c)
    end

    parse()
  end

  def chromosomes
    @chromosomes
  end

  def modal
    @modal
  end


  def parse
    puts @karyotype_str
    @karyotype_indecies = ShortKaryotype.index_abn(@karyotype_str)


    # separate each variation and index/handle each string
    primary_chr = nil
    @karyotype_str.split(",").each_with_index do |chr_alterations, i|
      if i <= 0
        @modal = chr_alterations
        next
      end
      if i.equal? 1 # Gender karyotype
        (chr_alterations.eql? 'XY') ? (@gender = 'male') : (@gender = 'female')
        next
      end

      puts "** #{chr_alterations} **"


      all_indecies = ShortKaryotype.index_abn(chr_alterations)
      puts all_indecies
      all_sorted = all_indecies.keys.sort
      puts all_sorted
      parans = ShortKaryotype.indecies_of("\\(", chr_alterations)
      parans |= ShortKaryotype.indecies_of"\\)", chr_alterations
      puts parans.sort

      chr_alterations.chars.each_with_index do |c,i|
        puts "#{i} : #{c}"
      end



      #next if indecies.size <= 0 # No abnormalities
      #
      #abn_index = indecies.keys.sort
      #abn_index.each_with_index do |index, j|
      #  (abn_index.length.equal?(j+1)) ?
      #      (pair = chr_alterations[index..chr_alterations.length]) :
      #      (pair = chr_alterations[index..abn_index[j+1]-1])
      #
      #  pairs = pair.split(/\)|\(/).reject{ |e| e.empty? }
      #  puts "-- #{pair} --> #{pairs}"
      #
      #  last_chr_event = nil; current_event = nil;
      #  pairs.each_with_index do |item, i|
      #    event = Variations.chromosome_abnormalities.invert[item].to_s
      #    # derivative just tells us where the primary chromosome should be in the karyotype map
      #    if current_event.eql?'derivative' or event.eql?'derivative'
      #      current_event = event unless event.nil?
      #      next
      #    end
      #    puts "#{event} :#{current_event}"
      #
      #    abn = item.split(/;|:/)
      #    puts abn
      #    case
      #      when current_event =~ /translocation/
      #        bands = pairs[i+1].split(/;|:/)
      #        #@chromosomes[]
      #        puts bands
      #    end
      #
      #
      #    current_event = event unless event.nil?
      #  end
      #
      #
      #    #for i in 1..pair.length
      #    #
      #    #
      #    #end
      #
      #
      #end


#      indecies.keys.sort.each do |i|
#        keyword = ShortKaryotype.all_keywords[indecies[i]]
#        puts "--#{keyword} : #{i}--"
#        #if i <= 1 # primary chromosome
#        #  primary_chr = chr_alterations.match(/^(\+|-|x)?#{keyword}\((\d+|X|Y)?\)/).to_s.match(/\((\d+|X|Y)?\)/).to_s
#        #  primary_chr = $1
##        else # structural alterations
#
#          puts chr_alterations[i..chr_alterations.length]
#          # walk the string
#          first_chr; second_chr
#          for j in i..chr_alterations.length
#            puts chr_alterations[j]
#            case
#              when chr_alterations[j..j+1].match(/\(\w/)
#                puts "#{j} Primary chr in abnormatlity"
#
#              when chr_alterations[j..j+1].match(/;|:\w/)
#                puts "#{j} Secondary chr in abnormatlity"
#              when chr_alterations[j..j+1].match(/\)\(/)
#                puts "#{j} Band description"
#            end
#
#          end
#
#          break
##        end
#      end

#  chr = k.match(/^\w+/)
#  @chromosomes[chr] = Chromosome.new(chr)
#
#  puts chr
#  break
#end

    end


  end


end