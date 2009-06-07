set defines= "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4" 
java -Dorg.osgi.framework.dir=%USERPROFILE%/freeplane/fwdir %defines% -jar framework.jar -init -xargs init.xargs
