#if [ ! -f \"$EMR_HOME/elastic-mapreduce\" ]; then
#  echo \"EMR_HOME not set.\"
#  exit
#fi

BUCKET="s3n://insilico"
MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.04"
CORE="--instance-group core --instance-type m1.large --instance-count 10 --bid-price 0.04"


cmd="ruby $EMR_HOME/elastic-mapreduce --create --stream --region eu-west-1 --name \"Streaming ART\" \
--ami-version 2.4.2  --enable-debugging --log-uri ${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE \
--cache \"s3n://insilico/tools/art.tgz#tools,s3://insilico/genomes/PatientOne/PatientOne.tgz#ref\" \
--input s3n://insilico/art_input.txt \
--mapper \"s3n://insilico/tools/art-mapper.rb tools/art_illumina ref/PatientOne.fa\" \
--reducer s3n://insilico/tools/direct-output.rb \
--output s3://insilico/genomes/PatientOne/FASTQ"

echo $cmd


#regex='(j-[A-Z0-9]+)'

#if [[ $ret =~ $regex ]]; then
#  job=$BASH_REMATCH
#  echo \"Connecting to job $job\"
#	$EMR_HOME/elastic-mapreduce --ssh $job
#fi



