#!/bin/bash

beepbeep_home=$(dirname $0)
beepbeep_home=$(cd $beepbeep_home/.. && pwd)

if [ "$JAVA_HOME" == "" ];  then
  java_cmd=java
else
  java_cmd=$JAVA_HOME/bin/java
fi

beepbeep_cp=$beepbeep_home/config:$beepbeep_home/lib/*
# Disable Bash file glob
beepbeep_opts=$(echo "$*" |sed -e 's/\*/\\*/g')
set -f
echo $beepbeep_opts
$java_cmd -cp $JAVA_OPTS $beepbeep_cp com.github.gquintana.beepbeep.cli.Main $beepbeep_opts
~