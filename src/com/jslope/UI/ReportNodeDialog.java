package com.jslope.UI;

import com.jslope.toDoList.core.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Hashtable;

/**
 * Date: 14.08.2005
 */
public class ReportNodeDialog extends JDialog {
    private JLabel subject;
    private JLabel notes;
    private boolean showInTree;


    private ReportNodeDialog(JFrame parentFrame) {
        super(parentFrame, "Create Report", true);
        setLocationRelativeTo(parentFrame);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Subject:");
        pane.add(label);
        subject = new JLabel("subject here");
        pane.add(subject);
        label = new JLabel("Notes:");
        pane.add(label);
        notes = new JLabel("notes here");
        pane.add(notes);


        //generating buttons:
        JButton ok = new JButton("Show in tree");
        ok.setMnemonic(KeyEvent.VK_O);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showInTree = true;
                setVisible(false);
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

    private static Map<JFrame, ReportNodeDialog> ourDialogs = new Hashtable<JFrame, ReportNodeDialog>();

    public static ReportNodeDialog getInstance(JFrame frame) {
        if (!ourDialogs.containsKey(frame)) {
            ourDialogs.put(frame, new ReportNodeDialog(frame));
        }
        return ourDialogs.get(frame);
    }

    JDialog getThis() {
        return this;
    }

    public void showTask(Task task) {
        showInTree = false;
        subject.setText(task.getSubject());
        notes.setText(task.getNotes());
        this.pack();
        setVisible(true);
    }

    public boolean showTaskInTree() {
        return showInTree;
    }
}
