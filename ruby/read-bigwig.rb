
require 'yaml'
require_relative 'lib/bands'


bands = Bands.new("#{Dir.home}/Analysis/band_genes.txt")


fout = File.open("#{Dir.home}/Analysis/band_coverage.txt", 'w')
fout.puts ['chr','band','coverage', 'mean','sd'].join("\t")
bands.chr_hash.each_pair do |chr, bds|

  bds.each_pair do |band, loc|

    coverage = `./bigWigSummary -type=coverage ~/Downloads/wgEncodeCrgMapabilityAlign100mer.bigWig chr#{chr} #{loc.begin} #{loc.end} 1`
    mean = `./bigWigSummary -type=mean ~/Downloads/wgEncodeCrgMapabilityAlign100mer.bigWig chr#{chr} #{loc.begin} #{loc.end} 1`
    std = `./bigWigSummary -type=std ~/Downloads/wgEncodeCrgMapabilityAlign100mer.bigWig chr#{chr} #{loc.begin} #{loc.end} 1`

    if $?.to_i == 0
      fout.puts [chr, band, coverage.chomp, mean.chomp, std.chomp].join("\t")
    else
      fout.puts [chr, band, 'NA','NA','NA'].join("\t")
    end

  end
end
fout.close
