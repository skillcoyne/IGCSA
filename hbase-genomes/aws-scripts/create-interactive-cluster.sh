

MASTER="--instance-group master --instance-type m1.large --instance-count 1 --bid-price 0.07"
CORE="--instance-group core --instance-type m1.large --instance-count 3 --bid-price 0.07"

ruby $EMR_HOME/elastic-mapreduce --create  --alive --region eu-west-1 --name "Interactive test cluster" --ami-version 2.4.2  --enable-debugging --log-uri s3://insilico/logs \
--set-termination-protection false --key-pair amazonkeypair $MASTER $CORE  \
