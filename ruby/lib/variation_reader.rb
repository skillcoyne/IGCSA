require 'yaml'

class VariationReader
    @tabix = 'tabix'


  def initialize(chromosome, vcf, output_dir)
    @chr = chromosome
    @file = vcf
    @output_dir = output_dir

    raise IOError "#{@output_dir} does not exist." unless File.exists?@output_dir
  end

  def set_tabix(tabix)
    @tabix = tabix
  end

  def read_variations(bpwindow, maxlength)
    # File per chromosome SNP/indel counts
    vout = File.open("#{@output_dir}/chr#{@chr}-counts.txt", 'w')
    vout.write("# Counts per #{bpwindow} base pairs\n")
    vout.write(['BIN','SNP', 'INDEL'].join("\t") + "\n")

    warnings = []

    bins = (maxlength.to_i/bpwindow.to_i).ceil
    min = 0; max = 0
    bins.times do
      max += bpwindow.to_i
      tabix_opt = "#{@chr}:#{min}-#{max}"
      vcf_file = "#{@output_dir}/tmp/chr#{@chr}:#{min}-#{max}.vcf"
      cmd = "#{@tabix} #{@file} #{tabix_opt} > #{vcf_file}"
      sys = system("#{cmd}")
      warnings << "tabix failed to run on #{@file}, please check that it is installed an available in your system path." unless sys

      begin
        snp_count, indel_count = 0, 0
        File.open(vcf_file, 'r').each_line do |line|
          line.chomp!
          vcf = Vcf.new(line)
          vcf.samples.clear
          snp_count += 1 if vcf.info['VT'].eql? 'SNP'
          indel_count += 1 if vcf.info['VT'].eql? 'INDEL'
        end
        vout.write(["#{min}-#{max}", snp_count, indel_count].join("\t") + "\n")

        FileUtils.rm_f(vcf_file)
      rescue Errno::ENOENT => e
        warnings << "File not found: #{vcf_file}. #{e.message}"
      end
      min = max
    end
    return {:warnings => warnings, :bins => bins, :output => "#{@output_dir}/chr#{@chr}-counts.txt"}
  end


end