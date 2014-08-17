package com.jslope.UI;

import com.jslope.toDoList.core.ReportGenerator;
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
public class MonthlyReport extends AbstractReport {

    private static MonthlyReport instance = null;
    private final int maxMonth = 11;
    private int today;

    public static MonthlyReport getInstance() {
        if (instance == null) {
            instance = new MonthlyReport();
        }
        try {
            instance.reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
        return instance;
    }

    JPanel  northPanel;
    SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

    private void init() {
        northPanel = getNorthPanel();
    }

    int timeThisMonth;
    protected void reload() throws LoadException {
        JPanel panel = new JPanel(new GridLayout(0, 7));
        Calendar calendar = new GregorianCalendar();
        today = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int i;
        for (i = 1; i < dayOfWeek; i++) {
            panel.add(new Label(""));// adding empty labels at the end
        }
        timeThisMonth = 0;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        JComponent comp, todayComp=null;
        for (i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            comp = getOneDayPanel(i, daysOfWeek[dayOfWeek - 1]);
            panel.add(comp);
            if (i == today) {
                todayComp = comp;
            }
        }
        date.setText(currentDateRepresentation() + " total worked: " + Utils.formatTime(timeThisMonth));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(BorderLayout.CENTER, scrollPane);
        mainPanel.add(BorderLayout.NORTH, northPanel);
        setContentPane(mainPanel);

        todayComp.addComponentListener(new ComponentAdapter(){
            public void componentShown(ComponentEvent e) {
                System.out.println("today component shown");
            }
        });
        this.pack();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Rectangle rec = todayComp.getBounds();
//        rec.setLocation((int)rec.getX()+100,(int)rec.getY()); //move rectangle to the left because not entier day is shown when autoscroll
        panel.scrollRectToVisible(rec);
    }

    String daysOfWeek[] = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};


    protected void showPrevious() {
        month--;
        if (month==0) {
            month = maxMonth;
            year--;
        }
        try {
            reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }

    protected void showNext() {
        month++;
        if (month>maxMonth) {
            month=0;
            year++;
        }
        try {
            reload();
        } catch (LoadException e) {
            e.printStackTrace();
        }
    }

    protected String currentDateRepresentation() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        Date date = cal.getTime();
        return formatter.format(date);
    }
    MonthlyReport getThis() {
        return this;
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
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(dayName + " " + dayNumber),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        JButton taskButton;
        int totalPerDay = 0;
        ReportGenerator.setFilter(filterNode, filterUser);
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

    private int month, year;

    private MonthlyReport() {
        super("Monthly report");
        month = Calendar.getInstance().get(Calendar.MONTH);
        year = Calendar.getInstance().get(Calendar.YEAR);
        System.out.println("month = " + month + " year = " + year);
        this.init();
    }

    public static void showHistory() {
        MonthlyReport win = MonthlyReport.getInstance();
        win.setVisible(true);
    }
    public static void showHistory(User user) {
        MonthlyReport win = MonthlyReport.getInstance();
        win.setUser(user);
        win.reload();
        win.setVisible(true);
    }
}
