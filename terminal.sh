#!/bin/bash

# find java claspath
DIR=`dirname $0`
#echo "we are operating from: $DIR"
CLASSPATH=.
CLASSPATH+=:$DIR/bin/
CLASSPATH+=:$DIR/lib/*
CLASSPATH+=:$DIR

NATIVE=$DIR/native/

echo "CLASSPATH=$CLASSPATH"
echo "NATIVE=$NATIVE"

java -Djava.library.path="$NATIVE" -cp "$CLASSPATH" de.topobyte.jterm.JTerm
