require_relative 'lib/gvf_reader'

require 'yaml'


file = ARGV[0]

#file = "/Users/sarah.killcoyne/Data/Ensembl/Homo_sapiens_somatic.gvf"
dir = "/Users/sarah.killcoyne/Data/VariationNormal/Ensembl"

chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')

reader = GFF::GVFReader.new(file)
puts YAML::dump reader

# --- Separate GVF into Chromosome specific files -- #
chrfh = Hash[chrs.map{|c| [c, File.open("#{dir}/chr#{c}.txt", 'w')] }]
chrfh.each_pair {|k,fh| fh.write(['chr', 'start', 'end', 'var.type', 'ref.seq', 'var.seq', 'local.id'].join("\t") + "\n" ) }

while gvf = reader.read_line
  printf "." if gvf.line_num % 500 == 0
  #puts "Reading #{gvf.line_num}"
  next unless chrfh.has_key?gvf.chr
  fh = chrfh[gvf.chr]
  fh.write( [gvf.chr, gvf.start_pos, gvf.end_pos, gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq], gvf.attributes[:id]].join("\t") + "\n" )
end



