require_relative 'lib/gvf_reader'

require 'yaml'




if ARGV[0].eql?'-s'
  separate_gvf = ARGV[1]
end





# --- Setup is the same for directory of VCF or a single file --- #
config_defaults = YAML.load_file("resources/var.props.example")
cfg = Utils.check_config("resources/var.props", config_defaults, ['tabix.path', 'chromosome.data'])

output_dir = "#{cfg['freq.output.dir']}/Ensembl/#{cfg['window']}"
Utils.setup_dirs([output_dir, "#{output_dir}/tmp"], true)

# --- Separate GVF into Chromosome specific files -- #

if separate_gvf

  chrs = ("1".."22").to_a
  chrs.push('X')
  chrs.push('Y')

  reader = GFF::GVFReader.new(file)
  puts YAML::dump reader

  chrfh = Hash[chrs.map { |c| [c, File.open("#{dir}/chr#{c}.txt", 'w')] }]
  chrfh.each_pair { |k, fh| fh.write(['chr', 'start', 'end', 'var.type', 'ref.seq', 'var.seq', 'local.id'].join("\t") + "\n") }

  while gvf = reader.read_line
    printf "." if gvf.line_num % 500 == 0
    #puts "Reading #{gvf.line_num}"
    next unless chrfh.has_key? gvf.chr
    fh = chrfh[gvf.chr]
    fh.write([gvf.chr, gvf.start_pos, gvf.end_pos, gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq], gvf.attributes[:id]].join("\t") + "\n")
  end

else
  dir = cfg['variation.dir']
  files = Dir["#{dir}/*.txt"]

  files.each do |file|

    File.open(file, 'r').each_line do |line|

    end
  end

end





