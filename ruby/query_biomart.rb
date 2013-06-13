require 'fileutils'
require 'biomart'
require 'yaml'


biomart = Biomart::Server.new("http://www.ensembl.org/biomart")
hsgene = biomart.datasets['hsapiens_gene_ensembl']


filters = {
    'chromosome_name' => 1,
    'chromosomal_region' => ['1:28000000'], #p36
    #'chromosomal_region' => ['28000001:34600000'], #p35
    'status' => ["KNOWN"]
}

attributes = [
    'ensembl_gene_id',
    'external_gene_id',
    'gene_biotype',
    'start_position',
    'end_position',
    'band'
]


results = hsgene.search(
    :filters => filters,
    :attributes => attributes,
    :process_results => true
).select! { |e| e['gene_biotype'].eql? 'protein_coding' }

unless results.nil?
  puts results.length
else
  puts "No results"
end


