#!/bin/sh
./compilar.sh && java -Dfile.encoding=UTF-8 -cp out uci.PruebasSistema && java -Dfile.encoding=UTF-8 -cp out uci.PruebasInterfaz
echo "Tiempos de ejecucion: java -cp out uci.Benchmark"
