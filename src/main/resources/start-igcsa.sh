PPATH=`pwd`

if [ $1 ]; then
  PPATH=$1
fi


time java -Dprops.path=${PPATH} -jar -Xms256m -Xmx1024m igcsa-jar-with-dependencies.jar