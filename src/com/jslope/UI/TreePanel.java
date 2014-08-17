package com.jslope.UI;

import com.jslope.toDoList.core.*;
import com.jslope.briskproject.networking.ui.NewServerDialog;
import com.jslope.briskproject.networking.LoginData;
import com.jslope.briskproject.networking.ClientProtocol;
import com.jslope.utils.Dialogs;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * Date: 13.07.2005
 * It just creates a panel for tree
 */
public class TreePanel {
    static private JSplitPane splitPane;
    private static JTree tree;
    private static JPopupMenu popup;
    private static TaskTreeModel treeModel;
    private static TreeElement oldSelectedNode = null;

    public static Component getTreePanel() {
        tree = new JTree(TaskTreeModel.getInstance());
        treeModel = (TaskTreeModel) tree.getModel();

        tree.setCellRenderer(new TaskTreeCellRenerer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreeElement node = (TreeElement) tree.getLastSelectedPathComponent();
                if (node != null) {
                    if (oldSelectedNode != null) {
                        oldSelectedNode.updateDataFromEditor();
                        treeModel.fireTreeNodeChanged(oldSelectedNode);
                    }
                    node.updateEditor();
                    ActionMenu.getInstance().setPermisions(node);
                    if (Options.isDebugMode()) {
                        System.out.println("Is Modified: " + node.get(TreeElement.IS_MODIFIED) + " id=" + node.getID());
//                        if (node instanceof Task) {
//                            ((Task)node).showTaskIntervals();
//                        }
                    }
                    oldSelectedNode = node;
                }
            }
        });
        popupInit();
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                checkPopup(e);
            }

            public void mousePressed(MouseEvent e) {
                checkPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                checkPopup(e);
            }
        });

        tree.addTreeExpansionListener(new TreeExpansionListener() {

            public void treeExpanded(TreeExpansionEvent event) {
                ((TreeElement) event.getPath().getLastPathComponent()).setExpanded(true);
            }

            public void treeCollapsed(TreeExpansionEvent event) {
                ((TreeElement) event.getPath().getLastPathComponent()).setExpanded(false);
            }
        });

        ActionMenu actions = ActionMenu.getInstance();
        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
        tree.getActionMap().put("Enter", actions.addTaskAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "Insert");
        tree.getActionMap().put("Insert", actions.addSubTaskAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
        tree.getActionMap().put("Delete", actions.removeAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), "ctrlUP");
        tree.getActionMap().put("ctrlUP", actions.moveUpAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), "ctrlDown");
        tree.getActionMap().put("ctrlDown", actions.moveDownAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "ctrlLeft");
        tree.getActionMap().put("ctrlLeft", actions.moveLeftAction);

        tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "ctrlRight");
        tree.getActionMap().put("ctrlRight", actions.moveRightAction);

        loadExpansionsRoot();
        tree.setSelectionRow(0);

        JScrollPane treeView = new JScrollPane(tree);


        JPanel treePane = new JPanel(new BorderLayout());
        treePane.add(BorderLayout.CENTER, treeView);

        JLabel treeLabel = new JLabel("Edit project tree:");
        treeLabel.setDisplayedMnemonic('t');
        treeLabel.setLabelFor(tree);
        treeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel labelPanel = new JPanel(new GridLayout(1, 1));
        if (Options.isNetworkClient()) {
            ActionMenu.addFilterButtons(labelPanel);
            labelPanel.setMaximumSize(new Dimension(200, 30));
        }
        labelPanel.add(treeLabel);
        treePane.add(BorderLayout.NORTH, labelPanel);

        TreeElement selectedNode = Options.getInstance().getSelectedNode();
        if (selectedNode != null) {
            selectNode(selectedNode);
        }
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treePane, EditorsPanel.getInstance().getPanel());
        splitPane.setDividerLocation(Options.getInstance().getSplitPane());


        return splitPane;
    }

    public static void loadExpansionsRoot() {
        loadExpansions((TreeElement) treeModel.getRoot());
    }

    public static void loadExpansions(TreeElement node) {
        loadExpansions(tree, node);
    }

    public static void loadExpansions(JTree tree, TreeElement node) {
        if (node.isExpanded()) {
            tree.expandPath(node.getPath());
            for (TreeElement child : node.getChildren()) {
                loadExpansions(tree, child);
            }
        }
    }

    private static void popupInit() {
        popup = ActionMenu.getPopupMenu();
    }


    public static void selectNode(TreeElement node) {
        TreePath path = node.getPath();
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    public static void moveItemUp() {
        TreeElement currentNode = getSelectedNode();
        if (currentNode != null) {
            if (currentNode.canMoveUp()) {
                currentNode.moveUp();
                reload(currentNode.getParent());
                selectNode(currentNode);
            }
        }
    }

    public static void moveItemDown() {
        TreeElement currentNode = getSelectedNode();
        if (currentNode != null) {
            if (currentNode.canMoveDown()) {
                currentNode.moveDown();
                reload(currentNode.getParent());
                selectNode(currentNode);
            }
        }
    }

    public static void moveItemLeft() {
        TreeElement currentNode = getSelectedNode();
        if (currentNode != null) {
            if (currentNode.canMoveLeft()) {
                currentNode.moveLeft();
                reload(currentNode.getParent());
                selectNode(currentNode);
            }
        }
    }

    public static void moveItemIn() {
        TreeElement currentNode = getSelectedNode();
        if (currentNode != null) {
            if (currentNode.canMoveRight()) {
                TreeElement nodeToBeUpdated = currentNode.getParent();
                currentNode.moveIn();
                reload(nodeToBeUpdated);
                selectNode(currentNode);
            }
        }
    }

    public static void reload(TreeElement nodeToBeUpdated) {
        treeModel.reload(nodeToBeUpdated);
        loadExpansions(nodeToBeUpdated);
    }

    public static TreeElement getSelectedNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection == null) {
            return null;
        }
        return (TreeElement) (currentSelection.getLastPathComponent());
    }

    public static void removeElement() {
        TreeElement currentNode = getSelectedNode();
        if (currentNode != null) {
            TreeElement parentNode = currentNode.getParent();
            TreeElement nextNode = currentNode.selectNextNode();
            if (currentNode != treeModel.getRoot()) {
                int answer = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                        "Are you sure you would like to delete " + currentNode + "?",
                        "Confirm task deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    currentNode.virtualDelete();
                    if (Options.isNetworkClient()) {
                        if (currentNode instanceof User) {
                            reload(parentNode);
                        } else {
                            reloadTree(); //it is done in case there is a task with children assigned to another users, in this case entire tree must be reloaded.
                        }
                    } else {
                        reload(parentNode);
                    }
                    selectNode(nextNode);
                }
            }
        }
    }

    public static void addSubTask() {
        TreeElement node = (TreeElement) tree.getLastSelectedPathComponent();
        if (node != null) {
            Task newTask = new Task();
            node.add(newTask);
            reload(node);
            selectNode(newTask);
            OneTaskPanel.selectSubject();
        }
    }

    public static void addTask() {
        TreeElement node = (TreeElement) tree.getLastSelectedPathComponent();
        if (node != null) {
            if (node.canAddSiblingTask()) {
                Task newTask = new Task();
                node.addSibling(newTask);
                reload(node.getParent());
                selectNode(newTask);
                OneTaskPanel.selectSubject();
            }
        }
    }

    /**
     * for debug purposes
     *
     * @param path to be shown
     */
    public static void showPath(TreePath path) {
        for (Object obj : path.getPath()) {
            System.out.println(" type = " + obj.getClass() + " obj = " + obj);
        }
    }

    private static void checkPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            TreeElement element = (TreeElement) tree.getClosestPathForLocation(e.getX(), e.getY()).getLastPathComponent();
            if (element != null) {
                selectNode(element);
            }
            popup.show(tree, e.getX(), e.getY());
        }
    }

    static public int getSplitPaneLocation() {
        return splitPane.getDividerLocation();
    }

    public static void moveItem() {
        TreeElement nodeToBeMoved = getSelectedNode();
        TreeElement newParent;
        if (nodeToBeMoved != null) {
            if (nodeToBeMoved instanceof User) {
                newParent = ChooseNodeDialog.chooseUser("Select where to move");
            } else {
                newParent = ChooseNodeDialog.chooseNode("Select where to move");
            }
            if (newParent != null) {
                if (nodeToBeMoved.hasAsGrandChild(newParent) || nodeToBeMoved.getParent() == newParent) {
                    JOptionPane.showMessageDialog(tree,
                            "You can't move a node to it's own subtree",
                            "Can't move a node",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!newParent.canAcceptNewChildren()) {
                    JOptionPane.showMessageDialog(tree,
                            "You can't move a node there",
                            "Can't move a node",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    nodeToBeMoved.verifyMoveOutOfParent(newParent);
                    nodeToBeMoved.removeFromParent();
                    newParent.add(nodeToBeMoved);
                    reload((TreeElement) treeModel.getRoot());
                    selectNode(nodeToBeMoved);
                }
            }
        }
    }

    public static void reloadTree() {
        TreeElement selectedNode = TreePanel.getSelectedNode();
        saveTree();
        //todo: here is possible memory hog, because tasks which are no longer on screen are kept in cache
        // so it might be better to call something like clearCache
        // but it might pose problem to persistent objects which are not tree elements
        treeModel.loadTree();
        treeModel.reload();
        loadExpansions((TreeElement) treeModel.getRoot());
        if (selectedNode != null) {
            if (selectedNode.isStillInTree()) { //todo: possible that it will be better to try to find closest node if this node is not in tree
                selectNode(selectedNode);
            }
        }
    }

    public static void saveTree() {
        saveCurrentNode();
        treeModel.saveRootNode();
    }

    /**
     * it only saves current node from editor to object, it doesn't save it to database
     */
    public static void saveCurrentNode() {
        TreeElement node = TreePanel.getSelectedNode();
        if (node != null) {
            node.updateDataFromEditor(); // it should update from editor
        }
    }

    public static void addUser() {
        NewServerDialog sd = new NewServerDialog(MainWindow.getInstance(),
                "Please enter login and password for new user");
        sd.pack();
        sd.focusComponents();
        LoginData loginData = null;
        do {
            sd.setVisible(true);
            if (!sd.wasClickedOk()) {
                return;
            }
            loginData = sd.getLoginData();

        } while (!successfullyAddUser(loginData));
    }

    private static boolean successfullyAddUser(LoginData loginData) {
        boolean validate = false;
        try {
            User user = new User();
            user.setLogin(loginData);
            user.set(User.NAME, loginData.getLogin());
            user.set(User.PARENT, getSelectedNode().getID());
            user.save();
            user.setIsModified(false);
            validate = ClientProtocol.addUser(user);
            if (validate) {
                TreeElement node = (TreeElement) tree.getLastSelectedPathComponent();
                if (node != null) {
                    node.add(user);
                    reload(node);
                    selectNode(user);
                }
                user.save();
            } else {
                JOptionPane.showMessageDialog(MainWindow.getInstance(),
                        "This username is already in use",
                        "Username exists",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            Dialogs.unableToConnectMessage();
            e.printStackTrace();
        }
        return validate;
    }

    public static void updateCurrentNode() {
        TreeElement node = (TreeElement) tree.getLastSelectedPathComponent();
        if (node != null) {
            node.updateEditor();
            ActionMenu.getInstance().setPermisions(node);
        }

    }

    public static void selectRootNode() {
        selectNode(Options.getInstance().getRootNode());
    }

    public static void shareItem() {
        TreeElement selected = getSelectedNode();
        if (selected != null) {
            if (selected instanceof Task) {
                User shareWith = (User) ChooseNodeDialog.chooseUser("Select with whom to share");
                selectNode(selected);
                if (shareWith != null) {
                    User tasksParent = selected.getParentUser();
                    if (shareWith.hasAsGrandChild(tasksParent)) {
                        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                                "You can't share task with user who already have access to this task",
                                "Wrong share request",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        System.out.println(" sharing ...");
                        if (TaskSharer.add(selected, shareWith)) {
                            shareWith.addSharedTask((Task) selected);
                        }
                    }
                }
            }
        }
    }
}
