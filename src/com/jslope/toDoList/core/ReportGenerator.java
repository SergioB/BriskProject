/**
 * Date: 07.08.2005
 */
package com.jslope.toDoList.core;

import com.jslope.toDoList.core.interfaces.TaskIntervalInterface;
import com.jslope.persistence.ObjectList;
import com.jslope.utils.Utils;

import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

public class ReportGenerator {
    private static ReportGenerator ourInstance = new ReportGenerator();
    private static DateFormat formater = new SimpleDateFormat("yyyy/MM/dd");
    private int reportDuration;

    public static ReportGenerator getInstance() {
        return ourInstance;
    }

    private ReportGenerator() {
    }

    static public List<TaskIntervalInterface> getDayElements(int day, int month, int year) {
        return getInstance().getOneDayElements(day, month, year);
    }

    public List<TaskIntervalInterface> getOneDayElements(int day, int month, int year) {
        Calendar cal = new GregorianCalendar(year, month, day);
        return getOneDayElements(cal);
    }

    public List<TaskIntervalInterface> getOneDayElements(Calendar cal) {

        List<TaskIntervalInterface> retValue = new Vector<TaskIntervalInterface>();

        Vector<TimeInterval> taskList = new Vector<TimeInterval>();
        ObjectList timeIntervals = TimeInterval.getTimeIntervalsForOneDay(cal, filterUser);
        while (timeIntervals.hasNext()) {
            TimeInterval interval = (TimeInterval) timeIntervals.next();
            if (filterNode != null) {
                if (filterNode.hasAsGrandChild(interval.getParrentTask())) {
                    taskList.add(interval);
                }
            } else {
                taskList.add(interval);
            }
        }
        int vectorSize = taskList.size(), taskDuration;
        // here we'll concatenate same tasks
        for (int i = 0; i < vectorSize; i++) {
            TimeInterval timeInterval = taskList.get(i);
            taskDuration = timeInterval.getSeconds();
            if (i < vectorSize - 1) {
                TimeInterval nextInterval = taskList.get(i + 1);
                if (timeInterval.sameTask(nextInterval)) {
                    i++;
                    for (; i < vectorSize; i++) {
                        nextInterval = (TimeInterval) taskList.get(i);
                        if (timeInterval.sameTask(nextInterval)) {
                            taskDuration += nextInterval.getSeconds();
                        } else {
                            break;
                        }
                    }
                    i--;
                }
            }
            retValue.add(new TaskDurationInterface(timeInterval, taskDuration));
//            label = new JLabel(timeInterval.getCompactSubject(taskDuration));
//            label.setFont(new Font("Monospaced", Font.PLAIN, 12));
//            label.setToolTipText(timeInterval.getDescription());
//            panel.add(label);
        }

        return retValue;
    }

    private TreeElement filterNode = null;
    private User filterUser = null;

    public static void setFilter(TreeElement filterNode, User user) {
        getInstance().filterNode = filterNode;
        getInstance().filterUser = user;
    }

    class TaskDurationInterface implements TaskIntervalInterface {
        int duration;


        private Task task;
        private TimeInterval timeInterval;

        public Task getTask() {
            return task;
        }

        TaskDurationInterface(TimeInterval timeInterval, int time) {
            this.duration = time;
            this.task = timeInterval.getParrentTask();
            this.timeInterval = timeInterval;
        }

        public String getSubject() {
            return task.getSubject();
        }

        public String getDescription() {
            String notes = task.getNotes();
            if (notes.length() > 0) {
                notes = "<br> " + notes;
            }
            String userString = "";
            if (Options.isNetworkClient()) {
                User user = timeInterval.getUser();
                if (user != null) {
                    userString = "<br><font color=\"#FF8040\">userString: " + user.getName() + "</font>";
                }
            }
            String description = "<html>" + showTreeTillRoot() + getSubject() + notes + userString + "<html>";
            return description;
        }

        private String showTreeTillRoot() {
            List<TreeElement> elements = new Vector<TreeElement>();
            TreeElement node = getTask();
            while (!node.isRoot()) {
                elements.add(0, node);
                node = node.getParent();
            }
//            elements.add(0, node);  //adding root node
            String retVal = "<table width=300>";
            int num = 0, tableSize = elements.size() + 3;
            for (TreeElement nod : elements) {
                retVal += "<tr>";
                if (num > 0) {
                    retVal += "<td colspan='" + num + "'>&nbsp;</td>";
                }
                retVal += "<td colspan='" + (tableSize - num) + "'>" +
                        "<font color = \"#0066FF\">" + nod.toString() + "</font></td>";
                retVal += "</tr>";
                num++;
            }
            retVal += "<tr>";
            for (int i = 0; i < tableSize; i++) {
                retVal += "<td>&nbsp;</td>";
            }
            retVal += "</tr></table>";
            return retVal;
        }

        public String getCompactSubject() {
            String subject = Utils.formatString(getSubject(), 15);
            return subject + " " + Utils.formatTime(duration);
        }

        public int getDuartion() {
            return duration;
        }

        public String getNotes() {
            return task.getNotes();
        }
    }

    public static void generate(Date start, Date finish) {
        getInstance().writeReport(start, finish);
    }

    public void writeReport(Date start, Date finish) {
        System.out.println("Generating report from " + start + " till " + finish);

        try {
            FileWriter fw = createFile(start, finish);
            fw.write(generateReport(start, finish));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String generateReport(Date start, Date finish) {
        StringBuffer report = new StringBuffer();
        reportDuration = 0;
        report.append("Report:");
        Calendar curent = GregorianCalendar.getInstance();
        curent.setTime(start);
        while (finish.after(curent.getTime())) {
            report.append(oneDayReport(curent));
            curent.add(Calendar.DAY_OF_MONTH, 1);
        }
        report.append("\ntotal: " + Utils.formatTime(reportDuration));
        return report.toString();
    }

    private String oneDayReport(Calendar curent) {
        StringBuffer day = new StringBuffer();
        day.append("\n  report for " + formater.format(curent.getTime()) + " :\n");
        int dayDuration = 0;
        for (TaskIntervalInterface taskInterval : getOneDayElements(curent)) {
            if (taskInterval.getDuartion() > 0) {
                dayDuration += taskInterval.getDuartion();
                day.append("    " + Utils.formatTime(taskInterval.getDuartion()));
                day.append(" - " + taskInterval.getSubject() + "\n");
                day.append(Utils.indentFormat("        ", taskInterval.getNotes()));
            }
        }
        day.append("    total this day: " + Utils.formatTime(dayDuration) + "\n");
        reportDuration += dayDuration;
        if (dayDuration > 0) {
            return day.toString();
        } else {
            return "";
        }
    }

    private FileWriter createFile(Date start, Date finish) throws IOException {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy_MM_dd");
        String fileName = "report_" + formater.format(start) + "__" +
                formater.format(finish) + ".txt";
        File file = new File(fileName);
        System.out.println("absolute path: " + file.getAbsolutePath());
        FileWriter fw = new FileWriter(file);
        return fw;
    }
}
