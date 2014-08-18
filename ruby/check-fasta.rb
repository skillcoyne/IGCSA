

fileA = ARGV[0]
fileB = ARGV[1]


f1 = File.open(fileA, 'r')
f2 = File.open(fileB, 'r')

hA = f1.gets
hB = f2.gets

unless hA.start_with?">" and hB.start_with?">"
  $stderr.puts "fasta file missing header"
  exit -1
end

count = 1
buffA, buffB = "", ""
while a = f1.getc

  buffA += a unless a =~ /\s/

  if buffA.size.eql?1000 or f1.eof?
    puts "#{count} #{f1.eof?}"

    while buffB.size < 1000 and !f2.eof?
      b = f2.getc
      buffB += b unless b =~ /\s/
    end

    # compare
    unless buffA == buffB
      $stderr.puts "Fuck"
      puts buffA
      puts "---------------"
      puts buffB
      exit -1
    end

    # reset
    buffA, buffB = "", ""
    count += 1
  end

end

