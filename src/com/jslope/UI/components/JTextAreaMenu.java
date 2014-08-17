package com.jslope.UI.components;

import static com.jslope.UI.components.JTextFieldMenu.addInsClipboard;

import javax.swing.*;

/**
 * Standart JTextArea with popup menu for edtic/copy paste
 * Date: 14.09.2005
 */
public class JTextAreaMenu extends JTextArea {
    public JTextAreaMenu() {
        super();
        initThis();
    }

    public JTextAreaMenu(int columns, int rows) {
        super(columns, rows);
        initThis();
    }

    private void initThis() {
        this.addMouseListener(PopupMouseListener.getInstance());
        addInsClipboard(this);
    }
}
