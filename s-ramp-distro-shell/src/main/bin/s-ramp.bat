@echo off
rem ---------------------------------------------------------------------------
rem Run script for the S-RAMP Interactive Shell (Windows)
rem
rem Environment Variable Prerequisites
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem ---------------------------------------------------------------------------
@echo on

if not exist "%JAVA_HOME%\bin\java.exe" goto noJava
%JAVA_HOME%/bin/java.exe -jar s-ramp-shell-${project.version}.jar

:noJava
echo The JAVA_HOME environment variable is not defined correctly.
echo This environment variable is needed to run the S-RAMP shell.
goto end

:end
