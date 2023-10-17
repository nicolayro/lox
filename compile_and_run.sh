#!/bin/bash

# Compile
javac -d target/ jlox/Jlox.java

if [[ -z $1 ]]; then
    java -cp target/ jlox.Jlox
else
    java -cp target/ jlox.Jlox $1
fi
