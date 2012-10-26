require 'yaml'
require 'fileutils'
require_relative 'lib/easy_sky_record'
require_relative 'lib/chromosome_fragment'

#dir = "/Users/skillcoyne/Data/sky-cgh/ESI/"

if ARGV.length <= 0
  puts "Requires a directory with sky ESI files."
  exit
end

dir = ARGV[0]

kdir = "#{dir}/karyotype"
bpdir = "#{dir}/breakpoints"

#output dirs
FileUtils.rm_rf(kdir) if Dir.exists?(kdir)
FileUtils.rm_rf(bpdir) if Dir.exists?(bpdir)

FileUtils.mkdir(kdir)
FileUtils.mkdir(bpdir)

records = {}
karyotypes = {}
Dir.foreach(dir) do |entry|
  file = "#{dir}/#{entry}"
  next if entry.start_with?(".")
  next if File.directory?(file)
  next unless File.basename(entry).match(/\.esi/)

  puts "Reading #{entry}..."

  esr = nil #EasySkyRecord.new()
  current_chr = nil
  skychrs = 0; cases = 0
  skycases = []
  File.open(file, 'r').each_line do |line|
    line = line.chomp

    if line.match(/^\s?SkyCase/)
      if cases > 0
        esr.stage = 'N/A' if esr.stage.nil?
        esr.diagnosis = 'N/A' if esr.diagnosis.nil?

        skycases.push(esr)
      end

      cases +=1
      skycase = line.sub(/SkyCase /, "")
      skycase.sub!(/^\"\s?/, "").sub!("\"", ":")
      esr = EasySkyRecord.new()
      esr.case = skycase
    end

    if line.match(/^s?diagnosis/)
      diag = line.sub(/diagnosis/, "")
      esr.diagnosis = diag
    end

    if line.match(/^\s?stage/)
      stage = line.sub(/stage/, "")
      esr.stage = stage
    end

    # not using these right now
    skycell = line.sub(/SkyCell/, "") if line.match(/SkyCell/)
    if line.match(/Karyotype/)
      karyotype = line.sub(/Karyotype/, "")
      esr.karyotype = karyotype
    end

    if line.match(/^NormalChromosome/)
      line.sub!(/NormalChromosome/, "")
      (chr, normal_cnt, karyotype_cnt) = line.split(" ")
      esr.add_normal(chr, karyotype_cnt) if karyotype_cnt.to_i > 0
    end

    # abnormal
    if line.match(/^SkyChromosome/)
      skychrs +=1
      line.sub!(/SkyChromosome/, "")
      (chr, karyotype_cnt) = line.split(" ")
      current_chr = chr
    end

    if line.match(/^SkyFrag/)
      line.sub!(/SkyFrag/, "")
      frag_info = line.split(" ")
      #puts "#{current_chr} #{frag_info[1..3].join(", ")}" if current_chr.eql? '21'
      (parent_chr, band_start, band_end) = frag_info[1..3]
      gene = frag_info[-1] if frag_info.length.eql?(7)

      fragment = ChromosomeFragment.new({:parent => parent_chr, :from => band_start, :to => band_end})
      fragment.add_gene(gene) if gene

      esr.add_fragment(current_chr, fragment)
    end
  end
  skycases.push(esr) # get last case
  records[entry] = skycases
end

puts "Writing records..."


# karyotypes
records.each_pair do |k, records|
    File.open("#{kdir}/#{File.basename(k, '.esi')}.karyotype", 'w') { |f|
      f.write("Case\tDiagnosis\tStage\tKaryotype\n")
      records.each do |r|
        if r.karyotype
          puts "#{r.case}\t#{r.diagnosis}\t#{r.stage}\t#{r.karyotype}\n"
          f.write "#{r.case}\t#{r.diagnosis}\t#{r.stage}\t#{r.karyotype}\n"
        end
      end
    }
end


records.each_pair do |k, records|
  File.open("#{bpdir}/#{File.basename(k, '.esi')}.txt", 'w') { |f|
    f.write "Case\tDiagnosis\tStage\tDerivativeChr\tFragChr\tFrom\tTo\n"
    records.each do |r|
      next if r.case.match(/mouse/)
      info = "#{r.case}\t#{r.diagnosis}\t#{r.stage}\t"
      next if r.fragments.empty?
      r.fragments.each_pair do |chr, frags|
        frags.each do |frag|
          f.write "#{info}\t#{chr}\t#{frag.parent}\t#{frag.from}\t#{frag.to}\n"
        end
      end
    end
  }
end


File.open("#{bpdir}/breakpoints.txt", 'w') { |f|
  f.write "Case\tDiagnosis\tStage\tDerivativeChr\tFragChr\tFrom\tTo\n"
  records.each_pair do |k, records|
    records.each do |r|
      next if r.case.match(/mouse/)
      info = "#{r.case}\t#{r.diagnosis}\t#{r.stage}\t"
      next if r.fragments.empty?
      r.fragments.each_pair do |chr, frags|
        frags.each do |frag|
          f.write "#{info}\t#{chr}\t#{frag.parent}\t#{frag.from}\t#{frag.to}\n"
        end
      end
    end
  end
}





