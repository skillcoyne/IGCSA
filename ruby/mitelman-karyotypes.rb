require_relative 'lib/sky_karyotype'
require 'logger'

file = ARGV[0]
path = File.absolute_path(file).sub!(File.basename(file), "")

logger = Logger.new(File.new("#{path}/karyotype_log.txt", 'w'))

File.open(file, 'r').each_line do |line|
  line.chomp!
  next if line.start_with?"#"

  sk = SkyKaryotype.new(logger)
  sk.parse(line)

  puts sk.breakpoints


end