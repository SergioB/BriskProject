package com.jslope.toDoList.core;

import static com.jslope.utils.Constants.MILLIS_IN_ONE_DAY;
import static com.jslope.toDoList.core.Transportables.USER;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.ObjectList;
import com.jslope.UI.*;
import com.jslope.briskproject.networking.LoginData;
import com.jslope.toDoList.core.persistence.*;

import javax.swing.*;
import java.util.*;
import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;

/**
 * Date: 20.07.2005
 */
public class User extends TreeElement {
    public static int MAX_LOGIN_LEN = 30, MAX_PASSWORD_LEN = 30, MAX_NAME_LENGHT = 100;
    List<TreeElement> subUsers, sharedTasks, sharedTasksDone;
    private Task internalTask = null;

    public static final String NAME = "Name";
    public static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String INTERNAL_TASK = "internalTask";
    private static final String IS_ADMIN = "isAdmin";
    private static final String ACCESS_LEVEL = "accessLevel";


    public void define() {
        super.define();
        this.addField(NAME, new TWVarcharField(MAX_NAME_LENGHT));
        this.addField(LOGIN, new TWVarcharField(MAX_LOGIN_LEN));
        this.addField(PASSWORD, new TWVarcharField(MAX_PASSWORD_LEN));
        this.addField(INTERNAL_TASK, new TWIDField());
        this.addField(IS_ADMIN, new TWBooleanField(false));
        this.addField(ACCESS_LEVEL, new TWIDField());
        this.set(NAME, "No Name");
    }

    public void setIsAdmin(boolean value) {
        ((TWBooleanField) getField(IS_ADMIN)).setValue(value);
    }

    public boolean isAdmin() {
        return ((TWBooleanField) getField(IS_ADMIN)).getBooleanValue();
    }

    public Task getInternalTask() {
        if (internalTask == null) {
            System.out.println("generating internal task for " + this.toString());
            internalTask = new Task();
            if (get(INTERNAL_TASK).equals(TWIDField.null_id)) {
                set(INTERNAL_TASK, internalTask.getID());
                internalTask.set(Task.IS_INTERNAL_TASK, "1");
                updateInternalTaskParent();
            } else {
                if (internalTask.objectExists(get(INTERNAL_TASK))) {
                    internalTask = (Task) loadObject(get(INTERNAL_TASK));
                } else {
                    System.out.println("Internal task is not in database!");
                    internalTask = new Task();  // it means that object was received but internal task wasn't
                }
            }
        }
        return internalTask;
    }

    private void updateInternalTaskParent() {
        if (parent != null) {
            getInternalTask().set(PARENT, ((User) parent).getInternalTask().getID());
        }
    }

    protected void initChildren() {
        super.initChildren();
        subUsers = childSet.getVector(ChildrenTypes.USERS);
        sharedTasks = childSet.getVector(ChildrenTypes.SHARED_TASKS);
        sharedTasksDone = childSet.getVector(ChildrenTypes.SHARED_TASKS_DONE);
    }


    protected TreeElementChildSet getChildSet() {
        return new TreeElementChildSet(this, EnumSet.of(ChildrenTypes.USERS, ChildrenTypes.SHARED_TASKS, ChildrenTypes.TASKS, ChildrenTypes.SHARED_TASKS_DONE, ChildrenTypes.TASKS_DONE));
    }


    public String toString() {
        return this.get(NAME);
    }

    public void updateEditor() {
        EditorsPanel.select(Editors.USER_PANEL);
        UserEditor.getInstance().update(this);
    }

    public void updateDataFromEditor() {
        UserEditor.getInstance().updateData(this);
    }

    protected boolean canChildMoveLeft() {
        return false; // for current version tasks can not move out of user
    }

    public Icon getIcon() {
        return Buttons.user;
    }

    public void setLogin(LoginData logData) {
        this.set(LOGIN, logData.getLogin());
        this.set(PASSWORD, logData.getPassword());
    }

    /**
     * returns true if such user with such login data exists
     *
     * @param login
     * @return true if login data exists
     */
    public static boolean verifyLogin(LoginData login) {
        User user = new User();
        return user.objectExists(Condition.var(LOGIN).Equal(login.getLogin()).AND(PASSWORD).Equal(login.getPassword()));
    }

