package com.jslope.UI;

import com.jslope.toDoList.core.ReportGenerator;
import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.interfaces.TaskIntervalInterface;
import com.jslope.persistence.LoadException;
import com.jslope.utils.Utils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 *  Date: 19.05.2005
 */
public class DailyReport extends AbstractReport {

    private static AbstractReport instance = null;
    private int today;

    public static AbstractReport getInstance() {
        if (instance == null) {
            instance = new DailyReport();
        }
        try {
            instance.reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void init() {
        northPanel = getNorthPanel();
    }

    int timeThisMonth;
    protected void reload() throws LoadException {
        today = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = showingDate.get(Calendar.DAY_OF_WEEK);

        timeThisMonth = 0;
        JComponent comp;
        comp = getOneDayPanel(showingDate.get(Calendar.DAY_OF_MONTH), daysOfWeek[dayOfWeek - 1]);

        JScrollPane scrollPane = new JScrollPane(comp);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(BorderLayout.CENTER, scrollPane);
        mainPanel.add(BorderLayout.NORTH, northPanel);
        setContentPane(mainPanel);
        date.setText(currentDateRepresentation());
        this.pack();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    String daysOfWeek[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    protected void showPrevious() {
        showingDate.add(Calendar.DAY_OF_MONTH, -1);

        try {
            reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }

    protected void showNext() {
        showingDate.add(Calendar.DAY_OF_MONTH, +1);
        try {
            reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }

    protected String currentDateRepresentation() {
        Date date = showingDate.getTime();
        return formatter.format(date);
    }

    class TaskAction implements ActionListener {
        private Task task;
        TaskAction(Task task) {
            this.task = task;
        }

        public void actionPerformed(ActionEvent e) {
            ReportNodeDialog dialog = ReportNodeDialog.getInstance(getThis());
            dialog.showTask(task);
            if (dialog.showTaskInTree()) {
                TreePanel.selectNode(task);
                MainWindow.getInstance().toFront();
            }
        }
    }
    private JComponent getOneDayPanel(int dayNumber, String dayName) throws LoadException {
        JPanel panel = new JPanel(new GridLayout(0, 1));
//        Box panel = Box.createVerticalBox();
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(dayName + " " + dayNumber),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JButton taskButton;
        int totalPerDay = 0;
        ReportGenerator.setFilter(filterNode, filterUser);
        int month, year;
        month = showingDate.get(Calendar.MONTH);
        year = showingDate.get(Calendar.YEAR);

        for (TaskIntervalInterface taskInterval : ReportGenerator.getDayElements(dayNumber, month,year)) {
            taskButton = new JButton(taskInterval.getCompactSubject());
            taskButton.setFont(new Font("Monospaced", Font.PLAIN, 12));
            taskButton.setToolTipText(taskInterval.getDescription());
            taskButton.addActionListener(new TaskAction(taskInterval.getTask()));
            panel.add(taskButton);
            totalPerDay += taskInterval.getDuartion();
        }
        if (totalPerDay > 0) {
            JLabel label1 = new JLabel(Utils.formatString("Total this day:", 15) + " "
                    + Utils.formatTime(totalPerDay));
            label1.setFont(new Font("Monospaced", Font.PLAIN, 12));
            panel.add(label1);
            timeThisMonth += totalPerDay;
        }
        panel.setOpaque(true);
        if (dayNumber == today) {
            panel.setBackground(Color.ORANGE);
        } else {
            panel.setBackground(Color.CYAN);
        }
        return panel;
    }

    private Calendar showingDate;

    private DailyReport() {
        super("Daily Report");
        showingDate = new GregorianCalendar();
        this.init();
    }

    public static void showHistory() {
        AbstractReport win = DailyReport.getInstance();
        win.setVisible(true);
    }
    public static void showHistory(User user) {
        AbstractReport win = DailyReport.getInstance();
        win.setUser(user);
        win.reload();
        win.setVisible(true);
    }

}
