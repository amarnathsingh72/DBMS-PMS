@REM Maven Wrapper script for Windows
@REM Downloads Maven 3.9.6 if not present and runs it

@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%
set MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo Downloading Maven %MAVEN_VERSION%...
    mkdir "%USERPROFILE%\.m2\wrapper" 2>nul
    powershell -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%USERPROFILE%\.m2\wrapper\maven.zip'"
    powershell -Command "Expand-Archive -Path '%USERPROFILE%\.m2\wrapper\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper' -Force"
    del "%USERPROFILE%\.m2\wrapper\maven.zip"
    echo Maven downloaded to %MAVEN_HOME%
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
