set PPATH=%CD%

time java -Dprops.path=%PPATH% -jar -Xms1024m -Xmx2g ${build.finalName}-${project.version}.jar -f %*