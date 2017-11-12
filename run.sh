#!/bin/bash

set -e

rm -rf bin > /dev/null 2>&1
mkdir bin

cd src

javac -Xlint:-deprecation -d ../bin -cp .:../lib/* Main.java > /dev/null

cd ../bin

java -cp .:../lib/* Main

cd ..
