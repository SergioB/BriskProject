package com.jslope.UI;

import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.Options;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Date: 09.08.2005
 */
public class TaskDoneEditor {
    JPanel panel;
    private JTextField subject;
    private JTextArea notes;
    private JLabel finishedDate;
    private JLabel timeSpent;
    private AbstractAction reopenAction;

    public JPanel getPanel() {
        return panel;
    }

    private static TaskDoneEditor ourInstance = new TaskDoneEditor();

    TaskDoneEditor() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel comment = new JLabel("Subject:");
        JPanel subjectPane = new JPanel();
        subjectPane.setLayout(new BoxLayout(subjectPane, BoxLayout.X_AXIS));
        subjectPane.add(comment);
        subject = new JTextField(20);
        subject.setEnabled(false);
        subject.setMaximumSize(new Dimension(3350, 30));
        subjectPane.add(subject);
        panel.add(subjectPane);
        if (Options.isNetworkClient()) {
            panel.add(TaskReadOnlyMessages.getInstance());
        } else {
            notes = new JTextArea(10, 50);
            notes.setEnabled(false);
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            panel.add(new JScrollPane(notes));
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());

        reopenAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        reopenTask();
                    }
                };

        reopenAction.putValue(Action.NAME, "Reopen task");
        reopenAction.putValue(Action.SMALL_ICON, Buttons.reopenIcon);
        JButton reopen = new JButton(reopenAction);

        buttonPanel.add(reopen);
        buttonPanel.add(Box.createHorizontalStrut(30));
        comment = new JLabel("Finished date:");
        buttonPanel.add(comment);
        finishedDate = new JLabel("");
        buttonPanel.add(finishedDate);
        buttonPanel.add(Box.createHorizontalStrut(30));
        comment = new JLabel("Time spent:");
        buttonPanel.add(comment);
        timeSpent = new JLabel("");
        buttonPanel.add(timeSpent);
        buttonPanel.setMaximumSize(new Dimension(800, 30));
        panel.add(buttonPanel);

    }

    private void reopenTask() {
        task.reopen();
//        TaskTreeModel.getInstance().reload();
        task.updateEditor();
        TreePanel.reloadTree();
    }

    public static TaskDoneEditor getInstance() {
        return ourInstance;
    }

    public static JPanel getTaskPanel() {
        return getInstance().panel;
    }

    Task task = null;
    public void update(Task task) {
        this.task = task;
        subject.setText(task.getSubject());
        if (Options.isNetworkClient()) {
            TaskReadOnlyMessages.updateTask(task);
        } else {
            notes.setText(task.getNotes());
        }
        finishedDate.setText(task.getFinishedDateAsText());
        task.updateTimeElapsed(timeSpent);
    }

    public void updateData(Task task) {
    }

    public static AbstractAction getReopenAction() {
        return getInstance().reopenAction;
    }
}
