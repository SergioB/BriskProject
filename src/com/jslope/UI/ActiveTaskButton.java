package com.jslope.UI;

import com.jslope.toDoList.core.Task;
import com.jslope.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Date: 09.09.2005
 */
public class ActiveTaskButton {
    private static final String NO_TASK_SELECTED= "No task active";
    private static JButton button = new JButton(NO_TASK_SELECTED);
    static {
        button.setEnabled(false); // it will be enabled only when task is resumed
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                TreePanel.selectNode(Task.getActiveTask());
            }
        });
    }
    public static JButton getButton() {
        return button;
    }

    public static void updateActiveTask() {
        Task activeTask = Task.getActiveTask();
        if (activeTask != null) {
            if (!activeTask.isPaused()) {
                button.setText(Utils.formatString(activeTask.getSubject(), 15) +
                        " " + activeTask.getFormatedTimeSpent());
            }
        }
    }
    public static void setEnabled(boolean enabled) {
        if (enabled) {
            updateActiveTask();
        } else {
            button.setText(NO_TASK_SELECTED);
        }
        button.setEnabled(enabled);
    }
}
