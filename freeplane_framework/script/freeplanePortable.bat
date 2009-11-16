@echo off
set freeplanedir=%~dp0
set defines= "-Dportableapp=true" "-Duser.home=%freeplanedir%Data"
set xargs=init.xargs
set defines= %defines% "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4"
@echo on
javaw.exe -Xmx512m "-Dorg.knopflerfish.framework.bundlestorage=memory" "-Dorg.knopflerfish.gosg.jars=reference:file:%freeplanedir%plugins/" %defines% -jar "%freeplanedir%framework.jar" -xargs "%freeplanedir%props.xargs" -xargs "%freeplanedir%%xargs%"
