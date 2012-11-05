


file = ARGV[0]
path = File.absolute_path(file).sub!(File.basename(file), "")
base = File.basename(file, ".txt")
fout = File.open("#{path}#{base}-chr.txt", 'w')
fout.write("Breakpoint\tChr\tBand\n")
skipped = []
File.open(file, 'r').each_line do |line|
  line.chomp!
  cols = line.split("\t")

  # missing the chromosome
  if (cols[-1].match(/^([\d|X|Y]{1,2})([q|p]\d+)/))
    #puts line
    chr = $1; band = $2
    puts "#{cols[-1]}\t#{chr}\t#{band}\n"
    fout.write("#{cols[-1]}\t#{chr}\t#{band}\n") #unless cols[-1].match(/or/)
  else
    skipped.push(line)
  end
  #fout.write("#{cols.join("\t")}\t#{$1}\n")
end

fout.close
puts "Skipped: #{skipped.length} that lacked a chromosome, lacked a band or had incorrectly formatted band information."

puts "#{path}#{base}-chr.txt written."