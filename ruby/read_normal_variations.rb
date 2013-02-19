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
Utils.setup_dirs([output_dir], true)

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

puts total_length


tabix = cfg['tabix'] || 'tabix'

warnings = []

threads = []
var_dirs = cfg['variation.dir'].split(';')
var_dirs.each do |dir|
  warn "#{dir} does not exist" unless File.exists? dir
  puts "Reading #{dir}"
  Dir.foreach(dir) do |entry|
    next if File.directory? "#{dir}/#{entry}"
    next if entry.start_with? "." or entry.match(/\.tbi$/) # ignore index files which also happen to have 'vcf' in the file name

    if entry.match(/chr(\d+|X|Y).*\.(vcf)/)
      chr = $1

      threads << Thread.new(chr) {
        var_count_file = "#{output_dir}/chr#{chr}-counts.txt"
        vout = File.open(var_count_file, 'w')
        vout.write("# Counts per #{cfg['window']} base pairs\n")
        vout.write(['SNP', 'INDEL'].join("\t") + "\n")

        bins = (chr_info[chr].to_f/cfg['window'].to_f).ceil
        #puts "Analyzing chromosome #{chr} (#{chr_info[chr]}), variation window #{cfg['window']}, #{bins} bins..."
        Thread.current['bins'] = "chr#{chr} (#{chr_info[chr]}), window #{cfg['window']}, #{bins} bins"

        (1..bins).each do |win|
          next unless (win.eql? 1 or win%cfg['window']==0) # this might only work if the window is multiples of 10...

          min = win; max = win+cfg['window']
          (win.eql? 1) ? (max -= 1) : (min += 1)
          #puts "#{win} #{min}-#{max}"

          tabix_opt = "#{chr}:#{min}-#{max}"
          vcf_file = "#{output_dir}/chr#{chr}:#{min}-#{max}.vcf"

          #puts tabix_opt

          cmd = "#{tabix} #{dir}/#{entry} #{tabix_opt} > #{vcf_file}"
          #puts cmd
          sys = system("#{cmd}")

          #warnings << "tabix failed to run on #{entry}, please check that it is installed an available in your system path." unless sys
          Thread.current['warnings'] << "tabix failed to run on #{entry}, please check that it is installed an available in your system path." unless sys

          #variants = {}
          snp_count, indel_count = 0, 0

          File.open(vcf_file, 'r').each_line do |line|
            line.chomp!
            vcf = Vcf.new(line)
            vcf.samples.clear
            #(variants["#{vcf.ref}#{vcf.alt}"] ||= []) << vcf

            snp_count += 1 if vcf.info['VT'].eql? 'SNP'
            indel_count += 1 if vcf.info['VT'].eql? 'INDEL'
          end

          #puts [entry, snp_count, indel_count].join("\t")
          vout.write([snp_count, indel_count].join("\t") + "\n")
          FileUtils.rm_f(vcf_file) # don't keep the vcf file around
        end
        vout.close
        #puts var_count_file
      }
    else # use some other tool
      warnings << "#{entry} REQUIRES A TOOL OTHER THAN TABIX"
    end
  end
end

threads.each {|t|
  t.join; t.abort_on_exception;
  puts t['bins']
  puts t['warnings'].join("\n")
}

warn warnings.join("\n")


