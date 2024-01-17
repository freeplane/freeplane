java ^
-Xms20m -Xmx1g -Dorg.freeplane.userfpdir="%APPDATA%\\Freeplane" ^
-cp "freeplanelauncher.jar;framework.jar" ^
org.freeplane.launcher.JavaLauncher ^
%*
