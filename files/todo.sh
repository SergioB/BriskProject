#!/bin/sh
echo $@
#thanks to freemind
full=$(dirname $0)
echo ${full}/lib/toDoList.jar
java -cp .:${full}/lib/hsqldb.jar:${full}/lib/BriskProject.jar com.jslope.toDoList.ToDoList
