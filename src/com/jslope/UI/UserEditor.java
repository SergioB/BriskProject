package com.jslope.UI;

import static com.jslope.utils.UIUtils.linePanel;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Options;
import com.jslope.utils.Utils;
import com.jslope.UI.components.FixedSizePlainDocument;
import com.jslope.UI.components.JTextFieldMenu;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Date: 09.08.2005
 */
public class UserEditor {

    JPanel panel;
    private JLabel timeSpent;
    private JLabel login;
//    private JComboBox accessLevel;

    public JPanel getPanel() {
        return panel;
    }

    private static UserEditor ourInstance = new UserEditor();
    private JTextField name;

    UserEditor() {
        panel = generatePanel();
    }

    public static UserEditor getInstance() {
        return ourInstance;
    }

    JPanel generatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
//        panel.setLayout(new GridBagLayout());
        //editor for name:
        JLabel label = new JLabel("Name:");
        label.setDisplayedMnemonic('N');
        label.setLabelFor(name);
        name = new JTextFieldMenu(50);
        name.setDocument(new FixedSizePlainDocument(User.MAX_NAME_LENGHT));
        name.setMaximumSize(new Dimension(3350, 30));
        panel.add(linePanel(label, name));
        name.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (!updatingUser) {
                    user.set(User.NAME, name.getText());
                    TaskTreeModel.getInstance().fireTreeNodeChanged(user);
                }
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

        });

        //editor for login/password
        label = new JLabel("login: ");
        login = new JLabel(" login ");
//        accessLevel = new JComboBox();
        if (Options.isNetworkClient()) {
            panel.add(linePanel(label, login));
//            JPanel accessPanel = new  JPanel();
//            accessPanel.setLayout(new BoxLayout(accessPanel, BoxLayout.LINE_AXIS));
//            accessPanel.add(new JLabel("Access level: "));
//            accessPanel.add(accessLevel);
//            accessPanel.setMaximumSize(new Dimension(800, 30));
//            panel.add(accessPanel);
        }

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.add(new JLabel("Time worked: "));
        timeSpent = new JLabel();
        labelPanel.add(timeSpent);
        panel.add(labelPanel);
        Box buttonPanel = Box.createHorizontalBox();
        JButton dailyButton = new JButton("Daily Report");
        dailyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDailyReport();
            }
        });
        buttonPanel.add(dailyButton);
        JButton monthlyButton = new JButton("Monthly Report");
        monthlyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showMonthlyReport();
            }
        });
        buttonPanel.add(monthlyButton);
        panel.add(buttonPanel);
        return panel;
    }

    private void showMonthlyReport() {
        MonthlyReport.showHistory(user);
    }

    private void showDailyReport() {
        DailyReport.showHistory(user);
    }

    public static JPanel getUserPanel() {
        return getInstance().panel;
    }

    User user = null;
    boolean updatingUser = false;   // this is needed so that user modified time will not be changed (because there are 2 document operation it first clears document then it adds new value (even if it's the same value)

    public void update(User user) {
        updatingUser = true;
        this.user = user;
        name.setText(user.get(User.NAME));
        login.setText(user.get(User.LOGIN));
        timeSpent.setText(Utils.formatTime(user.getWorkedTime()) + " in past " + Options.getInstance().getUserShowTime() + " days");
//        updateAccessLevel(user);
        updatingUser = false;
    }

//    private void updateAccessLevel(User user) {
//        accessLevel.removeAllItems();
//        for (User us : user.getAccessibleParents()) {
//            accessLevel.addItem(us);
//        }
//        accessLevel.setSelectedItem(user.getAccessLevel());
//    }

    public void updateData(User user) {
        user.set(User.NAME, name.getText());
//        user.setAccessLevel((User)accessLevel.getSelectedItem());
        user.save();
    }
}
