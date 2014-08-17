package com.jslope.UI;

import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.Message;
import com.jslope.toDoList.core.Options;
import com.jslope.UI.components.JTextAreaMenu;
import com.jslope.UI.components.JTextAreaReadOnly;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Date: 28.12.2005
 */
public class TaskMessages extends JScrollPane {
    private static TaskMessages instance = null;
    private boolean colorOrder;
    private Box box = null;

    public static TaskMessages getInstance() {
        if (instance == null) {
            instance = new TaskMessages();
        }
        return instance;
    }

    JTextArea messageEditor;

    protected TaskMessages() {
//        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messageEditor = new JTextAreaMenu(5, 20);
        messageEditor.setLineWrap(true);
        messageEditor.setWrapStyleWord(true);
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                scrollDown();
            }

            public void ancestorRemoved(AncestorEvent event) {

            }

            public void ancestorMoved(AncestorEvent event) {

            }
        });
    }

    Task task = null;

    /**
     * Updates task
     *
     * @param task
     * @param readOnly if  it is set then last message  is not editable, set true for tasks done
     */
    protected void update(Task task, boolean readOnly) {
        this.task = task;
//        if (box != null) {
//            this.remove(box);
//        }
        box = Box.createVerticalBox();
        colorOrder = true;
        Iterator<Message> messages = task.getMessages().iterator();
        if (messages.hasNext()) {
            while (messages.hasNext()) {
                Message message = messages.next();
                if (messages.hasNext()) {   // if this is not last element
                    addTextViewer(message);
                } else {
                    if (readOnly) {
                        addTextViewer(message);
                    } else {
                        if (message.get(Message.USER).equals(Options.getUserID())) {
                            editedMessage = message;
                            addEditor(message.getContent());
                        } else {
                            addTextViewer(message);
                            addEmptyMesssage(task);

                        }
                    }
                }
            }
        } else {
            if (!readOnly)  {
                addEmptyMesssage(task);
            }
        }
        this.setViewportView(box);
        this.revalidate();
        scrollDown();
    }

    private void scrollDown() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                revalidate();
                box.scrollRectToVisible(messageEditor.getBounds());
            }
        });
    }

    private void addEmptyMesssage(Task task) {
        editedMessage = new Message();
        editedMessage.setParent(task.getID());
        addEditor("");
    }

    String initialContent = null;

    private void addEditor(String content) {
        initialContent = content;
        messageEditor.setText(content);
        box.add(messageEditor);
    }

    private static DateFormat formater = new SimpleDateFormat("yyyy/MM/dd hh:mm");

    private void addTextViewer(Message message) {
        JTextAreaReadOnly textArea = new JTextAreaReadOnly();
        textArea.setLineWrap(true);
        if (colorOrder) {
            textArea.setBackground(Color.PINK);
        } else {
            textArea.setBackground(Color.CYAN);
        }
        textArea.setText(message.getContent());
        colorOrder = !colorOrder;
        JLabel label = new JLabel("created by:" + message.getUserName() + " on: " + formater.format(message.getCreationDate()) + " modified: " + formater.format(message.getLastEditedDate()));
        Box labelBox = Box.createHorizontalBox();
        labelBox.add(label);
        labelBox.add(Box.createHorizontalGlue());
        box.add(labelBox);
        box.add(textArea);
    }

    public static void updateTask(Task task) {
        getInstance().update(task,  false);
    }
    public static void updateTask(Task task, boolean readOnly) {
        getInstance().update(task,  readOnly);
    }

    public static void save() {
        getInstance().saveMessage();
    }

    Message editedMessage = null;

    private void saveMessage() {
        if (editedMessage != null) {
            if (valueChanged()) {
                editedMessage.setContent(messageEditor.getText());
                editedMessage.updateAndSave();
                if (task != null) {
                    task.updateTime();  //this is necessary so that task time change will be propagated up on server
                    //later it can be replaced with Messages and TimeIntervals propagating by itself
                }
            }
        }
    }

    private boolean valueChanged() {
        return !initialContent.equals(messageEditor.getText());
    }
}
