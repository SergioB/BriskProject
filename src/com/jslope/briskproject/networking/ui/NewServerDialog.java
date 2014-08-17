package com.jslope.briskproject.networking.ui;

import com.jslope.briskproject.networking.LoginData;
import com.jslope.toDoList.core.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Date: 12.10.2005
 */
public class NewServerDialog extends JDialog {
    private JTextField login;
    private JPasswordField password;
    private JPasswordField confirmPassword;

    private boolean wasOk = false;

    public boolean wasClickedOk() {
        return wasOk;
    }


    public NewServerDialog(JFrame parentFrame, String text) {
        super(parentFrame, "Please fill login data", true);
        setLocationRelativeTo(parentFrame);

        JPanel rootPanel = new JPanel();
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        JTextArea header = new JTextArea(text);
        header.setLineWrap(true);
        header.setWrapStyleWord(true);
        header.setEnabled(false);
        header.setDisabledTextColor(Color.BLACK);
        header.setBackground(this.getBackground());
        header.setFocusable(false);
//        JLabel head = new JLabel("<html>Remote server is not configured, now you should fill login information," +
//                " after which you'll become server administrator</html>");
        rootPanel.add(header);

        JPanel panel = new JPanel(new FormLayout());
//        JPanel panel = new JPanel(new GridLayout(0, 2));
        JLabel label = new JLabel("Login:");
        panel.add(label);
        login = new JTextField(20);
        panel.add(login);
        label.setLabelFor(login);

        label = new JLabel("Password:");
        panel.add(label);
        password = new JPasswordField(20);
        panel.add(password);
        label.setLabelFor(password);

        label = new JLabel("Confirm Password:");
        panel.add(label);
        confirmPassword = new JPasswordField(20);
        panel.add(confirmPassword);
        label.setLabelFor(confirmPassword);
        confirmPassword.addKeyListener(new KeyAdapter() {
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
        String login, password1, password2;
        login = this.login.getText().trim();
        password1 = new String(this.password.getPassword());
        password2 = new String(this.confirmPassword.getPassword());
        if (login.equals("")) {
            error("Please enter login");
        } else if (password1.equals("")) {
            error("Please enter password");
        } else if (!password1.equals(password2)) {
            error("Passwords don't match");
        } else if (login.length() > User.MAX_LOGIN_LEN) {
            error("Login is too big");
        } else if (password1.length() > User.MAX_PASSWORD_LEN) {
            error("Password is too big");
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
        NewServerDialog sd = new NewServerDialog(null, "text");
        sd.pack();
        sd.login.requestFocusInWindow();
        sd.setVisible(true);
        System.exit(0);
    }

    public void setVisible(boolean value) {
        if (value == true) {//if we show this dialog, then we initialize wasOk
            this.wasOk = false;
        }
        super.setVisible(value);
    }
}
