#!/usr/bin/env ruby



def get_read_info(fh)
  # sequence
  sequence = fh.gets.chomp
  # skip '+' line
  fh.gets
  # quality info for the sequence
  qual = fh.gets.chomp
  return( {:sequence => sequence, :quality => qual} )
end


#art_path=ARGV[0]
#ref=ARGV[1]
art_path = "tools/art_illumina"
ref = "ref/PatientOne.fa"
fastq="/tmp/art"

$stderr.puts "ART path: #{art_path}\nREF path: #{ref}"

exit(2) if art_path.nil? or art_path.length <= 0

  cmd = "#{art_path} -p -l 36 -f 3 -m 400 -s 10 -na -i #{ref} -o #{fastq} "
  $stderr.puts cmd


  output = `#{cmd}`
  $stderr.puts "art: #{output} #{$?}"

  unless $?.success?
    $stderr.puts "Failed to run art, error #{$?}.  #{cmd}"
    exit(-1)
  else
    read_files = Dir.glob("#{fastq}*.fq")
    $stderr.puts read_files

    f1 = File.open(read_files[0])
    f2 = File.open(read_files[1])


    while line1 = f1.gets and line2 = f2.gets
      line1.chomp!; line2.chomp!

      if line1 =~ /^@\w+.*\/\d/
        read_name = line1.sub(/\/\d+$/, "")
        # double check
        unless read_name.eql? line2.sub(/\/\d+$/, "")
          $stderr.puts "Reads are not mate-pairs: #{line1}, #{line2}"
          exit(-1)
        end

        read1_info = get_read_info(f1)
        read2_info = get_read_info(f2)

        puts [read_name, read1_info[:sequence], read1_info[:quality], read2_info[:sequence], read2_info[:quality] ].join("\t")

      end
    end

  end


