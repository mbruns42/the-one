#! /bin/sh
java -Xmx64G -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar core.DTNSim $*
