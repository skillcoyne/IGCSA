require 'hpricot'

xml = File.read('/Users/skillcoyne/Data/sky-cgh/XML/L.Stapleton_1.xml')

doc = Hpricot.XML(xml)

puts doc.search("//SkyCell/KaryotypeTerms")

karyotype = doc.search("//SkyCellHeader_karyotype").inner_text

puts karyotype.split(",")