

hadoop = "/Users/sarah.killcoyne/Tools/hdmk/hadoop"

File.open("/tmp/sims.txt", 'r').each_line do |line|
  line.chomp!

puts(line)
  (chrA,bandA,bstart,chrB,bandB,bend) = line.split("\t")

  bstart = bstart.to_i
  bend = bend.to_i





  #cmd = <<CMD
#{hadoop}/bin/hadoop jar ~/workspace/IGCSA/hbase-genomes/target/HBase-Genomes-1.2.jar minichr \
#-o /tmp -g GRCh37 \
#-band #{chrA}#{bandA} -band #{chrB}#{bandB} -n PatientBPs
#CMD

  # -n SimReads -l #{chrA}:#{bstart-800}-#{bstart} -l #{chrB}:#{bend-800}-#{bend} \

  puts cmd

#  `#{cmd}`

end


