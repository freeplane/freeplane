set CERT=your certificate owner name
"C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\signtool.exe" sign /a /n "%CERT%" /tr http://time.certum.pl/ "%~dp0..\DIST\*.exe"
pause
