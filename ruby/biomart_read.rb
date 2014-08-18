require 'fileutils'
require 'biomart'
require 'yaml'


biomart = Biomart::Server.new("http://www.ensembl.org/biomart")
hsgene = biomart.datasets['hsapiens_gene_ensembl']


if ARGV.length < 3
  puts("Usage: #{$0} <genes directory> <high low median> <chromosome list>")
  exit()
end

dir = ARGV[0]
dist = ARGV[1]
chrs = ARGV[2..ARGV.length]


#results = hsgene.search(
#    :filters => {'chromosome_name' => 10, 'chromosomal_region' => ["10:101000-10:140000"], 'status' => ["KNOWN"]},
#    :attributes => ['ensembl_gene_id', 'start_position', 'end_position', 'percentage_gc_content', 'gene_biotype'],
#    :process_results => true
#).select!{|e| e['gene_biotype'].eql?'protein_coding'}

if chrs.length.eql?1 and chrs[0].eql?'all'
  chrs = ("1".."22").to_a
  chrs.push('X')
  chrs.push('Y')
  puts chrs
end
puts "Running chromosomes: #{chrs.join(',')}"

gene_dir = "#{dir}/#{dist}/genes"
FileUtils.mkpath(gene_dir) unless File.exists?gene_dir


threads = []
chrs.each do |chr|
  file = "#{dir}/#{dist}/fragments/chr#{chr}.txt"
  puts file
  threads << Thread.new(file, dir, dist, chr) {
    outfile = "#{dir}/#{dist}/genes/chr#{chr}-genes.txt"

    Thread.current['message'] = "Writing #{outfile}"
    Thread.current['chr'] = chr

    total = 0; positions = {}; counts = []
    ferr = File.open("#{dir}/#{dist}/#{chr}-genes.err", 'w')

    # Write each chromosome separately for easier analysis later
    fout = File.open(outfile, 'w')
    fout.write ['Chr', 'Position', 'Genes', 'Total.Genes', 'Total.SNVs', 'Max.Count'].join("\t") + "\n"

    File.open(file, 'r').each_with_index do |line, i|
      next if i.eql? 0

      line.chomp
      (pos, snv, del, indel, ins, alt, sub, trep, gc, unk, bps, gcratio, unkratio) = line.split("\t")
      snv = snv.to_i; pos = pos.to_i

      counts << snv

      region = "#{chr}:#{pos}-#{chr}:#{pos+1000}"
      #puts region
      begin
      	results = hsgene.search(
          :filters => {'chromosome_name' => chr, 'chromosomal_region' => [region], 'status' => ['KNOWN']},
          :attributes => ['ensembl_gene_id', 'start_position', 'end_position', 'percentage_gc_content', 'gene_biotype'],
          :process_results => true)
      	genes = []
      	if (results.size > 0)
        	results.select! { |e| e['gene_biotype'].eql? 'protein_coding' }
        	genes = results.map { |e| e['ensembl_gene_id'] }
      	end
      	positions[pos.to_s] = genes

      	total += 1
			rescue Exception => e
				ferr.write "Error reading #{file}: #{e.message}\n"
			end
      sleep 3 if i%20 == 0 # just in case ensembl gets annoyed
    end
    counts.uniq!
    counts.sort!

    positions.each_pair do |pos, genes|
      fout.write [chr, pos, genes.join(","), genes.size, total, counts.first].join("\t") + "\n"
    end

    fout.close
    ferr.close
    Thread.current['total'] = total
  }
end

threads.each { |t|
  t.join; 
  puts [t['message'], ':', t['total']].join("\t")
}







