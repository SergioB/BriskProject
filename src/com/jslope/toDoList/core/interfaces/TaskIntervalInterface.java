package com.jslope.toDoList.core.interfaces;

import com.jslope.toDoList.core.Task;

/**
 * Date: 08.08.2005
 */
public interface  TaskIntervalInterface {
    public String getSubject();
    public String getDescription();
    public String getCompactSubject();
    public Task getTask();
    int getDuartion();
    public String getNotes();
}
