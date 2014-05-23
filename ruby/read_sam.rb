
## ONLY FOR SMALL SAM FILES


file = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments/HCC1954.2/der5-3/mapped-reads.sam"

File.open(file, 'r').each_line do |line|

  line.chomp!

  puts line
  read_data = line.split("\t")

  puts read_data
  puts read_data.size

  break

end