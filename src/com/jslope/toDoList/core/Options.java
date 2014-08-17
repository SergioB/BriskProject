/**
 * Date: 21.06.2005
 */
package com.jslope.toDoList.core;

import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.fields.BooleanField;
import com.jslope.persistence.fields.IntField;
import com.jslope.persistence.fields.IDField;
import com.jslope.persistence.fields.DoubleField;
import com.jslope.UI.TreePanel;
import com.jslope.toDoList.core.interfaces.TreeElementContainer;

import java.awt.*;

public class Options extends DBPersistentObject {
    private static Options ourInstance = null;
    public static final String AUTOSTART_TASK = "autostartTask",
    AUTOSTOP_TASK = "autostopTask", AUTOSTOP_INTERVAL = "autostopInterval"
    , ROOT_NODE_ID = "rootNode", SPLIT_PANE = "splitPane"
    , WINDOW_X = "windowX", WINDOW_Y = "windowY", WINDOW_WIDTH = "windowWidth", WINDOW_HEIGHT = "windowHeight"
    , SELECTED_NODE = "selectedNode", HIDE_TASKS_DONE = "hideTasksDone", TASK_DONE_FILTER = "taskDoneFilter"
    ,USER_SHOW_TIME = "userShowTime", NEW_TO_READ_TIME="newToReadTime";

    public static final String USER_ID = ROOT_NODE_ID;
    /**
     * this is cached value of persistent field, it is used
     * to make faster access to it's value, because it is checked everyt half of second
     */
    boolean autostopTask, hideTasksDone;
    /**
     * this variable is true if current application is Network Client wich connects to a server
     * otherwise (if it's stand alone application) it is false
     */
    static boolean isNetworkClient = false;

    public static void setNetworkClient(boolean is) {
        isNetworkClient = is;
    }

    public static boolean isNetworkClient() {
        return isNetworkClient;
    }

    int autostopInterval;

    public void define() {
        this.addField(AUTOSTART_TASK, new BooleanField(false));
        this.addField(AUTOSTOP_TASK, new BooleanField(true));
        this.addField(AUTOSTOP_INTERVAL, new IntField());
        this.addField(ROOT_NODE_ID, new IDField());

        this.addField(WINDOW_X, new DoubleField(0.0));
        this.addField(WINDOW_Y, new DoubleField(0.0));
        this.addField(WINDOW_WIDTH, new DoubleField(520.0));
        this.addField(WINDOW_HEIGHT, new DoubleField(220.0));

        this.addField(SPLIT_PANE, new IntField(188));

        this.addField(SELECTED_NODE, new IDField());
        this.addField(HIDE_TASKS_DONE, new BooleanField(false));
        this.addField(TASK_DONE_FILTER, new IntField(99999)); // maximum oldness of task done in days which will be showed in tree

        this.addField(USER_SHOW_TIME, new IntField(7));       //show user worked time in past N days
        this.addField(NEW_TO_READ_TIME, new IntField(1));       //show user worked time in past N days

        this.setAutostopTaskValues(true);
        this.setHideTasksDone(false);
        this.setAutostopIntervalValues(5); // setting default value
    }

    public void setHideTasksDone(boolean newValue) {
        ((BooleanField) getField(HIDE_TASKS_DONE)).setValue(newValue);
        hideTasksDone = newValue;
    }

    public boolean getHideTaskDone() {
        return hideTasksDone;
    }


    public static Options getInstance() {
        if (ourInstance == null) {
            Options tempInstance = new Options();
            ObjectList result = tempInstance.loadObjects();
            if (result.hasNext()) {
                ourInstance = (Options) result.next();
            } else {
                ourInstance = tempInstance;
                if (!isOnServer()) {    // generating root Node only if we're not on server
                    ourInstance.generateRootUser();
                }
                ourInstance.save();
            }

        }
        return ourInstance;
    }


    public boolean getAutostartTask() {
        return ((BooleanField) getField(AUTOSTART_TASK)).getBooleanValue();
    }

    public void setAutostartTask(boolean value) {
        ((BooleanField) getField(AUTOSTART_TASK)).setValue(value);
        this.save();
    }

    public void setAutostopTaskValues(boolean value) {
        ((BooleanField) getField(AUTOSTOP_TASK)).setValue(value);
        autostopTask = value;
    }

    public void setAutostopTask(boolean value) {
        setAutostopTaskValues(value);
        this.save();
    }

    public boolean getAutostopTask() {
        return autostopTask;
    }

    public void setAutostopIntervalValues(int interval) {
        ((IntField) getField(AUTOSTOP_INTERVAL)).setIntValue(interval);
        autostopInterval = interval;
    }

    public void setAutostopInterval(int interval) {
        setAutostopIntervalValues(interval);
        save();
    }

    public int getAutostopInterval() {
        return autostopInterval;
    }

