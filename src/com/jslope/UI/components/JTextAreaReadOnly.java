package com.jslope.UI.components;

import static com.jslope.UI.components.JTextFieldMenu.addInsClipboard;

import javax.swing.*;

/**
 * Standart JTextArea with popup menu for edtic/copy paste
 * Date: 14.09.2005
 */
public class JTextAreaReadOnly extends JTextAreaMenu {
    public JTextAreaReadOnly() {
        super();
        this.setEditable(false);        
    }
}
