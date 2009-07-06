@echo off
set freeplanedir=%~dp0
set fwdir=%freeplanedir%fwdir
set xargs=restart.xargs
if not exist %fwdir% set xargs=init.xargs
set defines= "-Dorg.freeplane.param1=%1" "-Dorg.freeplane.param2=%2" "-Dorg.freeplane.param3=%3" "-Dorg.freeplane.param4=%4"
@echo on
java "-Dorg.osgi.framework.dir=%fwdir%" "-Dorg.knopflerfish.gosg.jars=reference:file:%freeplanedir%plugins/" %defines% -jar "%freeplanedir%framework.jar" -xargs "%freeplanedir%props.xargs" -xargs "%freeplanedir%%xargs%"
