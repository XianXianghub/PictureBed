@echo off
echo start make ...
SET APKNAME=mssql.apk

for /f "tokens=2 delims=\" %%a in ('whoami') do set "currentUsername=%%a"
set JAVA_HOME_PATH="D:\androidstudio\jbr\bin\"
echo "currentUsername=%currentUsername%"
if /I "%currentUsername%"=="xiangsq" (
    set JAVA_HOME_PATH="D:\androidstudio\jbr\bin\"
) else if /I "%currentUsername%"=="zhouzy" (
    echo "1111"
) else if /I "%currentUsername%"=="up" (
   set JAVA_HOME_PATH="D:\android\Android Studio\jbr\bin\"
) else (
    echo "Unknown user"
)
set PATH=%JAVA_HOME%;%JAVA_HOME_PATH%;


SET APKFILE_PATH=app\build\outputs\apk\release\%APKNAME%
del %APKFILE_PATH%
del ..\release\%APKNAME%

call gradlew.bat -stop
call gradlew.bat assembleRelease
copy app\build\outputs\apk\release\%APKNAME%  D:\ASWork\release\

if exist %APKFILE_PATH% (
  echo make success
  copy app\build\outputs\mapping\release\mapping.txt  .
  start "" "D:\ASWork\release"
) else (
  echo make failed
) 
::./gradlew lintRelease