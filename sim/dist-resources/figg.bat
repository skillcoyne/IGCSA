set PPATH=%CD%

java -Dprops.path=%PPATH% -jar -Xms512m -Xmx1g ${build.finalName}-${project.version}.jar -f %*