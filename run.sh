#!/bin/bash

rm *.class > /dev/null 2>&1

set -e

javac Main.java
java Main
