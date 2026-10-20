#!/bin/bash
# Compile everything into the 'out' folder.   Usage: ./compile.sh
mkdir -p out
javac -d out -cp "lib/*" $(find src -name "*.java")
echo "Compile finished."
