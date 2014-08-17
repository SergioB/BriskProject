package com.jslope.UI;

import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.User;
import com.jslope.persistence.PersistenceException;
import com.jslope.UI.components.NumberEditor;
import com.jslope.briskproject.networking.NetworkConfig;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Date: 21.06.2005
 */
public class OptionsWindow extends JDialog {
    static private OptionsWindow instance = null;
    private JCheckBox hideFinishedTasks;
    private NumberEditor autostopInterval;
    private NumberEditor taskDoneFilter;
    private NumberEditor userShowTime;
    private JTextField serverHost;
    private NumberEditor taskReadTime;

    public static OptionsWindow getInstance() {
        if (instance == null) {
            instance = new OptionsWindow();
        }
        return instance;
    }

    JCheckBox autostartNextTask, autostopCurrentTask;

    private OptionsWindow() {
        super(MainWindow.getInstance());
//        JPanel pane = new JPanel(new GridLayout(0, 1));
        Box pane = Box.createVerticalBox();
        pane.add(createAutoPanel());
        pane.add(createNewTasksPanel());
        pane.add(createteTasksFinishedPanel());
        pane.add(createUserPanel());
        if (Options.isNetworkClient()) {
            pane.add(createNetworkPanel());
        }

        pane.add(createButtonPanel());

        restoreFromOptions();
        this.setContentPane(pane);
        setModal(true);
    }

    private Component createAutoPanel() {
        JPanel pane = new JPanel(new GridLayout(0, 1));
        pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Auto options"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        autostartNextTask = new JCheckBox("autostart next task");
        autostartNextTask.setToolTipText("If checked, then when current task is done next task will start");
        autostartNextTask.setMnemonic(KeyEvent.VK_A);
        pane.add(autostartNextTask);
        autostopCurrentTask = new JCheckBox("autostop current task");
        autostopCurrentTask.setToolTipText("If checked then current task will stop in case no action will be made on computer during set interval");
        pane.add(autostopCurrentTask);

        autostopInterval = new NumberEditor(5, 1, 500);
        autostopInterval.showTo(pane, "autostop interval:", "min");
        autostopInterval.setToolTip("Pauses task after this number minutes");
        return pane;
    }

    private Component createNewTasksPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("New received tasks"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        taskReadTime = new NumberEditor(1, 0, 60);
        taskReadTime.setToolTip("Time after which new taks becomes read, in seconds");
        taskReadTime.showTo(panel, "Task read time: ", "s");
        return panel;
    }

    private Component createNetworkPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Network"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JLabel label = new JLabel("server address: ");
        panel.add(label);
        serverHost = new JTextField();
        panel.add(serverHost);
        return panel;
    }

    private Component createUserPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Users"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        userShowTime = new NumberEditor(7, 1, 99999);
        userShowTime.showTo(panel, "Show worked time for past", "days");
        return panel;
    }

    private Component createteTasksFinishedPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Finished tasks"),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        hideFinishedTasks = new JCheckBox("hide finished tasks");
        hideFinishedTasks.setMnemonic(KeyEvent.VK_F);
        hideFinishedTasks.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateEnables();
            }
        });
        panel.add(hideFinishedTasks);
        taskDoneFilter = new NumberEditor(99999, 0, 99999);
        taskDoneFilter.showTo(panel, "Task filter:", "days");
        taskDoneFilter.setToolTip("Filters out tasks older than this number of days");
        return panel;
    }

    private void updateEnables() {
        taskDoneFilter.setEnabled(!hideFinishedTasks.isSelected());
    }

    private JPanel createButtonPanel() {
        JButton ok = new JButton("Ok");
        ok.setMnemonic(KeyEvent.VK_O);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveOptions();
                dispose();
            }
        });
        JButton cancel = new JButton("cancel");
        cancel.setMnemonic(KeyEvent.VK_C);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                restoreFromOptions();
                dispose();
            }
        });
//        JPanel buttonPanel = new JPanel(new GridLayout(1,0));
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        return buttonPanel;
    }

    private void saveOptions() {
        Options options = Options.getInstance();
        boolean oldHideTaskDone = options.getHideTaskDone();
        int oldTaskDoneFilter = options.getTaskDoneFilter();
        int oldUserShowTime = options.getUserShowTime();
        options.setAutostartTask(autostartNextTask.isSelected());
        options.setAutostopTask(autostopCurrentTask.isSelected());
        options.setAutostopInterval(((Integer) autostopInterval.getValue()).intValue());
        options.setHideTasksDone(hideFinishedTasks.isSelected());
        options.setTaskDoneFilter(taskDoneFilter.getValue());
        options.setUserShowTime(userShowTime.getValue());       //todo: to check for changes and update user editors
        Options.setNewToReadTime(taskReadTime.getValue());
        if (oldHideTaskDone != hideFinishedTasks.isSelected()) {
            TreePanel.reloadTree();
        } else if (!hideFinishedTasks.isSelected()) {
            if (oldTaskDoneFilter != taskDoneFilter.getValue()) {
                TreePanel.reloadTree();
            }
        }

        if (oldUserShowTime != userShowTime.getValue()) {
            ((User) Options.getInstance().getRootNode()).updateWorkedTimeRecursivelly();
        }

        if (Options.isNetworkClient()) {
            NetworkConfig.setHost(serverHost.getText());
            NetworkConfig.getInstance().save();
        }

        try {
            Options.getInstance().save();
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
    }

    private void restoreFromOptions() {
        Options options = Options.getInstance();
        autostartNextTask.setSelected(options.getAutostartTask());
        autostopCurrentTask.setSelected(options.getAutostopTask());
        autostopInterval.setValue(options.getAutostopInterval());
        hideFinishedTasks.setSelected(options.getHideTaskDone());
        taskDoneFilter.setValue(options.getTaskDoneFilter());
        userShowTime.setValue(options.getUserShowTime());
        taskReadTime.setValue(Options.getNewToReadTime());
        if (Options.isNetworkClient()) {
            serverHost.setText(NetworkConfig.getHost());
        }
        updateEnables();
    }
}
