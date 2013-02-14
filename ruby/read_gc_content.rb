require_relative 'lib/utils'
require 'yaml'
require 'zlib'

def count_gc(sequence)
  gc = sequence.split("")
  gc.keep_if {|e| e.match(/G|C/) }
  return gc.length
end

unless ARGV.length > 0
  puts "Usage: $0 <config file>"
  exit
end

config_defaults = YAML.load_file("resources/var.props.example")
cfg = Utils.check_config(ARGV[0], config_defaults, ['tabix.path', 'chromosome.data'])

Utils.setup_dirs(cfg['output.dir'].split(";"), true)
output_dir = cfg['output.dir']

fout = File.new("#{output_dir}/gc-content.txt", 'w')
fout.write( ['Chr', 'GC', 'TotalBP'].join("\t") + "\n" )


fasta = cfg['fasta.dir']
gc_content = {}
Dir.foreach(fasta) do |entry|
  if entry.match(/chr(\d+|X|Y)\.fa\.gz/)
    chr = $1
    gc_count = 0
    total_bp = 0
    if File.basename(entry, ".gz") # unzip
      Zlib::GzipReader.new(File.new("#{fasta}/#{entry}")).each_line do |line|
        line.chomp!
        next if line.start_with?">"
        total_bp += line.length
        # for some odd reason match was not working against the whole line
        gc_count += count_gc(line)
        break if gc_count > 120000
      end
    elsif File.basename(entry, ".fa")
      File.open("#{fasta}/#{entry}").each_line do |line|
        line.chomp!
        next if line.start_with?">"
        gc_count += count_gc(line)
        total_bp += line.length
      end
    end
    percentage = ((gc_count.to_f/total_bp.to_f)*100).round(2)
    fout.write( [chr, gc_count, total_bp].join("\t") + "\n")
    puts "chr#{chr} #{percentage}%"
  end
end
fout.close
