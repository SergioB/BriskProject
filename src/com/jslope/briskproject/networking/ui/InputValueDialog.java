package com.jslope.briskproject.networking.ui;

import com.jslope.toDoList.core.TreeElement;
import com.jslope.UI.TreePanel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;

/**
 * Date: 09.10.2005
 */
public class InputValueDialog extends JDialog {
    private JTextField value;
    private boolean wasOk = false;

    public boolean wasClickedOk() {
        return wasOk;
    }

    InputValueDialog(JFrame parent, String title, String defaultValue) {
        super(parent, title, true);
        setLocationRelativeTo(parent);
        value = new JTextField(defaultValue);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        JLabel description = new JLabel(title, JLabel.CENTER);
        pane.add(description);
        pane.add(value);
        value.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okOperation();
                }
            }
        });

        //generating buttons:
        JButton ok = new JButton("Ok");
        ok.setMnemonic(KeyEvent.VK_O);

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okOperation();
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

    private void okOperation() {
        wasOk = true;
        setVisible(false);
    }

    public static String enterValue(JFrame frame, String title, String host) {
        InputValueDialog dialog = new InputValueDialog(frame, title, host);
        dialog.pack();
        dialog.value.requestFocusInWindow();
        dialog.setVisible(true);
        String retValue = dialog.getValue();
        if (!dialog.wasClickedOk()) {
            retValue = null;
        }
        return retValue;
    }

    private String getValue() {
        return value.getText();
    }
}
