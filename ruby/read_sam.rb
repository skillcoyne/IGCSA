require 'yaml'
## ONLY FOR SMALL SAM FILES


dir="#{Dir.home}/Dropbox/Private/Work/Work Journal/Karyotype-Sequence Comparison"


seqs = Hash.new
Dir["#{dir}/*.sam"].each do |file|

  fout = File.open("#{File.dirname(file)}/#{File.basename(file, '.sam')}.reads.txt", 'w')
  fout.puts ['read', 'pos', 'qual', 'slen'].join("\t")

  puts file
  File.open(file, 'r').each_line do |line|

    line.chomp!

    next if line.start_with? "@"
    read_data = line.split("\t")

    read_name = read_data[0]
    position = read_data[3]
    mapq = read_data[4]

    seqs.has_key?read_data[9]? seqs[read_data[9]] = 1: seqs[read_data[9]] += 1

    qual = 0
    read_data[10].each_byte{|b| qual += (b-33) }

    fout.puts [read_name, position, qual, read_data[9].length].join("\t")

  end

  fout.flush
  fout.close
end

# File.open("#{dir}/seqs.txt", 'w') { |f|  seqs.map{|k,v| f.puts [k,v].join("\t") }  }


