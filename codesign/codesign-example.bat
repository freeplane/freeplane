set PASSWORD=yourPassword
set KEYSTORE=freeplane.pfx
"C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\signtool.exe" sign /tr http://time.certum.pl /f "%~dp0%KEYSTORE%" /p %PASSWORD% "%~dp0..\DIST\*.exe"
pause
