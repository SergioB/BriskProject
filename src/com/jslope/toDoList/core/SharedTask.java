package com.jslope.toDoList.core;

import com.jslope.UI.*;

import javax.swing.*;
import java.util.HashMap;

/**
 * Date: Mar 7, 2006
 * Time: 1:43:42 PM
 */
public class SharedTask extends TreeElement {

    boolean taskIsActive;
    Task task;

    TaskSharer taskSharer;
    private SharedTask(TaskSharer taskSharer) {
        this.task = taskSharer.getTask();
        this.taskSharer = taskSharer;
        taskIsActive = task.isActive();
    }
    public String getID() {
        return taskSharer.getID();
    }

    public void initParent() {

    }

    public void updateEditor() {
            EditorsPanel.select(Editors.SHARED_TASK);
            SharedTaskEditor.getInstance().update(this);
    }

    public void updateDataFromEditor() {

    }

    protected ChildrenTypes getType() {
        if (taskIsActive) {
            return ChildrenTypes.SHARED_TASKS;
        } else {
            return ChildrenTypes.SHARED_TASKS_DONE;
        }
    }

    protected boolean canChildMoveLeft() {
        return false;
    }
    protected boolean canChildMoveRight() {
        return false;
    }

    public Icon getIcon() {
        if (taskIsActive) {
            return Buttons.sharedTaskIcon;
        } else {
            return Buttons.sharedTaskDoneIcon;
        }
    }

    public void showDebugInfo() {

    }

    public User getParentUser() {
        return (User)parent;
    }

    /**
     * we don't need to load children for SharedTask
     */
    public void loadChildren() {
    }

    public void save() {
    }
    public String toString() {
        return "Shared: " + task.toString();
    }

    public static TreeElement getST(User user, Task task) {
        return getST(TaskSharer.getShare(user, task));
    }

    static private HashMap<String, TreeElement>  cache = new HashMap<String, TreeElement>();
    public static TreeElement getST(TaskSharer taskSharer) {
        if (cache.containsKey(taskSharer.getID())) {
            return cache.get(taskSharer.getID());
        } else {
            SharedTask st = new SharedTask(taskSharer);
            cache.put(taskSharer.getID(), st);
            return st;
        }
    }

    public Task getTask() {
        return task;
    }

    public void unassign() {
        justRemoveFromParent();
        TreePanel.reload(getParent());
        TreePanel.selectNode(getParent());
        taskSharer.virtualDelete();
    }
}
