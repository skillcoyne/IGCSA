require 'yaml'


dir = "/Volumes/exHD-Killcoyne/Insilico/runs/alignments"

r1 = "#{dir}/1p3610p14.ppreads"
r2 = "#{dir}/1p3610p12.ppreads"


left = Hash.new
right = Hash.new

reads = Hash.new
puts "Reading #{r1}"
File.open(r1, 'r').each_line do |line|
  line.chomp!
  cols = line.split("\t")

  if cols[3].to_i < 28000000
    left[cols[0]] = {'mapq' => cols[4], 'ref' => cols[2], 'pos' => cols[3].to_i}
  else
    right[cols[0]] = {'mapq' => cols[4], 'ref' => cols[2], 'pos' => cols[3].to_i}
  end

  # highest qual read
  if (reads.has_key? cols[0] and reads[cols[0]]['mapq'] < cols[4]) or !reads.has_key? cols[0]
    reads[cols[0]] = {'mapq' => cols[4], 'pos' => cols[3]}
  end

end

shared = Hash.new

puts "Reading #{r2}"
File.open(r2, 'r').each_line do |line|
  line.chomp!
  cols = line.split("\t")

  # if (reads.has_key?cols[0] and reads[cols[0]]['mapq'] < cols[4])
  # #if reads.has_key? cols[0]
  #
  #   shared[cols[0]] = {'mapq.1' => reads[cols[0]]['mapq'], 'mapq.2' => cols[4],
  #                      'ref.1' => reads[cols[0]]['ref'], 'ref.2' => cols[2],
  #                      'pos.1' => reads[cols[0]]['pos'], 'pos.2' => cols[3]}
  # end

  if left.has_key? cols[0] and (cols[3].to_i < 28000000 and left[cols[0]]['pos'] != cols[3].to_i)
    left[cols[0]]['shared'] = "#{cols[3]}, #{cols[4]}"
  end

  if right.has_key? cols[0] and (cols[3].to_i > 28000000 and right[cols[0]]['pos'] != cols[3].to_i)
    right[cols[0]]['shared'] = "#{cols[3]}, #{cols[4]}"
  end

end


puts YAML::dump left

# puts YAML::dump shared
# puts reads.length
#
# puts shared.length
#
# puts reads.length > shared.length
#

puts left.length
puts right.length