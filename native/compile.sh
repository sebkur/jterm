#!/bin/bash
gcc -I/usr/lib/jvm/java-7-openjdk-amd64/include/ -fPIC -o libterminal.so -shared de_topobyte_jterm_core_Terminal.c

gcc -I/usr/lib/jvm/java-7-openjdk/include/linux -I/usr/lib/jvm/java-7-openjdk/include/ -fPIC -o libterminal.so -shared de_topobyte_jterm_core_Terminal.c
