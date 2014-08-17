package com.jslope.UI;

import com.jslope.toDoList.core.Task;

/**
 * Created by IntelliJ IDEA.
 * User: I
 * Date: Feb 28, 2006
 * Time: 12:35:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaskReadOnlyMessages extends TaskMessages {
    static TaskReadOnlyMessages readOnlyInstance = null;
    public static TaskMessages getInstance() {
        if (readOnlyInstance == null) {
            readOnlyInstance = new TaskReadOnlyMessages();
        }
        return readOnlyInstance;
    }

    public static void updateTask(Task task) {
        getInstance().update(task,  true);
    }

}
