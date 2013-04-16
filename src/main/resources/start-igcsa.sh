PPATH=`pwd`

# For simplicity assuming that the required properties files are in the current directory.
#if [ $1 ]; then
#  PPATH=$1
#fi

time java -Dprops.path=${PPATH} -jar -Xms256m -Xmx1024m igcsa-jar-with-dependencies.jar $@


