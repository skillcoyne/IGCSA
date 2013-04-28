require_relative 'lib/gvf_reader'
require_relative 'lib/utils'

require 'yaml'


# --- Setup is the same for directory of VCF or a single file --- #
cfg = YAML.load_file("resources/var.props")

# --- Separate GVF into Chromosome specific files -- #
dir = cfg['variation.dir']
gvf_files = Dir["#{dir}/*.gvf"]

chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')

chrfh = Hash[chrs.map { |c| [c, File.open("#{dir}/chromosomes/chr#{c}.txt", 'w')] }]
chrfh.each_pair { |k, fh| fh.write(['chr', 'start', 'end', 'var.type', 'ref.seq', 'var.seq', 'local.id'].join("\t") + "\n") }

gvf_files.each do |file|
	puts "Reading #{file}"

	reader = GFF::GVFReader.new(file)

	while gvf = reader.read_line
    printf "." if gvf.line_num % 500 == 0
    #puts "Reading #{gvf.line_num}"
    next unless chrfh.has_key? gvf.chr
    fh = chrfh[gvf.chr]
    fh.write([gvf.chr, gvf.start_pos, gvf.end_pos, gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq], gvf.attributes[:id]].join("\t") + "\n")
	end
end

chrfh.each_pair{|k, fh| fh.close }



