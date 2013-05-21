require_relative 'lib/gvf_reader'
require_relative 'lib/utils'

require 'yaml'


# --- Setup is the same for directory of VCF or a single file --- #
#cfg = YAML.load_file("resources/var.props")

# --- Separate GVF into Chromosome specific files -- #
unless ARGV.length < 1
  gvf_files = ARGV[0]
  dir = ARGV[1]
else
  warn "Usage: #{$0} <gvf files, separated by comma> <dir to output> "
end

gvf_files = gvf_files.split(",")

if File.exists?dir
  FileUtils.mkpath("#{dir}/chromosomes")
else
  raise "#{dir} doesn't exist. Exiting."
end

puts gvf_files
puts dir


sources ={
    'dbSNP' => 'dbsnp_137',
    'COSMIC' => 'cosmic_61'
}

require_valid = false #  1000Genome|HapMap

chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')

chrfh = Hash[chrs.map { |c| [c, File.open("#{dir}/chromosomes/chr#{c}.txt", 'w')] }]
chrfh.each_pair { |k, fh|
  fh.write(['chr', 'start', 'end',
            'var.type', 'ref.seq', 'var.seq',
            'local.id', 'source' ].join("\t") + "\n") }

unvalidated = 0
records = 0
count = 0
gvf_files.each do |file|
  puts "Reading #{file}"

  reader = GFF::GVFReader.new(file)
  while gvf = reader.read_line
    records += 1
    printf "." if gvf.line_num % 500 == 0
    next unless chrfh.has_key? gvf.chr
    fh = chrfh[gvf.chr]
    records += 1

    local_id = gvf.attributes[:id]
    sources.each_pair do |source, id|
      if gvf.source.eql?source
        local_id = gvf.attributes[id.to_sym]
        break
      end
    end

    #if validated_by
    #if gvf.attributes[:validation_states].match(/1000Genome|HapMap/)
    #  count += 1
    #
      fh.write([gvf.chr, gvf.start_pos, gvf.end_pos,
              gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq],
              local_id, gvf.source].join("\t") + "\n")
    #else
    #  unvalidated += 1
    #end

  end
end

chrfh.each_pair{|k, fh| fh.close }

#puts "Total unvalidated #{unvalidated} of #{records}"

puts "Validated? #{count}"
