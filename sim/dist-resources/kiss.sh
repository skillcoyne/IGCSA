## ONLY for running the fragment generator

PPATH=`pwd`


time java -Dprops.path=${PPATH} -jar -Xms1024m -Xmx2g ${build.finalName}-${project.version}.jar -s 2 $@
