package com.jslope.UI.components;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 * Standart JTextField with popup menu for edtic/copy paste
 * Date: 14.09.2005
 */
public class JTextFieldMenu extends JTextField {
    public JTextFieldMenu(int columns) {
        super(columns);
        initThis();
    }

    private void initThis() {
        this.addMouseListener(PopupMouseListener.getInstance());
        addInsClipboard(this);
    }

    public static void addInsClipboard(JTextComponent comp) {  //todo: to move this method to specialized class
        comp.copy();
        comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), "ctrlIns");
        comp.getActionMap().put("ctrlIns", new DefaultEditorKit.CopyAction());

        comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), "ShiftIns");
        comp.getActionMap().put("ShiftIns", new DefaultEditorKit.PasteAction());

        comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), "ShiftDel");
        comp.getActionMap().put("ShiftDel", new DefaultEditorKit.CutAction());
    }
}
