require_relative 'lib/gvf_reader'
require_relative 'lib/utils'

require 'yaml'


# --- Setup is the same for directory of VCF or a single file --- #
cfg = YAML.load_file("resources/var.props")

# --- Separate GVF into Chromosome specific files -- #
# Am assuming this contains just the hapmap gvfs
dir = cfg['variation.dir']
gvf_files = Dir["#{dir}/*.gvf"]


chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')

chrfh = Hash[chrs.map { |c| [c, File.open("#{dir}/chromosomes/chr#{c}.txt", 'w')] }]
chrfh.each_pair { |k, fh|
  fh.write(['chr', 'start', 'end',
            'var.type', 'ref.seq', 'var.seq',
            'local.id', 'validation', 'source'].join("\t") + "\n") }

unvalidated = 0
records = 0
gvf_files.each do |file|
  puts "Reading #{file}"

  reader = GFF::GVFReader.new(file)
  while gvf = reader.read_line
    records += 1
    printf "." if gvf.line_num % 500 == 0
    next unless chrfh.has_key? gvf.chr
    fh = chrfh[gvf.chr]

    if   gvf.attributes[:validation].nil? ||
         gvf.attributes[:validation].match(/unknown|suspected|-/)
      unvalidated += 1
      next
    end

    if gvf.attributes[:validation].match(/1000Genome|HapMap/)
      count += 1

      source = File.basename(file, '.gvf')
      source = source.sub("CSHL-HAPMAP-", "") if source.downcase.match(/hapmap/)


      fh.write([gvf.chr, gvf.start_pos, gvf.end_pos,
                gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq],
                local_id, gvf.attributes[:validation], source].join("\t") + "\n")
    else
      unvalidated += 1
    end




    local_id = gvf.attributes[:id]
    if gvf.source.eql?'dbSNP'
      local_id = gvf.attributes[:dbsnp_137]
    end

    fh.write([gvf.chr, gvf.start_pos, gvf.end_pos,
              gvf.method, gvf.attributes[:reference_seq], gvf.attributes[:variant_seq],
              local_id, gvf.attributes[:variant_freq], population, gvf.source].join("\t") + "\n")
  end
end

chrfh.each_pair { |k, fh| fh.close }

puts "Total unvalidated #{unvalidated} of #{records}"
