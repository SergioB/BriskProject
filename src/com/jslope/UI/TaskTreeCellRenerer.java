package com.jslope.UI;

import com.jslope.toDoList.core.TreeElement;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Date: 29.08.2005
 */
public class TaskTreeCellRenerer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel,
                expanded, leaf, row,
                hasFocus);
        TreeElement element = (TreeElement) value;
        setIcon(element.getIcon());

        return this;
    }
}
