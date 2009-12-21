@echo off
set freeplanedir=%~dp0
set datadir= %freeplanedir%Data
if not exist %datadir% mkdir %datadir%
set defines= "-Dportableapp=true" "-Duser.home=%datadir%"
set xargs=init.xargs
set defines= %defines% "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4"
echo %defines%
@echo on
javaw.exe -Xmx512m "-Dorg.knopflerfish.framework.bundlestorage=memory" "-Dorg.knopflerfish.gosg.jars=reference:file:%freeplanedir%core/" %defines% -jar "%freeplanedir%framework.jar" -xargs "%freeplanedir%props.xargs" -xargs "%freeplanedir%%xargs%"