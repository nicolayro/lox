#!/bin/bash

EXTENSION=".lox"
TEST_DIR="examples/*$EXTENSION"

for path in $TEST_DIR; do
    PROGRAM=$(basename "$path" $EXTENSION)

    EXPECTED=$(cat "examples/$PROGRAM.txt") 
    ACTUAL=$(./compile_and_run.sh "examples/$PROGRAM.lox")

    if [[ $EXPECTED == $ACTUAL ]]; then
        echo "[SUCCESS] $PROGRAM"
    else
        echo "[FAIL]    $PROGRAM"
    fi
done

