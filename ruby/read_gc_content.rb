require_relative 'lib/utils'
require 'yaml'
require 'zlib'


def count(regex, seq)
  # for some odd reason match was not working against the whole line
  return seq.select { |e| e.match(regex) }.length
end

def count_gc(seq)
  return count(/G|C/, seq)
end

def count_unk(seq)
  return count(/N/, seq)
end

def count_gap(seq)
  return count(/-/, seq)
end

def gather_sequence_statistics(file, chr)
  window = $CFG['window']

  fout = File.new("#{$OUTDIR}/chr#{chr}-gc.txt", 'w')
  fout.write(['GC', 'Unk', 'Gap', 'BPs'].join("\t") + "\n")

  gc, unk, bp = 0, 0, 0
  bp_in_window = []

  file.each_line do |line|
    line.chomp!
    next if line.start_with? ">"
    bps = line.split("")

    # GC and unknown content per chromosome
    gc += count_gc(bps)
    unk += count_unk(bps)
    gap += count_gap(bps)
    bp += bps.length

    # GC and unknown content per window
    bps.each do |bp|
      bp_in_window << bp
      if bp_in_window.length.eql? window
        fout.write([count_gc(bp_in_window), count_unk(bp_in_window), count_gap(bp_in_window), bp_in_window.length].join("\t") + "\n")
        bp_in_window.clear
      end
    end
  end

  # Final chunk will not be max length so output the size for each window
  fout.write([count_gc(bp_in_window), count_unk(bp_in_window), count_gap(bp_in_window), bp_in_window.length].join("\t") + "\n")

  return {:gc_count => gc, :unk_count => unk, :gap_count => gap, :bp => bp}
end

config = ARGV[0]
unless ARGV.length > 0
  puts "Usage: $0 <config file>"
  puts "Using default config resources/var.props"
  config = "resources/var.props"
end

config_defaults = YAML.load_file("resources/var.props.example")
$CFG = Utils.check_config(config, config_defaults, ['tabix.path', 'chromosome.data'])

$OUTDIR = "#{$CFG['gc.output.dir']}/#{$CFG['window']}"
Utils.setup_dirs([$OUTDIR], false)

#fout = File.new("#{$OUTDIR}/gc-content.txt", 'w')
#fout.write(['Chr', 'GC', 'Unk' 'TotalBP'].join("\t") + "\n")

fasta = $CFG['fasta.dir']
fastaFiles = Dir["#{fasta}/*.fa.*"]

threads = []
fastaFiles.each do |entry|
  if entry.match(/chr(\d+|X|Y)/)
    chr = $1
    threads << Thread.new(chr) {
      Thread.current['chr'] = "#{chr} : #{entry}"
      if File.basename(entry, ".gz") # unzip
        stats = gather_sequence_statistics(Zlib::GzipReader.new(File.new("#{fasta}/#{entry}")), chr)
      elsif File.basename(entry, ".fa")
        stats = gather_sequence_statistics(File.open("#{fasta}/#{entry}"), chr)
      end
      gc_count = stats[:gc_count]; total_bp = stats[:bp]; unk_count = stats[:unk_count]; gap_count = stats[:gap_count]
      #percentage = ((gc_count.to_f/total_bp.to_f)*100).round(2)
      Thread.current['info'] = ["Chr#{chr}", "GC:#{gc_count}", "Unk:#{unk_count}", "Gap:#{gap_count}", total_bp].join("\t")
    }
  end
end

# It was very slow running it the other way.  As threads each chromosome can be done independently
threads.each{|t|
  t.join; t.abort_on_exception = true;
  puts t['info']
}

