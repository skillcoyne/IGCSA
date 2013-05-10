PPATH=`pwd`


time java -Dprops.path=${PPATH} -jar -Xms1024m -Xmx2g igcsa-1.0.jar $@


