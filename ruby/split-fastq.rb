require 'yaml'
require 'fileutils'


def get_read(fh)

  lines = Array.new
  (1..3).each do |i|
    lines << fh.gets.chomp
  end
  return lines
end



dir="/Users/sarah.killcoyne/Data/1000Genomes/Reads/ERX000272/unzipped"

fastq = Dir.glob("#{dir}/*.{fastq,fq,fa}")


read_id = /^.*\/(1|2)$/



f1in = File.open(fastq[0], 'r')
f2in = File.open(fastq[1], 'r')


count = 1

newdir = "/tmp/#{count}"
#newdir = "#{File.dirname(fastq[0])}/#{count}"
FileUtils.mkpath(newdir)

f1outname = "#{newdir}/#{File.basename(fastq[0])}"
f2outname = "#{newdir}/#{File.basename(fastq[1])}"

f1out = File.open(f1outname, 'w')
f2out = File.open(f2outname, 'w')

while line1 = f1in.gets and line2 = f2in.gets
  line1.chomp!; line2.chomp!

  if line1 =~ /^@\w+.*\/\d/
    read_name = line1.sub(/\/\d+$/, "")
    # double check
    unless read_name.eql? line2.sub(/\/\d+$/, "")
      $stderr.puts "Reads are not mate-pairs: #{line1}, #{line2}"
      exit(-1)
    end

    f1out.puts "#{read_name}/1"
    f1out.puts get_read(f1in).join("\n")

    f2out.puts "#{read_name}/2"
    f2out.puts get_read(f2in).join("\n")

    f1out.flush
    f2out.flush


    # 50gb                       21474836480
    #if (File.size(f1outname) >= 53687091200)
    if (File.size(f1outname) >= 21237)
      f1out.close
      f2out.close

      FileUtils.mv(newdir, "#{File.dirname(fastq[0])}/#{count}")

      count += 1

      newdir = "/tmp/#{count}"
      #newdir = "#{File.dirname(fastq[0])}/#{count}"
      FileUtils.mkpath(newdir)

      f1outname = "#{newdir}/#{File.basename(fastq[0])}"
      f2outname = "#{newdir}/#{File.basename(fastq[1])}"

      puts "Opening new files: #{f1outname}\t#{f2outname}"

      f1out = File.open(f1outname, 'w')
      f2out = File.open(f2outname, 'w')
    end

  end

end

f1out.flush
f2out.flush
f1out.close
f2out.close

