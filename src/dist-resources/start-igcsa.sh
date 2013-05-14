PPATH=`pwd`


time java -Dprops.path=${PPATH} -jar -Xms1024m -Xmx2g ${project.artifactId}-${project.version}.jar $@


