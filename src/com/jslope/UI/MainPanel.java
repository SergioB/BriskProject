package com.jslope.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Date: Mar 26, 2004
 */
public class MainPanel extends JPanel {

    boolean DEBUG = false;


    public MainPanel() {
//        super(new GridLayout(1, 0));
        super(new BorderLayout());

        this.add(BorderLayout.CENTER, TreePanel.getTreePanel());
        add(getToolBar(), BorderLayout.NORTH);
    }

    private JToolBar getToolBar() {
        JToolBar toolBar = new JToolBar();
        ToolBar.getInstance().showTo(toolBar);


        return toolBar;
    }

}