    public static User loadUser(LoginData login) {
        User user = new User();
        user = (User) user.loadOneObject(Condition.var(LOGIN).Equal(login.getLogin()).AND(PASSWORD).Equal(login.getPassword()));
        return user;
    }

    public int getOrder() {
        return USER.getID();
    }

    public LoginData getLogin() {
        return new LoginData(get(LOGIN), get(PASSWORD).toCharArray());
    }

    public static boolean loginExists(LoginData login) {
        User user = new User(); //todo: to get instance of user object from Transportable
        return user.objectExists(Condition.var(LOGIN).Equal(login.getLogin()));
    }

    public boolean canAddUser() {
        return true;
    }

    public void showDebugInfo() {
        System.out.println("This is user object" +
                "\nuser name:" + get(NAME) + "\nlogin: " + get(LOGIN) +
                "\npassw: " + get(PASSWORD) + "\nID: " + getID() +
                "\n modified time: " + getModifiedTime());
        System.out.println(" debug info of internal task:");
        getInternalTask().showDebugInfo();
//        showChildren();
    }

    protected ChildrenTypes getType() {
        return ChildrenTypes.USERS;
    }

    protected String getChildrenSorter() {
        return Task.ASSIGNED_INDEX;
    }


    public void loadChildren() {
        loadChildren(Condition.var(Task.ASSIGNED_TO).Equal(this.getID()));
        User user = new User();
        ObjectList result;
        result = user.loadObjects(Condition.var(TreeElement.PARENT).Equal(this.getID()).AND(IS_DELETED).Equal(false),
                new String[]{TreeElement.SORT_INDEX});
        boolean toReindex = false;
        while (result.hasNext()) {
            user = (User) result.next();
            subUsers.add(user);
            user.setParent(this);
            user.loadChildren();
            toReindex |= shouldReindex(user); // if (user.shouldReindex()) toReindex = true;
        }
        if (toReindex) {
            reindexUsers();
        }

        if (Options.isShowingTasks()) {
            for (TreeElement share : TaskSharer.getTreeShares(this)) {
                childSet.add(share);
                share.loadChildren();
            }
        }

    }

    private void reindexUsers() {
        childSet.reindex(ChildrenTypes.USERS);
    }

    public void setParent(TreeElement newParent) {
        super.setParent(newParent);
        updateInternalTaskParent();
    }

    protected void beforeSave() {
        getInternalTask().setSubject("Personal tasks of " + get(NAME));
        getInternalTask().save();  // if task is not generated then it authomatically will generate it
    }

    public void afterReceive() {
    }

    protected void afterLoad() {
        loadInternalTask();
        updateWorkedTime();
        if (Options.isOnServer()) {
            initParent();
        }
    }

    private void loadInternalTask() {
        internalTask = (Task) ((TWIDField) (getField(INTERNAL_TASK))).loadObject();
        if (internalTask == null) {
            getInternalTask();
        }
    }


    public void deleteObject() {
        unassignTasks();
        System.out.println("\n\n\nnow will delete internal task with ID: " + getInternalTask().getID());
        getInternalTask().deleteObject();
        super.deleteObject();
    }

    public void virtualDelete() {
        unassignTasks();
        getInternalTask().virtualDelete();
        super.virtualDelete();
    }


    private void unassignTasks() {
        unassignTasks(children);
        unassignTasks(doneChildren);
    }

    private void unassignTasks(List<TreeElement> list) {
        for (TreeElement task : list) {
            if (!getInternalTask().hasAsGrandChild(task)) {
                ((Task) task).unassing();
            }
        }
    }

    protected void remove(TreeElement element) {
        super.remove(element);
        if (element instanceof Task) {
            ((Task) element).unassing();
        }
    }


    /**
     * returns up to which user this user can access, for now it can't access higher than itself.
     *
     * @return itself
     */
    public TreeElement getAccessibleRoot() {
        return this;
    }

    /**
     * for user we need to add task directly assigned to user
     *
     * @param objectsToSend
     * @param afterDate
     */
    public void addObjectsToSend(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        LogicCondition taskCondition = Condition.var(Task.ASSIGNED_TO).Equal(this.getID())
                .AND(CHILDREN_MODIFIED_TIME).MoreThan(afterDate);
        addObjectsToSend(taskCondition, objectsToSend, afterDate, excludeList);
        addSubusersToSend(objectsToSend, afterDate, excludeList);
        addSharesToSend(objectsToSend, afterDate, excludeList);
        super.addObjectsToSend(objectsToSend, afterDate, excludeList);
    }

