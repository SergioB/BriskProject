package com.jslope.toDoList.core;

import com.jslope.persistence.PersistenceException;
import com.jslope.persistence.LoadException;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.sql.QueryException;
import com.jslope.persistence.fields.*;
import com.jslope.UI.MainWindow;
import com.jslope.toDoList.core.persistence.TWBooleanField;
import com.jslope.toDoList.core.persistence.TWTimestampField;
import com.jslope.briskproject.networking.NetworkConfig;

import javax.swing.*;
import java.util.Date;
import java.util.Calendar;

import sun.net.NetworkClient;

/**
 * Date: 17.05.2005
 */
public class TimeInterval extends TaskBelonged {
    public final static String DATE = "date", TIME_START = "time_start", TIME_END = "time_end", IS_DELETED = "is_deleted", USER = "user";

    public void define() {
        super.define();
        this.addField(DATE, new DateField());
        this.addField(TIME_START, new TWTimestampField());
        this.addField(TIME_END, new TWTimestampField(0));   // default value is 0
        this.addField(IS_DELETED, new TWBooleanField(false));
        this.addField(USER, new IDField());     //user who did the interval
        this.setCurrentTime(TIME_START);
        this.set(USER, Options.getUserID());
        updateTime();
    }

    public void setCurrentTime(String fieldName) {
        ((LongField) this.getField(fieldName)).setLongValue(System.currentTimeMillis());
    }

    public void save() throws PersistenceException {
        super.save();
    }

    public void finishAndSave() {
        setCurrentTime(TIME_END);
        this.save();
        updateUser();
    }

    public void justSave() {        
        this.save();
        updateUser();
    }

    public void afterReceive() {
        updateUser();
    }

    private void updateUser() {
        if (!Options.isOnServer()) {
            User user = getUser();
            if (user != null) {
                user.updateWorkedTime();
            }
        }
    }

    public User getUser() {
        String userID = get(USER);
        User user;
        if (User.userExists(userID)) {
            user = (User) loadObject(userID);
        } else {
            user = null;
        }
        return user;
    }


    public long getTimeStart() {
        return ((LongField) this.getField(TIME_START)).getLongValue();
    }

    public long getTimeEnd() {
        return ((LongField) this.getField(TIME_END)).getLongValue();
    }

    public int getSeconds() {
        long timeStart, timeEnd;
        timeStart = ((LongField) this.getField(TIME_START)).getLongValue();
        timeEnd = ((LongField) this.getField(TIME_END)).getLongValue();
        if (timeEnd == 0) { // in case it was not saved and TIME_END is not set
            timeEnd = new Date().getTime();
        }
        return (int) ((timeEnd - timeStart) / 1000);
    }

    /**
     * A function used for debuggin
     * @return gets start and end time
     */
    public String getTimes() {
        return "start: " + getTimeStart() +" end: " + getTimeEnd();
    }

    public void setParent(Task task) {
        this.set(PARENT_TASK, task.getID());
    }

    public Task getParrentTask() {
        try {
            return (Task) loadObject(this.get(PARENT_TASK));
        } catch (LoadException e) {
            Object[] options = {"Delete info",
                    "Create empty task"
            };
            int n = JOptionPane.showOptionDialog(MainWindow.getInstance(),
                    "It seems that there was a problem with database,\n" +
                            "possible caused by not closing application correctly\n" +
                            "there are empty data about task without task itself\n " +
                            "What should we do about it?",
                    "Database corruption found",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[1]);
            System.out.println("Was choosen n=" + n);
            if (n == 0) {
                ObjectList result = this.loadObjects(Condition.var(PARENT_TASK).Equal(this.get(PARENT_TASK)));
                while (result.hasNext()) {
                    result.next().deleteObject();
                }
            } else {
                Task task = new Task();
                task.setSubject("Empty task");
                task.setNotes("created automatically");
                task.set(Task.IS_ACTIVE, "false");
                String id = this.get(PARENT_TASK);
                task.setID(id);
                System.out.println("id to be set=" + id + " id got" + task.getID());
                task.save();
                return task;
            }
        }
        return null;
    }

    public boolean sameTask(TimeInterval interval) {
        if (Options.isNetworkClient()) {    //if this is a network client then we need to compare users, so we'll show in report different intervals with different users
            User thisUser = getUser();
            User thatUser = interval.getUser();
            if (thisUser != thatUser) {
                if (thisUser != null && thatUser != null) {
                    if (!thisUser.getID().equals(thatUser.getID())) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return getParrentTask().getID().equals(interval.getParrentTask().getID());

    }

    public String getSubject() {
        return getParrentTask().getSubject();
    }

    public String getNotes() {
        return getParrentTask().getNotes();
    }

    public void virtualDelete() {
        ((TWBooleanField) this.getField(IS_DELETED)).setValue(true);
        this.save();
        this.updateUser();
    }

    public int getOrder() {
        return Transportables.TIME_INTERVAL.getID();
    }

    public static ObjectList getTimeIntervalsForOneDay(Calendar calendar, User filterUser) {
        TimeInterval timeInterval = (TimeInterval) Transportables.get(Transportables.TIME_INTERVAL);
        try {
            LogicCondition condition = Condition.var(DATE).Equal(calendar.getTime()).
                    AND(IS_DELETED).Equal(false);
            if (filterUser != null) {
                condition = condition.AND(USER).Equal(filterUser.getID());
            }
            return timeInterval.loadObjects(condition);
        } catch (QueryException e) {
            System.out.println("Unable to load object list " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
