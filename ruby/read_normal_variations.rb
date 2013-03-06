require_relative 'lib/utils'
require 'yaml'
require 'vcf'
require_relative 'lib/variation_reader'




config = ARGV[0]
if ARGV.length <= 0
  puts "Usage: $0 <config file>"
  puts "Using default config resources/var.props"
  config = "resources/var.props"
elsif ARGV.length.eql? 1
  config = ARGV[0]
elsif ARGV.length.eql? 2
  config = ARGV[0]
  vcf_file = ARGV[1]
  unless vcf_file.match(/\.vcf/)
    puts "#{vcf_file} is not a recognized VCF"
    exit
  end
end

# --- Setup is the same for directory of VCF or a single file --- #
config_defaults = YAML.load_file("resources/var.props.example")
cfg = Utils.check_config(config, config_defaults, ['tabix.path', 'chromosome.data'])

output_dir = "#{cfg['freq.output.dir']}/#{cfg['window']}"
Utils.setup_dirs([output_dir, "#{output_dir}/tmp"], true)

chr_info_file = cfg['chromosome.data'] || 'resources/chromosome_gene_info_2012.txt'
chr_info = {}
total_length = 0
File.open(chr_info_file, 'r').each_line do |line|
  line.chomp!
  cols = line.split("\t")
  (chr, cm, bplength) = cols[0..2]
  chr_info[chr] = bplength
  total_length += bplength.to_i
end
puts "BP length of genome: #{total_length}"

tabix = cfg['tabix.path'] || 'tabix'


  var_dirs = cfg['variation.dir'].split(';')
  var_dirs.map! { |e| [e, Dir["#{e}/*.vcf.gz"]] }

  threads, warnings = [], []
  var_dirs.each do |dirpair|
    (dir, files) = dirpair[0..1]

    warn "#{dir} does not exist" unless File.exists? dir
    puts "Reading #{dir}"

    bpwindow = cfg['window']

    files.each do |entry|
      if entry.match(/chr(\d+|X|Y)/)
        chr = $1
        puts "#{chr} #{chr_info[chr]}: #{entry}"

        threads << Thread.new(chr) {

          reader = VariationReader.new(chr, entry, output_dir)
          reader.set_tabix(tabix)
          rv = reader.read_variations(bpwindow, chr_info[chr])

          Thread.current['warnings'] = rv[:warnings]
          Thread.current['bins'] = rv[:bins]
          Thread.current['file'] = rv[:output]
          Thread.current['chr'] = chr
        }
      else # use some other tool
        warnings << "#{entry} REQUIRES A TOOL OTHER THAN TABIX"
      end
    end
  end

  total_bins = 0
  threads.each { |t|
    t.join; t.abort_on_exception;
    puts "Read chromosome #{t['chr']}"
    puts t['info']
    puts "#{t['file']} written"
    puts "ERRORS: " + t['warnings'].join("\n") unless t['warnings'].empty?
    total_bins += t['bins']
  }
  puts "Total bins: #{total_bins}"
  warn "WARNING: " + warnings.join("\n")
  # don't keep the vcf file around
  FileUtils.rm_rf("#{output_dir}/tmp")
