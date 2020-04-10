# JTerm

JTerm is a terminal emulator written in Java with a very thin native layer to
connect to the underlying pseudoterminal device.

## Setup

Run this to compile the software:

    ./gradlew clean terminalShared createRuntime

and then execute the main script:

    ./terminal.sh

## Specification

Useful information on implementing a terminal emulator can be found
on <https://vt100.net/>, especially the [VT102 User
Guide](https://vt100.net/docs/vt102-ug/) and its chapter about
[Received Character Processing](https://vt100.net/docs/vt102-ug/chapter5.html)
is a useful resource. The page that describes the
[Xterm Control Sequences](https://www.xfree86.org/current/ctlseqs.html) is
also a very good reference.