    protected void afterLoad() {
        autostopInterval = ((IntField) getField(AUTOSTOP_INTERVAL)).getIntValue();
        autostopTask = ((BooleanField) getField(AUTOSTOP_TASK)).getBooleanValue();
        hideTasksDone = ((BooleanField) getField(HIDE_TASKS_DONE)).getBooleanValue();
    }

    public TreeElement getRootNode() {
        TreeElement node = (User) loadObject(this.get(ROOT_NODE_ID));
        return node;
    }

    public User getUser() {
        User user = (User) DBPersistentObject.loadObject(this.get(USER_ID));
        return user;
    }

    private void generateRootUser() {
        User user = new User();
        user.set(User.NAME, "Root Node");
        this.set(ROOT_NODE_ID, user.getID());
        user.save();
        Task task = new Task();
        task.setNotes("Edit notes here");
        task.set(Task.SORT_INDEX, "0");
        user.add(task);
        task.setParent(user);
        task.save();
    }

    public Point getWindowLocation() {
        return new Point((int) ((DoubleField) getField(WINDOW_X)).getDoubleValue(),
                (int) ((DoubleField) getField(WINDOW_Y)).getDoubleValue());
    }

    public Dimension getWindowSize() {
        return new Dimension((int) ((DoubleField) getField(WINDOW_WIDTH)).getDoubleValue(),
                (int) ((DoubleField) getField(WINDOW_HEIGHT)).getDoubleValue());
    }

    public void setWindowLocation(Point location) {
        set(WINDOW_X, "" + location.getX());
        set(WINDOW_Y, "" + location.getY());
    }

    public void setWindowSize(Dimension size) {
        set(WINDOW_WIDTH, "" + size.getWidth());
        set(WINDOW_HEIGHT, "" + size.getHeight());
    }

    public int getSplitPane() {
        return ((IntField) getField(SPLIT_PANE)).getIntValue();
    }

    public void setSplitPane(int newValue) {
        ((IntField) getField(SPLIT_PANE)).setIntValue(newValue);
    }

    public void setSelectedNode(TreeElement node) {
        if (node == null) {
            set(SELECTED_NODE, IDField.null_id);
        } else {
            set(SELECTED_NODE, node.getID());
        }
    }

    public TreeElement getSelectedNode() {
        String nodeID = get(SELECTED_NODE);
        if (nodeID.equals(IDField.null_id)) {
            return null;
        } else {
            return ((TreeElementContainer) DBPersistentObject.loadObject(nodeID)).getTreeElement();
        }
    }

    public int getTaskDoneFilter() {
        return ((IntField) getField(TASK_DONE_FILTER)).getIntValue();
    }

    public void setTaskDoneFilter(int value) {
        ((IntField) getField(TASK_DONE_FILTER)).setIntValue(value);
    }

    boolean showTasks = true, showUsers = true;

    public static void setShowTasks(boolean newValue) {
        getInstance().showTasks = newValue;
    }

    public static boolean isShowingTasks() {
        return getInstance().showTasks;
    }

    public static void setShowUsers(boolean newValue) {
        getInstance().showUsers = newValue;
    }

    public static boolean isShowingUsers() {
        return getInstance().showUsers;
    }

    public static boolean isDebugMode() {
        return true;
    }

    public static void setRootNode(String rootNodeID) {
        System.out.println("\n\n\n\n\nsetting rootNodeID to " + rootNodeID+"\n\n\n\n\n");
        getInstance().set(ROOT_NODE_ID, rootNodeID);
        getInstance().save();
    }

    public static boolean rootIsAdmin() {
        User root = (User) getInstance().getRootNode();
        return root.isAdmin();
    }

    /**
     * it can save a different node than saveRootNode from TreeModel, because there rootNode might be a Task 
     */
    public static void saveRootNode() {
        TreePanel.saveCurrentNode();
        TreeElement rootNode = getInstance().getRootNode();
        rootNode.save();
        rootNode.saveChildren();
    }

    static boolean onServer = true;
    public static boolean isOnServer() {
        return onServer;
    }
    public static void setOnServer(boolean newValue) {
        onServer = newValue;
    }

    /**
     * Current logged user ID
     * @return current user ID
     */
    public static String getUserID() {
        return getInstance().get(USER_ID);
    }

    public void setUserShowTime(int value) {
        ((IntField) getField(USER_SHOW_TIME)).setIntValue(value);
    }

    public int getUserShowTime() {
        return ((IntField) getField(USER_SHOW_TIME)).getIntValue();
    }

    public static int getNewToReadTime() {
        return ((IntField) getInstance().getField(NEW_TO_READ_TIME)).getIntValue();
    }

    public static User getLoggedUser() {
        return (User)getInstance().getRootNode();
    }

    public static void setNewToReadTime(int value) {
        ((IntField) getInstance().getField(NEW_TO_READ_TIME)).setIntValue(value);
    }
}
