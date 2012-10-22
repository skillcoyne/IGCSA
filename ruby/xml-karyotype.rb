require 'yaml'
require 'fileutils'
require_relative 'lib/sky_karyotype'


dir = "/Users/skillcoyne/Data/sky-cgh/ESI/"
#dir = "C:/Users/LCSB_Student/Desktop/Data/sky-cgh/ESI/"
error_dir = "#{dir}/logs"
FileUtils.mkdir(error_dir) unless Dir.exists?error_dir

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
  current_case = ""
  File.open(fullpath, 'r').each_line do |line|

    line = line.chomp
    if line.match(/SkyCase/)
      current_case = line.sub(/SkyCase/, "")
    end
    if line.match(/stage/)
      stage[entry] += 1
    end
    if line.match(/Karyotype/)
      #Logging.configure({'logout' => "#{error_dir}/#{entry}.log"})
      log = Logger.new("#{error_dir}/#{entry}.log")
      sk = SkyKaryotype.new(log)
      line.sub!(/^\s*Karyotype\s*/, "")
      line.gsub!(/\s*/, "")
      line.gsub!(/&lt;/,"<")
      line.gsub!(/&gt;/, ">")
      line.gsub!(/&quot;/, '"')

      # Sometimes includes multiple karyotypes under "Clone" headings
      next if line.match(/Clone/)
      puts line
      sk.parse(line)
      #puts sk.karyotype
      karyotypes[entry] += 1
    end
  end
end


#karyotypes.delete_if {|k,v| v <= 0}
#
#karyotypes.each_pair do |k, v|
#  puts "#{k}: #{cases[k]} #{v} #{stage[k]}"
#end
