package com.jslope.UI;

import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.Options;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Hashtable;

/**
 * Date: 14.08.2005
 */
public class ChooseNodeDialog extends JDialog {
    private JLabel description;
    private JTree tree;
    private boolean wasOk;

    public TreeElement getSelectedValue() {
        return selectedValue;
    }

    TreeElement selectedValue;

    /**
     * Warning!!! now works only when Options.isShowingUsers() == true 
     * @param text
     * @return tree element
     */
    static TreeElement chooseUser(String text) {
        return chooseUser(MainWindow.getInstance(), text);
    }
    static TreeElement chooseUser(JFrame historyWindow, String text) {
        boolean oldShowingTasks = Options.isShowingTasks();
        Options.setShowTasks(false);
        TaskTreeModel.getInstance().saveRootNode();
        TaskTreeModel.getInstance().reloadTree();
        TreeElement retValue = chooseNode(historyWindow, text);
        Options.setShowTasks(oldShowingTasks);
        TaskTreeModel.getInstance().reloadTree();
        TreePanel.loadExpansionsRoot();
        return retValue;
    }

    /**
     * returns selected node or null if nothing was selected;
     *
     * @param text - message for user (what for he needs a node)
     * @return choosed node
     */
    static TreeElement chooseNode(String text) {
        return chooseNode(MainWindow.getInstance(), text);
    }
    public static TreeElement chooseNode(JFrame historyWindow, String text) {
        ChooseNodeDialog dialog = getInstance(historyWindow);
        dialog.initDialog(text);
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.wasClickedOk()) {
            return dialog.getSelectedValue();
        } else {
            return null;
        }
    }

    public boolean wasClickedOk() {
        return wasOk;
    }

    private void initDialog(String text) {
        selectedValue = null;
        wasOk = false;
        setTitle(text);
        description.setText(text);
        TreePanel.loadExpansions(tree, (TreeElement)TaskTreeModel.getInstance().getRoot());
    }

    private ChooseNodeDialog(JFrame parentFrame) {
        super(parentFrame, "Create Report", true);
        setLocationRelativeTo(parentFrame);

        //this code is to make dialog show the bottom buttons which it doesn't do in some cases
        Point p = getLocation();
        p.setLocation(p.getX(), 100 );
        setLocation(p);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        description = new JLabel("Report interval:", JLabel.CENTER);
        description.setDisplayedMnemonic('t');
        pane.add(description);

        JPanel intervalPane = new JPanel();
        intervalPane.setLayout(new BoxLayout(intervalPane, BoxLayout.LINE_AXIS));
        tree = new JTree(TaskTreeModel.getInstance());
        tree.setCellRenderer(new TaskTreeCellRenerer());

        description.setLabelFor(tree);

        intervalPane.add(new JScrollPane(tree));
        intervalPane.add(Box.createRigidArea(new Dimension(10, 0)));
        pane.add(intervalPane);

        //generating buttons:
        JButton ok = new JButton("Ok");
        ok.setMnemonic(KeyEvent.VK_O);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path == null) {
                    JOptionPane.showMessageDialog(getThis(),
                            "Please select a node",
                            "Node is not selcted",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    selectedValue = (TreeElement) (path.getLastPathComponent());
                    wasOk = true;
                    setVisible(false);
                }
            }
        });
        JButton cancel = new JButton("cancel");
        cancel.setMnemonic(KeyEvent.VK_C);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(ok);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancel);
        pane.add(Box.createRigidArea(new Dimension(10, 10)));
        pane.add(buttonPanel);

        //adding handling of Esc key:
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        pane.getInputMap().put(stroke, "ESCAPE");
        pane.getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        setContentPane(pane);
    }

    private static Map<JFrame, ChooseNodeDialog> ourDialogs = new Hashtable<JFrame, ChooseNodeDialog>();
    private static ChooseNodeDialog getInstance(JFrame frame) {
        if (!ourDialogs.containsKey(frame)) {
            ourDialogs.put(frame, new ChooseNodeDialog(frame));
        }
        return ourDialogs.get(frame);
    }

    JDialog getThis() {
        return this;
    }

}
