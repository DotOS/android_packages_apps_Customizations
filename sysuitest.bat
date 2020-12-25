@echo off
setlocal
:RECOVERY
CLS
set /p DUMMY=Waiting...
echo Getting SystemUI...
scp -r -i %systemdrive%%homepath%\.ssh\id_rsa iacob@147.75.84.3:/dot/iacob/dot5/out/target/product/davinci/system/system_ext/priv-app/SystemUI/SystemUI.apk D:\Projects\dotOS\Customizations
echo Sending SystemUI...
adb push D:/Projects/dotOS/Customizations/SystemUI.apk /sdcard/11R/
SET /P AREYOUSURE=Reboot (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO BACKUP
:REBOOT
echo Rebooting...
adb reboot recovery
set /p DUMMY=Waiting to mount system
:BACKUP
SET /P AREYOUSURE=Backup SystemUI (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO REPLACE
adb shell "cd /system/system_ext/priv-app/SystemUI/ && cp SystemUI.apk SystemUI.apk.bak"
GOTO REPLACE
:REPLACE
set /p DUMMY=Replace and reboot
adb shell "cd /system/system_ext/priv-app/SystemUI/ && mv /sdcard/11R/SystemUI.apk SystemUI.apk && reboot"
GOTO RECOVERY