:LOOP
java -Xmx512m -Dorg.freeplane.userfpdir="%APPDATA%\\Freeplane" -jar freeplanelauncher.jar %*
IF %ERRORLEVEL% NEQ 194 (
  GOTO COMPLETED
)
GOTO LOOP
:COMPLETED
