package com.jslope.UI;

import com.jslope.persistence.LoadException;
import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.User;
import com.jslope.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;

/**
 * Date: May 17, 2006
 * Time: 2:20:26 PM
 */
public abstract class AbstractReport extends JFrame {
    private JButton filterButton;
    private JButton userButton;
    JPanel  northPanel;
    SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
    JLabel date;
    TreeElement filterNode = null;
    User filterUser = null;

    public AbstractReport(String title) throws HeadlessException {
        super(title);
    }

    protected abstract void reload() throws LoadException;

    protected JPanel getNorthPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 7));
        JButton left = new JButton("<");
        left.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPrevious();
            }
        });
        JButton right = new JButton(">");
        right.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showNext();
            }
        });
        date = new JLabel(currentDateRepresentation());
        date.setHorizontalAlignment(JLabel.CENTER);

        panel.add(left);
        panel.add(date);
        panel.add(right);

        filterButton = new JButton("No filter");
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setFilter();
            }
        });
        filterButton.setToolTipText("Click to set filter");
        panel.add(filterButton);

        userButton = new JButton("No user");
        userButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUser();
            }
        });
        userButton.setToolTipText("Click for user report");
        panel.add(userButton);
        return panel;
    }

    private void setFilter() {
        TreeElement newFilterNode = ChooseNodeDialog.chooseNode(this, "Select node for filter");
        if (newFilterNode != null) {
            filterButton.setText(Utils.formatString(newFilterNode.toString(), 20));
        } else {
            filterButton.setText("No filter");
        }
        filterNode = newFilterNode;
        reload();
    }

    private void setUser() {
        User user = (User)ChooseNodeDialog.chooseUser(this, "Select user for report");
        setUser(user);
        reload();
    }

    protected void setUser(User user) {
        if (user == null) {
            userButton.setText("No user");
        } else {
            userButton.setText(user.getName());
        }
        filterUser = user;
    }

    protected abstract void showPrevious();

    protected abstract void showNext();

    protected abstract String currentDateRepresentation();

    AbstractReport getThis() {
        return this;
    }
}
