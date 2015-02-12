@echo off
rem ---------------------------------------------------------------------------
rem Run script for the Artificer Interactive Shell (Windows)
rem
rem Environment Variable Prerequisites
rem
rem   JAVA_HOME       Must point at a valid Java installation.
rem
rem ---------------------------------------------------------------------------
@echo on

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT" setlocal

if "%OS%" == "Windows_NT" (
  set "DIRNAME=%~dp0%"
) else (
  set DIRNAME=.\
)

if not exist "%JAVA_HOME%\bin\java.exe" goto noJava
"%JAVA_HOME%/bin/java.exe" -Xmx1024m -jar "%DIRNAME%\artificer-shell-${project.version}.jar" %*

:noJava
echo The JAVA_HOME environment variable is not defined correctly.
echo This environment variable is needed to run the Artificer shell.
goto end

:end
