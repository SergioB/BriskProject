/**
 * Date: 14.08.2005
 */
package com.jslope.UI;

import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.TreeElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ToolBar {
    private static ToolBar ourInstance = new ToolBar();
    private JButton upArrow;
    private JButton downArrow;
    private JButton leftArrow;
    private JButton rightArrow;
    private JButton move;
    private JButton share;
    private JButton remove;
    private JButton addTask;
    private JButton addSubTask;
    private JButton addUser;

    public static ToolBar getInstance() {
        return ourInstance;
    }

    private ToolBar() {
        ActionMenu actions = ActionMenu.getInstance();
        addUser = new JButton(actions.addUserAction);
        addUser.setText("");
        addTask = new JButton(actions.addTaskAction);
        addTask.setText("");
        addSubTask = new JButton(actions.addSubTaskAction);
        addSubTask.setText("");
        upArrow = new JButton(actions.moveUpAction);
        upArrow.setText("");
        downArrow = new JButton(actions.moveDownAction);
        downArrow.setText("");
        leftArrow = new JButton(actions.moveLeftAction);
        leftArrow.setText("");
        rightArrow = new JButton(actions.moveRightAction);
        rightArrow.setText("");
        move = new JButton(actions.moveAction);
        move.setText("");
        share = new JButton(actions.shareAction);
        share.setText("");
        remove = new JButton(actions.removeAction);
        remove.setText("");
    }
    public void showTo(Container c) {
        if (Options.isNetworkClient()) {
            c.add(addUser);
        }
        c.add(addTask);
        c.add(addSubTask);
        c.add(upArrow);
        c.add(downArrow);
        c.add(leftArrow);
        c.add(rightArrow);
        c.add(move);
        if (Options.isNetworkClient()) {
            c.add(share);
        }
        c.add(remove);
        c.add(ActiveTaskButton.getButton());
        if (Options.isDebugMode()) {
            c.add(getDebugInfo());
        }
        if (Options.isNetworkClient()) {
            c.add(new JButton(Menu.getInstance().dataExchange));
        }
    }

    private Component getDebugInfo() {
        JButton debug = new JButton("debug");
        debug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreeElement node = TreePanel.getSelectedNode();
                if (node == null) {
                    System.out.println("Active node is null");
                } else {
                    node.showDebugInfo();
                }
            }
        });
        return debug;
    }
}
