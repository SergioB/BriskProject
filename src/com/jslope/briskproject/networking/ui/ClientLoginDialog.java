package com.jslope.briskproject.networking.ui;

import com.jslope.briskproject.networking.LoginData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Date: 12.10.2005
 */
public class ClientLoginDialog extends JDialog {
    private JTextField login;
    private JPasswordField password;

    private boolean wasOk = false;

    public boolean wasClickedOk() {
        return wasOk;
    }


    public ClientLoginDialog(JFrame parentFrame) {
        super(parentFrame, "Please fill login data", true);
        setLocationRelativeTo(parentFrame);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Please log in:");
        rootPanel.add(label);

        JPanel panel = new JPanel(new FormLayout());
//        JPanel panel = new JPanel(new GridLayout(0, 2));
        label = new JLabel("Login:");
        panel.add(label);
        login = new JTextField(20);
        panel.add(login);
        label.setLabelFor(login);

        label = new JLabel("Password:");
        panel.add(label);
        password = new JPasswordField(20);
        panel.add(password);
        label.setLabelFor(password);

        password.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okAction();
                }
            }
        });


        rootPanel.add(panel);

        //generating buttons:
        JButton ok = new JButton("Ok");
        ok.setMnemonic(KeyEvent.VK_O);

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okAction();
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
        rootPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        rootPanel.add(buttonPanel);

        //adding handling of Esc key:
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        rootPanel.getInputMap().put(stroke, "ESCAPE");
        rootPanel.getActionMap().put("ESCAPE", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        rootPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        setContentPane(rootPanel);
        this.addWindowStateListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                System.out.println("setting focus");
                login.requestFocusInWindow();
            }
        });
    }

    private void okAction() {
        String login;
        login = this.login.getText().trim();
        if (login.equals("")) {
            error("Please enter login");
        } else {
            wasOk = true;
            setVisible(false);
        }
        return;
    }

    private void error(String errorMessage) {
        JOptionPane.showMessageDialog(this,
                errorMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void focusComponents() {
        login.requestFocusInWindow();
    }

    public LoginData getLoginData() {
        return new LoginData(login.getText(), password.getPassword());
    }

    /**
     * for debugging
     *
     * @param args
     */
    public static void main(String[] args) {
        ClientLoginDialog sd = new ClientLoginDialog(null);
        sd.pack();
        sd.login.requestFocusInWindow();
        sd.setVisible(true);
        System.exit(0);
    }
}
