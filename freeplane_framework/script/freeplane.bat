java ^
--add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED ^
--add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED ^
-Xmx1g -Dorg.freeplane.userfpdir="%APPDATA%\\Freeplane" ^
-jar freeplanelauncher.jar %*
