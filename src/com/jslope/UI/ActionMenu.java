/**
 * Date: 14.08.2005
 */
package com.jslope.UI;

import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.Options;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.Vector;
import java.util.List;

public class ActionMenu {
    private static ActionMenu ourInstance = new ActionMenu();

    private static JButton hideUsers;
    private static JButton hideTasks;
    private static final String HIDE_TASKS = "hide tasks";
    private static final String HIDE_USERS = "hide users";


    public AbstractAction addTaskAction;
    public AbstractAction addSubTaskAction;
    public AbstractAction moveUpAction;
    public AbstractAction moveDownAction;
    public AbstractAction moveLeftAction;
    public AbstractAction moveRightAction;
    public AbstractAction removeAction;
    public AbstractAction moveAction;
    public AbstractAction shareAction;
    public AbstractAction addUserAction;
    private AbstractAction taskDoneAction;
    private AbstractAction reopenAction;
    private AbstractAction pauseAction;

    public static ActionMenu getInstance() {
        return ourInstance;
    }

    JMenu menu;
    JPopupMenu popupMenu;

    private ActionMenu() {
        menu = new JMenu("Actions");
        menu.setMnemonic(KeyEvent.VK_A);
        popupMenu = new JPopupMenu();

        MenuGenerator generator = new MenuGenerator();
        addUserAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.addUser();
            }
        };
        if (Options.isNetworkClient()) {
            VirtualMenuItem addUser = new VirtualMenuItem("Add user", Buttons.addUserIcon);
            addUser.setAction(addUserAction);
            addUser.setTooltip("Add user");
            generator.add(addUser);
        }

        addTaskAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.addTask();
            }
        };

        VirtualMenuItem addTask = new VirtualMenuItem("Add task (Enter)", Buttons.addTaskIcon);
        addTask.setAction(addTaskAction);
        addTask.setTooltip("Add task at the same level");
        generator.add(addTask);

        addSubTaskAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.addSubTask();
            }
        };

        VirtualMenuItem addSubTask = new VirtualMenuItem("Add SubTask (Ins)", Buttons.addSubTaskIcon);
        addSubTask.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        addSubTask.setAction(addSubTaskAction);
        addSubTask.setTooltip("Add subtask");
        generator.add(addSubTask);

        moveUpAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.moveItemUp();
            }
        };
        VirtualMenuItem up = new VirtualMenuItem("Up", Buttons.upImage);
        up.setAction(moveUpAction);
        up.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));
        up.setTooltip("Move up");
        generator.add(up);

        moveDownAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.moveItemDown();
            }
        };
        VirtualMenuItem down = new VirtualMenuItem("Down", Buttons.downImage);
        down.setAction(moveDownAction);
        down.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));
        down.setTooltip("Move down");
        generator.add(down);

        moveLeftAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.moveItemLeft();
            }
        };
        VirtualMenuItem left = new VirtualMenuItem("Left (Ctrl + Left)", Buttons.leftImage);
        left.setAction(moveLeftAction);
        left.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));
        left.setTooltip("Move left");
        generator.add(left);

        moveRightAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.moveItemIn();
            }
        };
        VirtualMenuItem right = new VirtualMenuItem("Right ", Buttons.rightImage);
        right.setAction(moveRightAction);
        right.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));
        right.setTooltip("Move right");
        generator.add(right);

        shareAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.shareItem();
            }
        };

        VirtualMenuItem share = new VirtualMenuItem("Share", Buttons.shareImage);
        share.setAction(shareAction);
        share.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
        share.setTooltip("Share with another user");
        generator.add(share);


        moveAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.moveItem();
            }
        };
        VirtualMenuItem move = new VirtualMenuItem("Move", Buttons.moveImage);
        move.setAction(moveAction);
        move.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
        move.setTooltip("Move to arbitrary location");
        generator.add(move);

        removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                TreePanel.removeElement();
            }
        };
        VirtualMenuItem remove = new VirtualMenuItem("Remove Task", Buttons.delImage);
        remove.setAction(removeAction);
        remove.setTooltip("Remove");
        generator.add(remove);

        VirtualMenuItem pause = new VirtualMenuItem();
        pauseAction = OneTaskPanel.getPauseAction();
        pause.setAction(pauseAction);
        generator.add(pause);

        VirtualMenuItem taskDone = new VirtualMenuItem();
        taskDoneAction = OneTaskPanel.getTaskDoneAction();
        taskDone.setAction(taskDoneAction);
        generator.add(taskDone);

        VirtualMenuItem reopenTask = new VirtualMenuItem();
        reopenAction = TaskDoneEditor.getReopenAction();
        reopenTask.setAction(reopenAction);
        generator.add(reopenTask);

        generator.populate(menu);
        JMenu tmpMenu = new JMenu();
        generator.populate(tmpMenu);
        popupMenu = tmpMenu.getPopupMenu();
    }


    public void setPermisions(TreeElement node) {
        addUserAction.setEnabled(node.canAddUser());
        moveDownAction.setEnabled(node.canMoveDown());
        moveUpAction.setEnabled(node.canMoveUp());
        moveLeftAction.setEnabled(node.canMoveLeft());
        moveRightAction.setEnabled(node.canMoveRight());
        if (Options.isShowingTasks()) {
            addTaskAction.setEnabled(node.canAddSiblingTask());
            addSubTaskAction.setEnabled(node.canAddSubtask());
        } else {
            addTaskAction.setEnabled(false);
            addSubTaskAction.setEnabled(false);
        }

        moveAction.setEnabled(node.canMove());
        removeAction.setEnabled(node.canDelete());
        shareAction.setEnabled(node.canBeShared());
        pauseAction.setEnabled(node.canBePaused());
        taskDoneAction.setEnabled(node.canDoTaskDone());
        reopenAction.setEnabled(node.canReopen());
    }

    public static void addFilterButtons(JPanel labelPanel) {
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        hideUsers = new JButton(HIDE_USERS);
        hideTasks = new JButton(HIDE_TASKS);
        hideTasks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideTasks();
            }
        });
        hideUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideUsers();
            }
        });

        buttonsPanel.add(hideUsers);
        buttonsPanel.add(hideTasks);
        buttonsPanel.setMaximumSize(new Dimension(500, 20));
        labelPanel.add(buttonsPanel);
        labelPanel.add(Box.createHorizontalGlue());
    }

    private static void hideUsers() {
        Options.setShowUsers(!Options.isShowingUsers());
        if (Options.isShowingUsers()) {
            TaskTreeModel.getInstance().setUserRoot();
            hideUsers.setText(HIDE_USERS);
            hideTasks.setEnabled(true);
        } else {
            TaskTreeModel.getInstance().setTaskRoot();
            hideUsers.setText("show users");
            hideTasks.setEnabled(false);
        }
        TreePanel.reloadTree();
    }

    private static void hideTasks() {
        Options.setShowTasks(!Options.isShowingTasks());
        TreePanel.reloadTree();
        if (Options.isShowingTasks()) {
            hideTasks.setText(HIDE_TASKS);
            hideUsers.setEnabled(true);
        } else {
            hideTasks.setText("show tasks");
            hideUsers.setEnabled(false);
        }
    }

    class VirtualMenuItem {
        String title = null;
        ImageIcon image = null;
        KeyStroke accelerator = null;
        AbstractAction action = null;
        private String toolTip = null;

        VirtualMenuItem() {
        }

        VirtualMenuItem(String ttl, ImageIcon img) {
            title = ttl;
            image = img;
        }

        public void setAccelerator(KeyStroke newAccelerator) {
            accelerator = newAccelerator;
        }

        public void setAction(AbstractAction action) {
            this.action = action;
        }

        public void setTooltip(String toolTip) {
            this.toolTip = toolTip;
        }

        public JMenuItem generateElement() {
            JMenuItem item = new JMenuItem(title, image);
            if (accelerator != null) {
                item.setAccelerator(accelerator);
            }
            if (action != null) {
                if (title != null) {
                    action.putValue(Action.NAME, title);
                }
                if (image != null) {
                    action.putValue(Action.SMALL_ICON, image);
                }
                if (accelerator != null) {
                    action.putValue(Action.ACCELERATOR_KEY, accelerator);
                }
                if (toolTip != null) {
                    action.putValue(Action.SHORT_DESCRIPTION, toolTip);
                }
                item.setAction(action);
            }
            return item;
        }
    }

    class MenuGenerator {
        List<VirtualMenuItem> elements = new Vector<VirtualMenuItem>();

        public void add(VirtualMenuItem newElement) {
            elements.add(newElement);
        }

        public void populate(JMenu menu) {
            for (VirtualMenuItem menuItem : elements) {
                JMenuItem item = menuItem.generateElement();
                menu.add(item);
            }
        }
    }

    public static JPopupMenu getPopupMenu() {
        return ourInstance.popupMenu;
    }

    public static JMenu getMenu() {
        return ourInstance.menu;
    }
}
