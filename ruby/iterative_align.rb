require 'yaml'
require 'mysql2'


# 1. Generate mini aberrations
# 2. Index & Align
# 3. Score
# 4. Generate neighboring aberrations & ...?
# 5. Repeat 2-4 until??

def special_generator_job()

  cmd = <<JOB
hadoop jar HBase-Genomes-1.2.jar SpecialGenerator -o /tmp/special -b #{band_pairs.join(";")}
JOB



end


