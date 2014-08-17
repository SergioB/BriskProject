package com.jslope.UI;

import com.jslope.toDoList.ToDoList;

import javax.swing.*;

/**
 *  Date: 08.05.2005
 */
public class Buttons {

    public final static ImageIcon  upImage = createImageIcon("images/up.gif", "up"),
    downImage = createImageIcon("images/down.gif", "down"),
    leftImage = createImageIcon("images/left.gif", "out"),
    rightImage = createImageIcon("images/right.gif", "in"),
    shareImage = createImageIcon("images/share.gif", "share"), 
    moveImage = createImageIcon("images/move.gif", "move"),
    delImage = createImageIcon("images/delete.gif", "delete"),
    addTaskIcon = createImageIcon("images/add_task.gif","add task"),
    addSubTaskIcon = createImageIcon("images/add_sub_task.gif","add sub task"),
    user = createImageIcon("images/user.gif","user"),
    addUserIcon = createImageIcon("images/user.gif","user"),
    sharedTaskIcon = createImageIcon("images/shared_task.gif","task"),
    sharedTaskNewIcon = createImageIcon("images/shared_taskNew.gif","task"),
    sharedTaskDoneIcon = createImageIcon("images/shared_task_done.gif","task"),
    taskIcon = createImageIcon("images/task.gif","task"),
    manyTasksIcon = createImageIcon("images/manyTasks.gif","tasks"),
    taskNewIcon = createImageIcon("images/taskNew.gif","task"),
    manyTasksNewIcon = createImageIcon("images/manyTasksNew.gif","tasks"),
    hasNewChildrenIcon = createImageIcon("images/has_new_children.gif","tasks"),
    taskDoneIcon = createImageIcon("images/taskDone.gif","task done"),
    manyTasksDoneIcon = createImageIcon("images/manyTasksDone.gif","tasks done"),
    sharedTaskNewChildrenIcon = createImageIcon("images/shared_task_new_children.gif","shared tasks"),
    startIcon = createImageIcon("images/start_button.gif","start task"),
    pauseIcon = createImageIcon("images/pause_button.gif","pause task"),
    taskDoneButtonIcon = createImageIcon("images/task_done_Button.gif","task done"),
    reopenIcon = createImageIcon("images/reopen_task.gif","reopen task"),
    applicationIcon = createImageIcon("images/TaskManager.gif","");



    /** Returns an ImageIcon, or null if the path was invalid. */
    private static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = ToDoList.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


}
