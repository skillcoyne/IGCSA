require 'yaml'
require_relative 'lib/sky_karyotype'


#s = "p22q11"
##(p|q)\d+
#puts s.scan(/([p|q]\d+[\.\d]?)/)
#exit

karyotype = "48,XX,+5,inv(9)(p22q22),+21"

#karyotype = karyotype = "48-60,X[12],der(X)del(X)(p11.1)del(X)(q22)[12],+der(X)t(X;8)(q28;q23)t(8;21)# (q24.3;q?)(CMYC+)t(8;21)( q24.3;q?)(CMYC+)t(8;21)(q?;q?)(CMYC-)[11],-Y[16],# trc(1;1;1)(qter;qterp11;p11)del(1)(q11)[16],# der(1)(20?->20?::8q24.3::15?::12?::1p32->1q25::13q14->13qter)(CMYC+)[15],# +der(1)del(1)(p32)t(1;13)(q11;q?)del(13)(q?)[11],+der(1)**[10],del(2)(p23)[13],# +der(2)t(1;2)(p12;q11.2)t(1;2)(p?;?)[16],+der(2)del(p23)t(2;6)(q21;p15)[10],# +der(2)t(1;6;2) **[5],-3,der(3)(3pter->3q22::q13.1->qter)[15],# der(4)t(4;20)9q26;?)t(7;20)(q22;?)[8],+der(4)t(4;10)(q21;?)[6],+frag(4)[7],del(6)(p11.2)[10],der(6)del(6)(p11.2)t(1;6)(p32;q23)[1],+der(6)t(1;6)(q32;q23)[9],+der(6)t(4;6)(q31.1;p21.3)[12],+der(6)t(2;6)(q12;p12)t(1;6)(p22;q21)[15],+der(6)del(6)(p11.2)t(6;17)# (q13;q21)t(8;17)(q13;q23)(CMYC+)[6],+frag(6)[6],der(7)t(7;20)(p22;p11.2)[14],# +der(7)t(12;7)(?;p22)[5],del(8)[3],+der(8)t(8;12)**[4],del(9)(p13)[8],+der(9)del(9)(p23)del(9)(q34.1)[8],+der(9)t(9;19)(q34.3;q13.1)[5],del(10)**[5],+der(10)t(5;10)(?;p13)[12],# der(11)t(4;11)(q27;p15)[12],der(12)t(12;17)(p11.2;q21)[12],der(12)t(3;12)(q22;q24.3)# [10],n13[4],del(13)(q22q32)[2],+der(13)del(13)(q13)t(1;13)(p?;q13)t(1;13)(?;?)[13],+ins(13;1)(q13;q12q32)[1],n14[9],der(14)del(X)del(13)(q12)t(13;14)(p11.2;q34)[12],+der(14)del(13)(q12)t(13;14)(p11.2;q34)[13],del(15)(q21q22)[12],n16[6],del(16)(p?)[8],+der(16)t(12;16)(?;q22)[4],+der(16)t(16;17)(q22;q21)[9],n18[9],n19[11],der(19)t(3;19)# (q21;q13.3)[13],+der(19)del(X)(p22.1)t(X;19)(q28;p13.1)dup(19)(q13.3q13.1)[8],n20[6],der(20)t(6;20)(q13.2;?)[9],+der(20)del(20)(p11.2)t(6;20)(?;q13.2)[10],+der(20)del(20)(p11.2)t(7;20)(q22;q13.2)[9],n21[14],der(21)t(8;21)(?;p11.2)[4],+der(21)del(21)(q22)t(8;21)(?;p11.2)[7],n22[13],del(22)[9],+der(22)t(9;22)**[8],+der(22)t(5;22)(p13;p11.2)[6]#"
sk = SkyKaryotype.new

sk.parse(karyotype)
#sk.find_polyploidy(karyotype)
#puts sk.karyotype.keys

sk.karyotype.each_pair do |k,v|
  puts "#{k} #{v.length}"
end


#puts sk.chromosomes.each {|c| puts "#{c.chromosome}" }

#"+der(19)del(X)(p22.1)t(X;19)(q28;p13.1)dup(19)(q13.3q13.1)"
#sk.find_derivatives("+der(19)del(X)(p22.1)t(X;19)(q28;p13.1)dup(19)(q13.3q13.1)")

#sk.find_derivatives("+der(6)t(2;6)(q12;p12)t(1;6)(p22;q21)[15]")
#
#puts YAML::dump sk