    /**
     * it receive all tasks sharer object of certain user
     *
     * @param objectsToSend
     * @param excludeList
     */
    private void addSharesToSend(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        for (TaskSharer taskSharer : TaskSharer.getShares(this)) {
            taskSharer.addAllSubtasks(objectsToSend, afterDate, excludeList);
            if (!excludeList.contains(taskSharer.getID())) {
                objectsToSend.add(taskSharer);
            }
        }
    }

    private void addSubusersToSend(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        ObjectList result;
        result = this.loadObjects(Condition.var(PARENT).Equal(this.getID()).AND(CHILDREN_MODIFIED_TIME).MoreThan(afterDate));
        User user;
        while (result.hasNext()) {
            user = (User) result.next();
            user.addObjectsToSend(objectsToSend, afterDate, excludeList);
        }

    }

    public void send(DataOutput out) throws IOException {
        super.send(out);
        Task task = getInternalTask();
        task.send(out);
    }

    public void receive(DataInput in) throws IOException {
        super.receive(in);
        Task task = getInternalTask();
        task.receive(in);
    }

    /**
     * todo: this can be optimizing by only seting a flag by public method and when getWorkedTiem is invoked to verify that flag and compute worked time if required
     */
    public void updateWorkedTime() {
        TimeInterval ti = new TimeInterval();
        long sinceWhen = System.currentTimeMillis() - Options.getInstance().getUserShowTime() * MILLIS_IN_ONE_DAY;
        workedTime = 0;
        ObjectList result = ti.loadObjects(Condition.var(TimeInterval.USER).Equal(getID())
                .AND(TimeInterval.TIME_START).MoreThan(sinceWhen).AND(TimeInterval.IS_DELETED).Equal(false));
        while (result.hasNext()) {
            ti = (TimeInterval) result.next();
            workedTime += ti.getSeconds();
        }
    }

    int workedTime = 0;     // here is cached value of worked time so we'll not call each time functionality of updateWorkedTime

    public int getWorkedTime() {
        return workedTime;
    }

    public void updateWorkedTimeRecursivelly() {
        this.updateWorkedTime();
        for (TreeElement user : subUsers) {
            ((User) user).updateWorkedTimeRecursivelly();
        }
    }

    static User emptyUser = new User();

    /**
     * this is done to make quicker searches without creating new objects (because table name is known and we don't need new object to test it)
     *
     * @param ownerID
     * @return true if user exists
     */
    public static boolean userExists(String ownerID) {
        return emptyUser.objectExists(ownerID);
    }

    public void initParent() {
        if (objectExists(get(PARENT))) {
            parent = (TreeElement) ((TWIDField) getField(PARENT)).loadObject();
        } else {
            parent = null;
        }
    }

    public String getName() {
        return get(NAME);
    }

    public void add(TreeElement element) {
        super.add(element);
        if (element instanceof Task) {
            ((Task) element).addTimeToParent();
        }
    }

    public void addRefreshMarker() {
        System.out.println(" adding refresh marker for: " + this);
        RefreshMarker rm = RefreshMarker.getMarkerFor(this);
        rm.updateTime();
        rm.save();
    }

    //todo: when will implement user levels to upgrade this
    public Iterable<User> getUsersAtTheSameLevel() {
        Set<User> users = new HashSet<User>();
        users.add(this);
        return users;
    }

    public User getParentUser() {
        if (this.isRoot()) {
            return (User) this;
        } else {
            return (User) getParent();
        }
    }

    public void setName(String newName) {
        set(NAME, newName);
    }

    public Iterable<User> getAccessibleParents() {
        Vector<User> users = new Vector<User>();
        users.add(this);
        User current = this;
        while (!current.isRoot()) {
            current = (User) current.getParent();
            users.insertElementAt(current, 0);
        }
        return users;
    }

    public User getAccessLevel() {
        if (((TWIDField) getField(ACCESS_LEVEL)).isNull()) {
            return this;
        } else {
            return (User) ((TWIDField) getField(ACCESS_LEVEL)).loadObject();
        }
    }

    public void setAccessLevel(User user) {
        set(ACCESS_LEVEL, user.getID());
    }

    public void addSharedTask(Task task) {
        this.add(SharedTask.getST(this, task));
        TreePanel.reload(this);
    }
}
