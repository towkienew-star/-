@echo off
chcp 65001 >nul
setlocal
where java >nul 2>nul
if errorlevel 1 (
  echo Install Java JDK 21 first: https://adoptium.net/
  pause
  exit /b 1
)
where gradle >nul 2>nul
if errorlevel 1 (
  echo Installing local Gradle 8.13. Internet required.
  if not exist .buildtools mkdir .buildtools
  if not exist .buildtools\gradle-8.13\bin\gradle.bat (
    curl.exe -L -f -o .buildtools\gradle.zip https://services.gradle.org/distributions/gradle-8.13-bin.zip
    if errorlevel 1 (echo Download failed & pause & exit /b 1)
    powershell -NoProfile -Command "Expand-Archive -Path '.buildtools\gradle.zip' -DestinationPath '.buildtools' -Force"
    if errorlevel 1 (echo Extraction failed & pause & exit /b 1)
  )
  call .buildtools\gradle-8.13\bin\gradle.bat build --no-daemon
) else (
  call gradle build --no-daemon
)
if errorlevel 1 (
  echo Build failed. Read the error above.
) else (
  echo Ready: build\libs\towkiemorphs-1.21.4-1.0.0.jar
)
pause
