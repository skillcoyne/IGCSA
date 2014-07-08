if [ ! -f "$EMR_HOME/elastic-mapreduce" ]; then
  echo "EMR_HOME not set."
  exit
fi

BUCKET="s3n://insilico"
MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type m1.large --instance-count 1 --bid-price 0.07"

#ruby $EMR_HOME/elastic-mapreduce --create --alive --region eu-west-1 --name "Interactive ruby setup test" \
#--ami-version 2.4.2  --enable-debugging --log-uri ${BUCKET}/logs \
#--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE \
#--bootstrap-action "s3://insilico/bwa_test/ruby-bootstrap.sh"

ret=`ruby $EMR_HOME/elastic-mapreduce --create --stream --region eu-west-1 --name "Streaming BWA Align test" \
--ami-version 2.4.2  --enable-debugging --log-uri ${BUCKET}/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE \
--cache s3n://insilico/bwa_test/bwa#bwa \
--input s3n://insilico/bwa_test/mini.tsv \
--mapper s3n://insilico/bwa_test/mapper.rb \
--reducer aggregate \
--output s3://insilico/bwa_test/output/alg`

regex='(j-[A-Z0-9]+)'

if [[ $ret =~ $regex ]]; then
  job=$BASH_REMATCH
  echo "Connecting to job $job"
	$EMR_HOME/elastic-mapreduce --ssh $job
fi



