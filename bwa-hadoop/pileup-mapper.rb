



$stdin.each do |line|
  line.chomp!

  (chr, pos, ref_allele, count, bases, qualities) = line.split("\t")

  puts [chr, pos, count].join("\t")
end
