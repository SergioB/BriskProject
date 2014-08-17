echo $@
#
# Thanks to Peter Torngaard
#
full=$(dirname $0)
java -cp .:${full}/../lib/hsqldb.jar:${full}/../lib/BriskProject.jar com.jslope.briskproject.networking.ClientMain
exit