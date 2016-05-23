@echo off

set "beepbeep_home=%~dp0.."

set "java_cmd=java"
if "%JAVA_HOME%" == "" goto java_cmd_set
  set "java_cmd=%JAVA_HOME%\bin\java"
:java_cmd_set

set "beepbeep_cp=%beepbeep_home%\config;%beepbeep_home%\lib\*"
set "beepbeep_opts=%*"
"%java_cmd%" -cp "%beepbeep_cp%" %JAVA_OPTS% com.github.gquintana.beepbeep.cli.Main %beepbeep_opts%
