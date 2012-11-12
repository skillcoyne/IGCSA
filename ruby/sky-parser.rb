require 'yaml'
require 'logger'
require_relative 'lib/sky_karyotype'


if ARGV.length <= 0
  print "Root directory for ESI/cam karyotypes required"
  exit
end

dir = ARGV[0]
logger = Logger.new(File.new("#{dir}/log.txt", 'w'))

#esidir = "#{dir}/ESI/karyotype"
#camdir = "#{dir}/path.cam.ac.uk/Lung/"

events = {}

Dir.foreach(dir) do |entry|
  file = "#{dir}/#{entry}"
  next if entry.start_with?(".")
  next if File.directory?(file)

  puts "Reading #{entry}..."

  unless (File.basename(entry).match(/\.karyotype/) or File.basename(entry).match(/\.kt/))
    puts "#{entry} is not a karyotype file"
    next
  end

  kts = 0
  File.open(file, 'r').each_line do |line|
    line.chomp
    next if line.length <= 0
    next if line.match(/mouse/)
    next if line.match(/Case/) # column names
    karyotype = line.split(/\t/)[-1].gsub!(/\s/, "")

    aberrations = karyotype.split(",")

    aberrations.each do |a|
      events[a] = 0 unless events.has_key? a
      events[a] += 1
    end

    kts+=1
    #sk = SkyKaryotype.new(logger)
    #sk.parse(karyotype)
    #
    #puts sk.breakpoints

    #sk.abnormal_chr.each do |chr|
    #  sk.normal_chr[chr.chromosome] += 1
    #  next if chr.kind_of? DerivativeChromosome
    #  puts "Derivative #{chr.chromosome}:"
    #  puts "\tinsertions:" + (YAML::dump chr.insertions)
    #  puts "\tdeletions:" + (YAML::dump chr.deletions)
    #  puts "\tduplications:"
    #  chr.duplications.each do |dup|
    #    puts "\t\t#{dup.as_string}"
    #  end
    #
    #  puts "\tfragments:"
    #  chr.fragments.each do |frag|
    #    puts "\t\t#{frag.as_string}"
    #  end
    #  if chr.kind_of? DerivativeChromosome
    #    puts "\ttranslocations:"
    #    chr.translocations.each do |t|
    #      puts "\t\t#{t.as_string}"
    #    end
    #  end
    #end
  end
  puts "Karyotypes: #{kts}"
end

puts events.keys.length

#sk.normal_chr.each_pair {|k,v| puts "#{k} = #{v}"}

