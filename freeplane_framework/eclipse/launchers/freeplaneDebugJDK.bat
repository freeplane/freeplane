@echo off
cd ..\..\BUILD
set freeplanedir=%cd%\
set xargs=init.xargs
set defines= "-Dorg.freeplane.param1=%~1" "-Dorg.freeplane.param2=%~2" "-Dorg.freeplane.param3=%~3" "-Dorg.freeplane.param4=%~4" "-Dorg.freeplane.param4=%~5" "-Dorg.freeplane.param4=%~6" "-Dorg.freeplane.param4=%~7" "-Dorg.freeplane.param4=%~8" "-Dorg.freeplane.userfpdir=%appdata%\Freeplane"
set resdir="-Dorg.freeplane.globalresourcedir=%freeplanedir%resources/"
@echo on
"C:\Program Files\Java\jdk1.8.0_60\bin\java" -Xdebug -Xrunjdwp:transport=dt_socket,address=1044,server=y,suspend=y -Xmx512m -jar freeplanelauncher.jar
