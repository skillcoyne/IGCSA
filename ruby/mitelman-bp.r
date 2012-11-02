


file = ARGV[0]
path = File.absolute_path(file).sub!(File.basename(file), "")
base = File.basename(file, ".txt")
fout = File.open("#{path}#{base}-chr.txt", 'w')
fout.write("RefNo\tCaseNo\tBreakpoint\tChr\n")
File.open(file, 'r').each_line do |line|
  line.chomp!

  cols = line.split("\t")
  cols[-1] =~ /^([\d|X|Y]{1,2})/
  fout.write("#{cols.join("\t")}\t#{$1}\n")


end

fout.close
puts "#{path}#{base}-chr.txt written."