#!/bin/sh
echo "Compilando..."
rm -rf out && mkdir out
javac -encoding UTF-8 -d out src/uci/*.java src/uci/*/*.java pruebas/uci/*.java && echo "Compilacion exitosa."
