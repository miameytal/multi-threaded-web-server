#!/bin/bash

# Compile all Java files in the Sources directory
javac Sources/*.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful"
else
    echo "Compilation failed"
fi
