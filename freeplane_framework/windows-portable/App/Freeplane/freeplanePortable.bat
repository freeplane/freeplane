@echo off
set freeplanedir=%~dp0
set defines= "-Dportableapp=true" "-Duser.home=%freeplanedir%..\..\Data"
set java_exe=%freeplanedir%..\..\..\CommonFiles\Java\bin\javaw.exe
if not exist "%java_exe%" set java_exe=javaw.exe
set xargs=init.xargs
set defines= %defines% "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4"
set resdir="-Dorg.freeplane.globalresourcedir=%freeplanedir%resources/"
@echo on
%java_exe% -Xmx512m "-Dorg.knopflerfish.framework.bundlestorage=memory" "-Dorg.knopflerfish.gosg.jars=reference:file:%freeplanedir%core/" %resdir% %defines% -jar "%freeplanedir%framework.jar" -xargs "%freeplanedir%props.xargs" -xargs "%freeplanedir%%xargs%"
