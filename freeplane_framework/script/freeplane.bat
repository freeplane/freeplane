@echo off
set installdir=%cd%
if not exist %installdir%\init.xargs goto error
set fwdir=%USERPROFILE%\freeplane\fwdir
set xargs=restart.xargs
if not exist %fwdir% set xargs=init.xargs

set defines= "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4"
java -Dorg.osgi.framework.dir=%fwdir% %defines% -jar framework.jar -xargs %xargs%
goto end

:error
echo this batch file nust be run from installation directory
:end