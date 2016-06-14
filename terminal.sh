#!/bin/bash

DIR=$(dirname $0)
LIBS="$DIR/build/lib-run"

if [ ! -d "$LIBS" ]; then
	echo "Please run 'gradle createRuntime'"
	exit 1
fi

CLASSPATH="$LIBS/*"
NATIVE=$DIR/native/

echo "CLASSPATH=$CLASSPATH"
echo "NATIVE=$NATIVE"

exec java -Djava.library.path="$NATIVE" -cp "$CLASSPATH" de.topobyte.jterm.JTerm
