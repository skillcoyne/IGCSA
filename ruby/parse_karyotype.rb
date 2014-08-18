require 'cytogenetics'
require 'logger'



#file = ARGV[1]
file = "/tmp/HCC1143.txt"


kt_string = File.open(file, 'r').read
kt_string.sub!(/\s*/, "")


log = Logger.new("/tmp/kt_parse.log")
log.datetime_format = "%M"
log.level = Logger::INFO
Cytogenetics.logger = log






kt = Cytogenetics.karyotype(kt_string)

puts "Breakpoints: #{kt.report_breakpoints}"
puts "Fragments: #{kt.report_fragments}"
kt.report_ploidy_change
puts kt.normal_chr
puts kt.summarize
kt.aberrations.each_pair do |k,v|
  puts "#{k}: #{v}"
end