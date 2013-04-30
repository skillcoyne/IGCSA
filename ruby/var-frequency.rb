require_relative 'lib/gvf_reader'
require_relative 'lib/utils'

require 'yaml'


# --- Setup is the same for directory of VCF or a single file --- #
#config_defaults = YAML.load_file("resources/var.props.example")
#cfg = Utils.check_config("resources/var.props", config_defaults, ['tabix.path', 'chromosome.data'])
cfg = YAML.load_file("resources/var.props")


output_dir = "#{cfg['output.dir']}/Frequency"
puts output_dir

dir = "#{cfg['variation.dir']}/chromosomes"
puts dir
files = Dir["#{dir}/chr*.txt"]

chrs = ("1".."22").to_a
chrs.push('X')
chrs.push('Y')


snv = {}
length_vars = {}
vars = {}

files.each do |entry|
  chrdir = "#{output_dir}/" + File.basename(entry).sub("\.txt", "") + "/vars"
  FileUtils.rm_f(chrdir) if File.exists?chrdir

  File.open(entry, 'r').each_with_index do |line, index|
    line.chomp!
    next if index.eql? 0
    (chr, start, stop, type, refseq, varseq, id) = line.split("\t")

    if type.eql? 'SNV'
      next if refseq.eql?'.'
      varseq.split(",").each do |s|
        next if s.eql?'.' or s.eql?'N'
        snv[refseq+s] = 0 unless snv.has_key? refseq+s
        snv[refseq+s]+= 1
      end
    else
      length_vars[type] = {} unless length_vars.has_key? type
      vars[type] = {} unless vars.has_key? type

      start = start.to_i; stop = stop.to_i
      (stop-start == 0) ? (length = 1) : (length = stop-start)
      length_vars[type][length] = 0 unless length_vars[type].has_key? length
      length_vars[type][length] += 1

      varseq = refseq if type.eql? 'deletion'

      vars[type][varseq] = 0 unless vars[type].has_key? varseq
      vars[type][varseq] += 1
    end

  end
end
# SNV counts
File.open("#{output_dir}/snv-counts.txt", 'w') { |f|
  Hash[snv.sort].each_pair do |base, count|
    f.write "#{base}\t#{count}\n"
  end
}
# Length based variation counts
length_vars.each_pair do |variation, lengths|
  line = []
  File.open("#{output_dir}/#{variation}-length-count.txt", 'w') { |f|
    f.write ['length', 'count'].join("\t") + "\n"
    Hash[lengths.sort].each_pair { |l, c| f.write [l, c].join("\t") + "\n" }
  }
end

# Variation sequence counts
#File.open("#{chrdir}/#{variation}-seq.txt", 'w') { |f|
#  f.write ['sequence', 'sequence.count'].join("\t") + "\n"
#  Hash[vars[variation].sort].each_pair { |s, c| f.write [s, c].join("\t") + "\n" }
#}



