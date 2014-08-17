package com.jslope.UI;

import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.User;
import com.jslope.utils.StopWatch;
import com.jslope.UI.components.JTextFieldMenu;
import com.jslope.UI.components.JTextAreaMenu;
import com.jslope.UI.components.FixedSizePlainDocument;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Date: 07.03.2005
 */
public class OneTaskPanel extends JPanel {

    JTextField subject;
    JTextArea notes;
    JLabel time;
    Options options = Options.getInstance();
    StopWatch stopWatch = new StopWatch();
    int mouseX, mouseY;
    private JLabel assignedStatus;
    private JLabel taskOwner;
    private AbstractAction pauseAction;
    private AbstractAction taskDoneAction;

    public Task getTask() {
        return task;
    }

    Task task = null;
    static OneTaskPanel instance;

    private OneTaskPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel subjectPane = new JPanel();
        subjectPane.setLayout(new BoxLayout(subjectPane, BoxLayout.X_AXIS));
        JLabel subjectLabel = new JLabel("Subject:");
        subject = new JTextFieldMenu(20);
        subject.setDocument(new FixedSizePlainDocument(Task.MAX_SUBJECT_LENGHT));
        subject.setMaximumSize(new Dimension(3350, 30));
        subject.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                checkForEmpty();
            }
        });
        subject.getDocument().addDocumentListener(new DocumentListener() {
            public void update() {
                if (!updatingTask) {    //we don't need to update task in the midlle of other operations
                    task.setSubject(subject.getText());
                    TaskTreeModel.getInstance().fireTreeNodeChanged(task);
                }
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });

        subject.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TreePanel.addTask();
                    checkForEmpty();
                }
//                else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
//                    TreePanel.addSubTask();
//                    checkForEmpty();
//                }
            }
        });
        subject.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "justIns");
        subject.getActionMap().put("justIns", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.addSubTask();
                checkForEmpty();
            }
        });


        subjectLabel.setDisplayedMnemonic('u');
        subjectLabel.setLabelFor(subject);
        subjectPane.add(subjectLabel);
        subjectPane.add(subject);
