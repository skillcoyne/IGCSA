require_relative 'lib/variations'
require_relative 'lib/cytogenetic_variation'

def recursive_translocation(str, last_chr, last_band, fragments)
  puts "*-* #{str} *-*"
  chrs = str[str.index(/\(/, 0)+1..str.index(/\)/, 0)-1].split(/;|:/)

  # just a check
  unless last_chr.eql?(chrs[0])
    warn "Chromosomes don't match"
  end

  band_start = str.index(/\)\(/, 0)+1
  band_end = str.index(/\)/, band_start)

  bands = str[band_start+1..band_end-1].split(/;|:/)

  fragments.push(["#{last_chr}#{last_band}", "#{chrs[0]}#{bands[0]}"])
  fragments.push(["#{chrs[0]}#{bands[0]}", "#{chrs[1]}#{bands[1]}"])

  next_index = str.index(/t\(/, str.index(/;|:/))
  unless next_index.nil?
    recursive_translocation(str[next_index..str.length], chrs[1], bands[1], fragments)
  else
    return fragments
  end
end

# sliding window after the first two descriptions
karyotype = "48-60,X[12],der(X)del(X)(p11.1)del(X)(q22)[12],+der(X)t(X;8)(q28;q23)t(8;21)# (q24.3;q?)(CMYC+)t(8;21)( q24.3;q?)(CMYC+)t(8;21)(q?;q?)(CMYC-)[11],-Y[16],# trc(1;1;1)(qter;qterp11;p11)del(1)(q11)[16],# der(1)(20?->20?::8q24.3::15?::12?::1p32->1q25::13q14->13qter)(CMYC+)[15],# +der(1)del(1)(p32)t(1;13)(q11;q?)del(13)(q?)[11],+der(1)**[10],del(2)(p23)[13],# +der(2)t(1;2)(p12;q11.2)t(1;2)(p?;?)[16],+der(2)del(p23)t(2;6)(q21;p15)[10],# +der(2)t(1;6;2) **[5],-3,der(3)(3pter->3q22::q13.1->qter)[15],# der(4)t(4;20)9q26;?)t(7;20)(q22;?)[8],+der(4)t(4;10)(q21;?)[6],+frag(4)[7],del(6)(p11.2)[10],der(6)del(6)(p11.2)t(1;6)(p32;q23)[1],+der(6)t(1;6)(q32;q23)[9],+der(6)t(4;6)(q31.1;p21.3)[12],+der(6)t(2;6)(q12;p12)t(1;6)(p22;q21)[15],+der(6)del(6)(p11.2)t(6;17)# (q13;q21)t(8;17)(q13;q23)(CMYC+)[6],+frag(6)[6],der(7)t(7;20)(p22;p11.2)[14],# +der(7)t(12;7)(?;p22)[5],del(8)[3],+der(8)t(8;12)**[4],del(9)(p13)[8],+der(9)del(9)(p23)del(9)(q34.1)[8],+der(9)t(9;19)(q34.3;q13.1)[5],del(10)**[5],+der(10)t(5;10)(?;p13)[12],# der(11)t(4;11)(q27;p15)[12],der(12)t(12;17)(p11.2;q21)[12],der(12)t(3;12)(q22;q24.3)# [10],n13[4],del(13)(q22q32)[2],+der(13)del(13)(q13)t(1;13)(p?;q13)t(1;13)(?;?)[13],+ins(13;1)(q13;q12q32)[1],n14[9],der(14)del(X)del(13)(q12)t(13;14)(p11.2;q34)[12],+der(14)del(13)(q12)t(13;14)(p11.2;q34)[13],del(15)(q21q22)[12],n16[6],del(16)(p?)[8],+der(16)t(12;16)(?;q22)[4],+der(16)t(16;17)(q22;q21)[9],n18[9],n19[11],der(19)t(3;19)# (q21;q13.3)[13],+der(19)del(X)(p22.1)t(X;19)(q28;p13.1)dup(19)(q13.3q13.1)[8],n20[6],der(20)t(6;20)(q13.2;?)[9],+der(20)del(20)(p11.2)t(6;20)(?;q13.2)[10],+der(20)del(20)(p11.2)t(7;20)(q22;q13.2)[9],n21[14],der(21)t(8;21)(?;p11.2)[4],+der(21)del(21)(q22)t(8;21)(?;p11.2)[7],n22[13],del(22)[9],+der(22)t(9;22)**[8],+der(22)t(5;22)(p13;p11.2)[6]#"

#karyotype = "46,XY,-Y,der(X)del(X)(p11.1)del(X)(q22)[12],+der(X)t(X;8)(q28;q23)t(8;21)(q24.3;q22)"

chromosomes = Hash.new
(Array(1..23)|['X', 'Y']).each do |c|
  chromosomes[c.to_s] = Chromosome.new(c)
end

# Clean: don't need to know number of cells
karyotype = karyotype.gsub(/\[\d+\]/, "")

karyotype.split(",").each_with_index do |abnormality, index|

  puts "#{index}: #{abnormality}"

  # not going to bother with the uncertain ones
  if abnormality.start_with?("#")
    puts "-- UNCERTAINTY --"
    puts abnormality
    puts "---------"
    next
  end

  if abnormality.match(/\*\*/)
    puts "--- Ignoring ** for now ---"
    puts abnormality
    puts "---------"
    next
  end

  if index.eql?(0)
    modal = abnormality
    next
  end

  if index.eql?(1)
    gender = abnormality
    next
  end

  i = 0
  while  i < abnormality.length
    puts "*** #{abnormality} ***"

    ## Add/subtract chromosome
    if abnormality[i].match(/\+|-/) and abnormality[i+1].match(/\d+|X|Y/)
      chr = abnormality[i+1..abnormality.length]
      puts "Add/Sub #{chr}"
      (abnormality[i].match(/-/)) ? (chromosomes[chr].loss) : (chromosomes.gain)
      i = abnormality.length

      ## insertion
    elsif abnormality[i..abnormality.index(/\(/)].match(/ins/)
      chr = abnormality[abnormality.index(/\(/, i+1)+1..abnormality.index(/\)/, i+1)-1]
      puts "Insertion #{abnormality[i..abnormality.length]}"
      i = abnormality.length

      ## duplication
    elsif abnormality[i..abnormality.index(/\(/)].match(/dup/)
      chr_e = abnormality.index(/\)/, i+1)
      chr = abnormality[abnormality.index(/\(/, i+1)+1..chr_e-1]
      bands = abnormality[chr_e..abnormality.index(/\)/, chr_e+1)]
      puts "Duplication #{chr} #{bands}"
      i = abnormality.length

      ## ring chromosome
    elsif abnormality[i..abnormality.index(/\(/, i+1)].match(/r/) and !abnormality[i..abnormality.index(/\(/, i+1)-1].match(/der/)
      chr = abnormality[abnormality.index(/\(/, i+1)+1..abnormality.index(/\)/, i+1)-1]
      puts "Ring chromosome #{chr}"

      ## derivative chromosome
    elsif (abnormality[i].match(/\+/) and abnormality[i..abnormality.index(/\(/, i+1)-1].match(/der/)) or
        (abnormality[i..abnormality.index(/\(/, i+1)-1].match(/der/))
      chr = abnormality[abnormality.index(/\(/, i+1)+1..abnormality.index(/\)/, i+1)-1]
      puts "Derivative chromosome #{chr}"
      i = abnormality.index(/\)/, i+1)

      ## partial deletion
    elsif abnormality[i..abnormality.index(/\(/, i)].match(/del/)
      chr_e = abnormality.index(/\)/, i+1)
      chr = abnormality[abnormality.index(/\(/, i+1)+1..chr_e-1]

      band_e = abnormality.index(/\)/, chr_e+1)
      band = abnormality[chr_e+2..band_e-1]

      puts "Partial deletion '#{chr}' '#{band}'"
      chromosomes[chr].delete_band(band)
      i = band_e+1

      ## translocation
    elsif abnormality[i].match(/t/) and abnormality[i+1].match(/\(/)
      puts "Translocation #{abnormality[i..abnormality.length]}"
      chr = abnormality[abnormality.index(/\(/, i)+1..abnormality.index(/;|:/)-1]
      puts "Chromosome #{chr}"
      fragments = recursive_translocation(abnormality[i+1..abnormality.length], chr, "qter", Array.new)
      chromosomes[chr].set_fragments(fragments)
      i = abnormality.length
    else
      i = i+1
    end
  end
end





