package com.jslope.UI;

import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.SharedTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Date: 09.08.2005
 */
public class SharedTaskEditor {
    JPanel panel;
    private JTextField subject;
    private JLabel timeSpent;

    public JPanel getPanel() {
        return panel;
    }

    private static SharedTaskEditor ourInstance = new SharedTaskEditor();

    SharedTaskEditor() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel comment = new JLabel("Shared task:");
        JPanel subjectPane = new JPanel();
        subjectPane.setLayout(new BoxLayout(subjectPane, BoxLayout.X_AXIS));
        subjectPane.add(comment);
        subject = new JTextField(20);
        subject.setEnabled(false);
        subject.setMaximumSize(new Dimension(3350, 30));
        subjectPane.add(subject);
        panel.add(subjectPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        JButton gotoTask = new  JButton("Go to Task");
        gotoTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.selectNode(task);
            }
        });
        buttonPanel.add(gotoTask);
        buttonPanel.add(Box.createHorizontalStrut(30));
        JButton unasign =  new JButton("Unassign");
        unasign.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sharedTask.unassign();
            }
        });
        buttonPanel.add(unasign);
        buttonPanel.add(Box.createHorizontalStrut(30));
        comment = new JLabel("Time spent:");
        buttonPanel.add(comment);
        timeSpent = new JLabel("");
        buttonPanel.add(timeSpent);
        buttonPanel.setMaximumSize(new Dimension(800, 30));
        panel.add(buttonPanel);

    }

    public static SharedTaskEditor getInstance() {
        return ourInstance;
    }

    public static JPanel getTaskPanel() {
        return getInstance().panel;
    }
    Task task = null;
    SharedTask sharedTask = null;
    public void update(SharedTask sharedTask) {
        this.task =  sharedTask.getTask();
        this.sharedTask = sharedTask;
        subject.setText(task.getSubject());
        task.updateTimeElapsed(timeSpent);
    }

    public void updateData(Task task) {
    }
}
