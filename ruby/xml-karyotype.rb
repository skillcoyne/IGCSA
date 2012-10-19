require 'hpricot'
require 'yaml'
require_relative 'lib/sky_karyotype'

xml_dir = "/Users/skillcoyne/Data/sky-cgh/XML/"

Dir.foreach(xml_dir) do |entry|
  entry = "#{xml_dir}#{entry}"
  next if File.directory?(entry)
  puts "Reading #{entry}"
  xml = File.read(entry)
  doc = Hpricot.XML(xml)

  puts doc.search("//SkyCell/KaryotypeTerms")

  karyotype = doc.search("//SkyCellHeader_karyotype").inner_text
  puts karyotype
  #puts karyotype.split(",")
end

#xml = File.read('/Users/skillcoyne/Data/sky-cgh/XML/L.Stapleton_1.xml')
#
#doc = Hpricot.XML(xml)
#
#puts doc.search("//SkyCell/KaryotypeTerms")
#
#karyotype = doc.search("//SkyCellHeader_karyotype").inner_text
#
#puts karyotype.split(",")