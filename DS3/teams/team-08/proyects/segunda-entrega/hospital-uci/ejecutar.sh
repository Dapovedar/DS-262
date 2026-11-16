#!/bin/sh
[ -d out ] || ./compilar.sh
java -Dfile.encoding=UTF-8 -cp out uci.Main "$@"
