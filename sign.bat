@echo off
setlocal
:SIGN
CLS
SET /P AREYOUSURE=Sign (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO PROMPT
java -jar apksigner.jar sign --key platform.pk8 --cert platform.x509.pem app/release/app-release.apk
:PROMPT
SET /P AREYOUSURE=Install (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO SIGN
adb install app/release/app-release.apk
GOTO SIGN