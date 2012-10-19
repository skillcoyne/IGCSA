require 'yaml'
require 'logger'
require_relative 'lib/sky_karyotype'

log = Logger.new(STDOUT)
log.level = Logger::Error

dir = "C:\\Users\\LCSB_Student\\Desktop\\Data\\sky-cgh\\ESI\\"
sk = SkyKaryotype.new

karyotypes = {}
stage = {}
cases = {}
Dir.foreach(dir) do |entry|
  karyotypes[entry] = 0
  stage[entry] = 0
  cases[entry] = 0
  fullpath = "#{dir}#{entry}"
  next if File.directory?(fullpath)
  next if File.basename(fullpath).start_with?(".")
  File.open(fullpath, 'r').each_line do |line|
    line = line.chomp
    if line.match(/SkyCase/)
      cases[entry] +=1
    end
    if line.match(/stage/)
      stage[entry] += 1
    end
    if line.match(/Karyotype/)
      line.sub!(/^\s*Karyotype\s*/, "")
      puts line
      sk.parse(line)
      #puts sk.karyotype
      karyotypes[entry] += 1
      break
    end
  end
end


#karyotypes.delete_if {|k,v| v <= 0}
#
#karyotypes.each_pair do |k, v|
#  puts "#{k}: #{cases[k]} #{v} #{stage[k]}"
#end
