echo $@
full=$(dirname $0)
java -cp .:${full}/../lib/hsqldb.jar:${full}/../lib/BriskProject.jar com.jslope.briskproject.server.ServerMain
exit