//        subject.setFocusAccelerator();
        if (!Options.isNetworkClient()) {
            notes = new JTextAreaMenu(5, 20);
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
        }  //else is done in class TaskMessages

        pauseAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                task.updateDataFromEditor();
                if (task.isPaused()) {
                    resume();
                    stopWatch.restart();
                } else {
                    pause();
                }
            }
        };
        pauseAction.putValue(Action.NAME, " Start ");
        pauseAction.putValue(Action.SMALL_ICON, Buttons.startIcon);
        JButton pause = new JButton(pauseAction);
        pause.setMnemonic(KeyEvent.VK_S);

        taskDoneAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!task.isInternalTask()) {
                    task.updateDataFromEditor();
                    if (task.hasActiveChildren()) {
                        int answer = JOptionPane.showConfirmDialog(getThis(),
                                "This task has unfinished children.\n Should we proceed anyway?",
                                "Confirm task done",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (answer != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                    task.setDoneAndShowNext();
                    ActiveTaskButton.setEnabled(Task.getActiveTask() != null);  //if activeTask == null it is disabled otherwise it is enabled
                }
            }
        };
        taskDoneAction.putValue(Action.NAME, "Task Done");
        taskDoneAction.putValue(Action.SMALL_ICON, Buttons.taskDoneButtonIcon);
        JButton taskDone = new JButton(taskDoneAction);
        taskDone.setMnemonic(KeyEvent.VK_D);
        time = new JLabel("00:00:00");
        time.setOpaque(true); // it is done so we'll be able to see background color
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        assignedStatus = new JLabel("");
        statusPanel.add(assignedStatus);
        statusPanel.add(new JLabel("     "));
        taskOwner = new JLabel();
        statusPanel.add(taskOwner);
        //time.setPreferredSize(new Dimension(300,30));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(taskDone);
//        buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(time);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(pause);
        buttonPanel.setMaximumSize(new Dimension(500, 30));
        this.add(subjectPane);
        if (Options.isNetworkClient()) {
            this.add(TaskMessages.getInstance());
        } else {
            this.add(new JScrollPane(notes));
        }
        this.add(statusPanel);
        this.add(buttonPanel);
        Timer timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (task != null) {
                    if (task.isNewlyModified()) {
                        verifyNewlyTask();
                    }
                    task.updateTimeElapsed(time);
                    ActiveTaskButton.updateActiveTask();

                    if (!Task.isActiveTaskPaused()) {
                        if (options.getAutostopTask()) {
                            checkAutostopTask();
                        }
                    }
                }
            }
        });
        timer.start();
        addKeyboardListenerForStopWatch();
    }

    private void verifyNewlyTask() {
        long diff = System.currentTimeMillis() - selectionTime;
        if (diff > Options.getNewToReadTime() * 1000) {
            if (TreePanel.getSelectedNode().getID().equals(task.getID())) {
                task.setAsRead();
                TaskTreeModel.getInstance().fireTreeNodeChanged(task);
            }
        }
    }

    private void addKeyboardListenerForStopWatch() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                stopWatch.restart();
            }

        }, AWTEvent.KEY_EVENT_MASK);

    }

    private JPanel getThis() {
        return this;
    }

    private void checkForEmpty() {
        if (subject.getText().equals("empty")) {
            subject.setSelectionStart(0);
            subject.setSelectionEnd(5);
        }
    }

    private void checkAutostopTask() {
        PointerInfo pi = MouseInfo.getPointerInfo();
        Point po = pi.getLocation();
        Task pausedTask = null;
        if (mouseX != (int) po.getX() || mouseY != (int) po.getY()) {
            mouseX = (int) po.getX();
            mouseY = (int) po.getY();
            stopWatch.restart();
        } else if (stopWatch.getMinutes() >= options.getAutostopInterval()) {
            pausedTask = pause();
            Object options[] = {"Continue", "Stop"};
            MainWindow win = MainWindow.getInstance();
            // actually win.toFront() should work, but it doesn't work, so it's only method to move a window to front
            win.setAlwaysOnTop(true);
            win.setAlwaysOnTop(false);
            int answer = JOptionPane.showOptionDialog(getThis(),
                    "Task has been paused because there were no activity on this computer",
                    "Task autostoping",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, options[1]);
            if (answer == 0) {
                if (pausedTask == task) {   //if it's currently shown task then we'll use standart procedure
                    resume();
                } else {    //if it's other task we'll just resume task and enable button
                    pausedTask.resume();
                    ActiveTaskButton.setEnabled(true);
                }
            }
        }
    }

    long selectionTime = 0;

    void setTask(Task task) {
        this.task = task;
        updateFromTask();
        selectionTime = System.currentTimeMillis();
    }

    /**
     * @return task which was paused
     */
    Task pause() {
        Task activeTask = Task.getActiveTask();
        if (task == activeTask) {
            updateToTask(task);
            setNotBusyBackground();
            if (!task.isPaused()) { // if current task is active task
                ActiveTaskButton.setEnabled(false);
            }
            task.pause();
            updateFromTask();
            return task;
        } else {
            activeTask.pause();
            ActiveTaskButton.setEnabled(false);
            return activeTask;
        }
    }

    void resume() {
        setBusyBackground();
        task.resume();
        updateFromTask();
        ActiveTaskButton.setEnabled(true);
    }

    boolean updatingTask = false;

    void updateFromTask() {
        updatingTask = true;
        subject.setText(task.getSubject());
        if (Options.isNetworkClient()) {
            TaskMessages.updateTask(task);
        } else {
            notes.setText(task.getNotes());
        }

        if (task.isPaused()) {
            setNotBusyBackground();
            if (task.hasStarted()) {
                pauseAction.putValue(Action.NAME, " Resume ");
                pauseAction.putValue(Action.SMALL_ICON, Buttons.startIcon);
//                pause.setText("Resume");
            } else {
                pauseAction.putValue(Action.NAME, " Start ");
                pauseAction.putValue(Action.SMALL_ICON, Buttons.startIcon);
//                pause.setText("Start");
            }
        } else {
            setBusyBackground();
            pauseAction.putValue(Action.NAME, " Pause ");
            pauseAction.putValue(Action.SMALL_ICON, Buttons.pauseIcon);
//            pause.setText("Pause");
        }
        if (task.isAssignedToUser()) {
            if (User.userExists(task.getAssignedUserID())) {
                assignedStatus.setText("assigned to user: " + task.getAssignedUser());
            } else {
                assignedStatus.setText("task is assigned to unaccesible user ");
            }
        } else {
            assignedStatus.setText("task is not directly assigned to user");
        }
        taskOwner.setText(task.getOwnerString());
//        taskDoneAction.setEnabled(!task.isInternalTask()); //todo: in canDoTaskDone
        updatingTask = false;
    }

    private void setNotBusyBackground() {
        time.setBackground(null);
    }

    private void setBusyBackground() {
        time.setBackground(Color.PINK);
    }

    void updateToTask(Task task) {
        task.setSubject(subject.getText());
        if (Options.isNetworkClient()) {
            TaskMessages.save();
        } else {
            task.setNotes(notes.getText());
        }

    }

    public static OneTaskPanel getInstance() {
        if (instance == null) {
            instance = new OneTaskPanel();
        }
        return instance;
    }

    public void focusSubject() {
        subject.requestFocusInWindow();
    }

    public static void selectSubject() {
        getInstance().subject.requestFocus();
    }

    public static AbstractAction getPauseAction() {
        return getInstance().pauseAction;
    }

    public static AbstractAction getTaskDoneAction() {
        return getInstance().taskDoneAction;
    }
}
