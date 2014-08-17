package com.jslope.UI;

/**
 * Date: 09.08.2005
 */
public enum Editors {
    USER_PANEL("UserPanel"),
    TASK_PANEL("TaskPanel"),
    TASK_DONE_PANEL("TaskDonePanel"),
    SHARED_TASK("SharedTaskPanel");

    String type;
    Editors(String type) {
        this.type = type;
    }
    public String show() {
        return type;
    }

}
