package com.jslope.UI;

import com.jslope.toDoList.core.Task;

import javax.swing.*;

/**
 * Date: 09.08.2005
 */
public class TaskEditor {
    OneTaskPanel panel;
    public JPanel getPanel() {
        return panel;
    }

    private static TaskEditor ourInstance = new TaskEditor();

    TaskEditor() {
        panel = generatePanel();
    }
    public static TaskEditor getInstance() {
        return ourInstance;
    }
    OneTaskPanel generatePanel() {
        return OneTaskPanel.getInstance();
    }

    public static JPanel getTaskPanel() {
        return getInstance().panel;
    }

    public void update(Task task) {
        panel.setTask(task);
    }

    public void updateData(Task task) {
        panel.updateToTask(task);
    }
}
