require 'yaml'
require 'mysql2'


# 1. Generate mini aberrations
# 2. Index & Align
# 3. Score
# 4. Generate neighboring aberrations & ...?
# 5. Repeat 2-4 until??

HADOOP = "/Users/sarah.killcoyne/Tools/hd/hadoop/bin/hadoop"
JAR = "/Users/sarah.killcoyne/workspace/IGCSA/hbase-genomes/target/HBase-Genomes-1.2.jar"
BWA = "/bwa-tools/bwa.tgz"

def run_cmd(cmd)
  puts cmd

  output = `#{cmd}`
  unless $?.success?
    $stderr.puts "#{cmd}\nCommand failed: #{output}"
    exit $?
  else
    puts "#{cmd} finished."
  end

end


def mini_abrs(locations, path, name)
  cmd = <<JOB
#{HADOOP} jar #{JAR} minichr -o #{path} -p GRCh37 -n #{name} -l #{locations.join(",")}
JOB

  run_cmd(cmd)

end


def bwa_index(path, name)
  cmd = <<JOB
#{HADOOP} jar #{JAR} index -b #{BWA} -p #{path}/#{name}
JOB

  run_cmd(cmd)
end


def bwa_align(index, tsv_reads)
  cmd = <<JOB
#{HADOOP} jar #{JAR} align -b #{BWA} -i #{index} -r #{tsv_reads}
JOB

  run_cmd(cmd)
end


#mini_abrs(["1:3201-5800"], "/tmp/special", "PatientX")
#bwa_index("/tmp/special", "PatientX")
bwa_align("/tmp/special/PatientX/index", "/reads/mini.tsv")


