require_relative 'lib/gvf_reader'
require_relative 'lib/utils'

require 'yaml'


# --- Separate GVF into Chromosome specific files -- #
unless ARGV.length < 1
  gvf_file_dir = ARGV[0]
else
  warn "Usage: #{$0} <gvf file dir>"
end

gvf_files = Dir["#{gvf_file_dir}/*.gvf*"]

chromosomes_dir = "#{gvf_file_dir}/chromosomes"
FileUtils.mkpath(chromosomes_dir) unless File.exists?chromosomes_dir

chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')

chrfh = Hash[chrs.map { |c| [c, File.open("#{chromosomes_dir}/chr#{c}.txt", 'w')] }]
chrfh.each_pair { |k, fh|
  fh.write(['chr', 'start', 'end',
            'var.type', 'ref.seq', 'var.seq',
            'local.id', 'source', 'validation', 'clinical' ].join("\t") + "\n") }

count_ids = {}

unvalidated = 0
records = 0
count = 0
classes = {}
gvf_files.each do |file|
  puts "Reading #{file}"

  reader = GFF::GVFReader.new(file)
  while gvf = reader.read_line
    records += 1
    printf "." if gvf.line_num % 500 == 0
    next unless chrfh.has_key? gvf.chr
    fh = chrfh[gvf.chr]
    records += 1

    #unless gvf.score.nil?
    #end

    local_id = gvf.attributes[:id]
    if gvf.source.eql?"dbSNP"
      local_id = gvf.attributes[:dbsnp_137]
    end

    if gvf.attributes[:validation] and gvf.attributes[:validation].match(/1000Genome|HapMap/)
      count_ids[local_id] = 0 unless count_ids.has_key?local_id
      count_ids[local_id] += 1
      classes[gvf.method] = 1;
      count += 1

      fh.write([gvf.chr, gvf.start_pos, gvf.end_pos,
              gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq],
              local_id, gvf.source, gvf.attributes[:validation_states], gvf.attributes[:clinical_significance]].join("\t") + "\n")
    else
      unvalidated += 1
    end

  end
end

puts YAML::dump count_ids
puts count_ids.length

puts classes.keys.join(", ")

chrfh.each_pair{|k, fh| fh.close }

puts "Total unvalidated #{unvalidated} of #{records}"

puts "Validated? #{count}"
