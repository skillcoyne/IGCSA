require 'yaml'

$EVENTS = {}


def parse_events(karyotype, events)
  aberrations = karyotype.split(",")

  aberrations.each_with_index do |a, i|
    next if i <= 1
    a.gsub!(/\s/, "")
    a.gsub!(/\[\d+\]/, "")
    next if (a.length <= 1 or a.match(/\?/))

    a.sub!(/^[\+|-]/, "") unless a.match(/^[\+|-][\d|X|Y]+$/) # +t(13;X)(q13;p12) doesn't need a +

    # 13x2 is normal, 13x3 is a duplicate and should read +13
    if a.match(/^([\d+|X|Y]+)x(\d+)/)
      chr = $1; dups = $2.to_i
      if dups.eql? 0
        a = "-#{chr}"
        events[a] = 0 unless events.has_key? a
        events[a] += 2
      elsif dups > 2
        dups -= 2
        a = "+#{chr}"
        events[a] = 0 unless events.has_key? a
        events[a] += dups
      end
      # add(7)x2 should read +7, presume 'add' does not indicate normal diploid even when x2
    elsif a.match(/^add\(([\d|X|Y]+)\)x(\d+)/)
      chr = $1; dups = $2.to_i
      a = "+#{chr}"
      events[a] = 0 unless events.has_key? a
      events[a] += dups
      # add(9)(p21)x2 should indicate that this happened twice
    elsif a.match(/(.*)x(\d+)$/)
      a = $1; dups = $2.to_i
      events[a] = 0 unless events.has_key? a
      events[a] += dups
      # del(7) should be -7  but not del(7)(q12)
    elsif a.match(/^del\(([\d|X|Y]+)\)$/)
      chr = $1
      a = "-#{chr}"
      events[a] = 0 unless events.has_key? a
      events[a] += 1
    else # everything else
      events[a] = 0 unless events.has_key? a
      events[a] += 1
    end
  end
  #k_to_a =

  return events
end




if ARGV.length <= 0
  print "Root directory for ESI/cam karyotypes required"
  exit
end
dir = ARGV[0]




#db = Mysql2::Client.new(:host => "localhost", :username => "root", :database => "cancer_karyotypes")



matrix = []



ktsql = File.open("#{dir}/sql/karyotypes.txt", 'w')
src = 'mitelman'
uid_kt = 1
## mitelman karyotypes
File.open("#{dir}/mm-karyotypes.txt").each_line do |karyotype|
  karyotype.chomp!
  next if karyotype.start_with? "#"
  events = parse_events(karyotype, events)
  ktsql.write("#{uid_kt}\t#{src}\t#{karyotype}\n")
  uid_kt += 1
end

exit


## NCBI sky-fish karyotypes
esidir = "#{dir}/ESI/karyotype"
src = 'ncbi'
Dir.foreach(esidir) do |entry|
  file = "#{esidir}/#{entry}"
  next if entry.start_with?(".")
  next if File.directory?(file)

  unless (File.basename(entry).match(/\.karyotype/) or File.basename(entry).match(/\.kt/))
    puts "#{entry} is not a karyotype file"
    next
  end

  kts = 0
  File.open(file, 'r').each_line do |line|
    line.chomp
    next if line.length <= 0
    next if line.match(/mouse/)
    next if line.match(/Case/) # column names
    karyotype = line.split(/\t/)[-1].gsub!(/\s/, "")

    events = parse_events(karyotype, events)

    ktsql.write("#{uid_kt}\t#{src}\t#{karyotype}\n")
    uid_kt += 1
  end
end

puts events.keys.length

## Cambridge karyotypes
camdir = "#{dir}/path.cam.ac.uk"
src = 'cam'
Dir.foreach(camdir) do |cd|
  ktdir = "#{camdir}/#{cd}"
  next if cd.start_with?(".")
  next unless File.directory? ktdir

  Dir.foreach(ktdir) do |entry|
    next if entry.start_with?(".")
    file = "#{ktdir}/#{entry}"

    unless (File.basename(entry).match(/\.karyotype/) or File.basename(entry).match(/\.kt/))
      puts "#{entry} is not a karyotype file"
      next
    end

    File.open(file, 'r').each_line do |karyotype|
      karyotype.chomp!
      events = parse_events(karyotype, events)
      ktsql.write("#{uid_kt}\t#{src}\t#{karyotype}\n")
      uid_kt += 1
    end
  end
end

ktsql.close

#puts events.keys.join("\n")
puts events.keys.length


File.open("#{dir}/events.txt", 'w') { |f|
  f.write("event\tfrequency\n")
  events.each_pair { |k, v| f.write("#{k}\t#{v}\n") }
}



