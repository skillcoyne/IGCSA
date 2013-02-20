require_relative 'lib/utils'
require 'yaml'
require 'vcf'

config = ARGV[0]
unless ARGV.length > 0
  puts "Usage: $0 <config file>"
  puts "Using default config resources/var.props"
  config = "resources/var.props"
end

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


tabix = cfg['tabix'] || 'tabix'

var_dirs = cfg['variation.dir'].split(';')
var_dirs.map! { |e| [e, Dir["#{e}/*.vcf.gz"]] }
threads, warnings = [], []


var_dirs.each do |dirpair|
  (dir, files) = dirpair[0..1]

  warn "#{dir} does not exist" unless File.exists? dir
  puts "Reading #{dir}"

  files.each do |entry|
    if entry.match(/chr(\d+|X|Y)/)
      chr = $1
      puts "#{chr} : #{entry}"
      threads << Thread.new(chr) {
        Thread.current['warnings'] = []
        Thread.current['chr'] = chr
        var_count_file = "#{output_dir}/chr#{chr}-counts.txt"

        # File per chromosome SNP/indel counts
        vout = File.open(var_count_file, 'w')
        vout.write("# Counts per #{cfg['window']} base pairs\n")
        vout.write(['BIN','SNP', 'INDEL'].join("\t") + "\n")

        bins = (chr_info[chr].to_f/cfg['window'].to_f).ceil
        Thread.current['bins'] = bins
        Thread.current['info'] = "chr#{chr} (#{chr_info[chr]}), window #{cfg['window']}, #{bins} bins"

        (1..bins).each do |win|
          next unless (win.eql? 1 or win%cfg['window']==0) # this might only work if the window is multiples of 10...

          # Set up bins
          min = win; max = win+cfg['window']
          (win.eql? 1) ? (max -= 1) : (min += 1)

          tabix_opt = "#{chr}:#{min}-#{max}"
          vcf_file = "#{output_dir}/tmp/chr#{chr}:#{min}-#{max}.vcf"

          cmd = "#{tabix} #{entry} #{tabix_opt} > #{vcf_file}"
          sys = system("#{cmd}")
          Thread.current['warnings'] << "tabix failed to run on #{entry}, please check that it is installed an available in your system path." unless sys

          begin
            snp_count, indel_count = 0, 0
            File.open(vcf_file, 'r').each_line do |line|
              line.chomp!
              vcf = Vcf.new(line)
              vcf.samples.clear
              #(variants["#{vcf.ref}#{vcf.alt}"] ||= []) << vcf
              snp_count += 1 if vcf.info['VT'].eql? 'SNP'
              indel_count += 1 if vcf.info['VT'].eql? 'INDEL'
            end
            vout.write(["#{min}-#{max}", snp_count, indel_count].join("\t") + "\n")
          rescue Errno::ENOENT => e
            Thread.current['warnings'] << "File not found: #{vcf_file}. #{e.message}"
          end
        end
        vout.close
        Thread.current['file'] = var_count_file
      }
    else # use some other tool
      warnings << "#{entry} REQUIRES A TOOL OTHER THAN TABIX"
    end
  end
end

# don't keep the vcf file around
total_bins = 0
threads.each { |t|
  t.join; t.abort_on_exception;
  puts "Read chromosome #{t['chr']}"
  puts t['info']
  puts "#{t['file']} written"
  puts "ERRORS: " + t['warnings'].join("\t") + "\n" unless t['warnings'].empty?
  total_bins += t['bins']
}

puts "Total bins: #{total_bins}"

warn "WARNING: " + warnings.join("\n")

FileUtils.rm_rf("#{output_dir}/tmp")


