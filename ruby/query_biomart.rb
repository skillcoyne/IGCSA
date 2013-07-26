require 'fileutils'
require 'biomart'
require 'yaml'


biomart = Biomart::Server.new("http://www.ensembl.org/biomart")
hsgene = biomart.datasets['hsapiens_gene_ensembl']


filters = {
    'chromosome_name' => 2,
    #'chromosomal_region' => ['1:28000000'], #p36
    #'chromosomal_region' => ['28000001:34600000'], #p35
    'status' => ["KNOWN"]
}

attributes = [
    'ensembl_gene_id',
    'ensembl_peptide_id',
    'gene_biotype',
]


results = hsgene.search(
    :filters => filters,
    :attributes => attributes,
    :process_results => true
).select! { |e| e['gene_biotype'].eql? 'protein_coding' }

unless results.nil?

  puts results.map{|e| e['ensembl_gene_id']}.uniq.length

  puts results.map{|e| e['ensembl_peptide_id']}.uniq.length


  puts results.length
#  puts YAML::dump results
else
  puts "No results"
end


