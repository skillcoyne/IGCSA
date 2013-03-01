require 'biomart'
require 'yaml'


biomart = Biomart::Server.new("http://www.ensembl.org/biomart")
hsgene = biomart.datasets['hsapiens_gene_ensembl']


#results = hsgene.search(
#    :filters => {'chromosome_name' => 10, 'chromosomal_region' => ["10:101000-10:140000"], 'status' => ["KNOWN"]},
#    :attributes => ['ensembl_gene_id', 'start_position', 'end_position', 'percentage_gc_content', 'gene_biotype'],
#    :process_results => true
#).select!{|e| e['gene_biotype'].eql?'protein_coding'}


dir = "#{Dir.home}/Data/VariationNormal"

threads = []
Dir["#{dir}/chr*/dist2.txt"].each do |file|
  puts file
  threads << Thread.new(file) {
    chr = File.basename(File.dirname(file))
    chr.sub!("chr", "")
    Thread.current['message'] = "Writing #{File.dirname(file)}/genes-dist2.txt"
    Thread.current['chr'] = chr
    Thread.current['errors'] = []

    total = 0; positions = {}; counts = []
    # Write each chromosome separately for easier analysis later
    fout = File.open("#{File.dirname(file)}/genes-dist2.txt", 'w')
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
				Thread.current['errors'] << "Error reading #{file}: #{e.message}"
			end
      sleep 3 if i%20 == 0 # just in case ensembl gets annoyed
    end
    counts.uniq!
    counts.sort!

    positions.each_pair do |pos, genes|
      fout.write [chr, pos, genes.join(","), genes.size, total, counts.first].join("\t") + "\n"
    end

    fout.close
    Thread.current['total'] = total
  }
end

threads.each { |t|
  t.join; 
  puts [t['message'], ':', t['total']].join("\t")
  puts t['errors'].join("\n") unless t['errors'].empty?
}







