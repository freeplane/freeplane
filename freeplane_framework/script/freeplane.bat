@echo off
set freeplanedir=%~dp0
set xargs=init.xargs
set defines= "-Dorg.freeplane.param1=%~1" "-Dorg.freeplane.param2=%~2" "-Dorg.freeplane.param3=%~3" "-Dorg.freeplane.param4=%~4" "-Dorg.freeplane.param4=%~5" "-Dorg.freeplane.param4=%~6" "-Dorg.freeplane.param4=%~7" "-Dorg.freeplane.param4=%~8" "-Dorg.freeplane.userfpdir=%appdata%\Freeplane"
set resdir="-Dorg.freeplane.globalresourcedir=%freeplanedir%resources/"
@echo on
java -Xmx512m "-Dorg.knopflerfish.framework.bundlestorage=memory" "-Dorg.knopflerfish.gosg.jars=reference:file:%freeplanedir%core/" %resdir% %defines% -jar "%freeplanedir%framework.jar" -xargs "%freeplanedir%props.xargs" -xargs "%freeplanedir%%xargs%"
