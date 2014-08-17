package com.jslope.toDoList.core;

import static com.jslope.toDoList.core.Transportables.TASK_SHARER;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.toDoList.core.persistence.TWIDField;
import com.jslope.toDoList.core.persistence.TWBooleanField;
import com.jslope.toDoList.core.persistence.TWIntField;
import com.jslope.toDoList.core.interfaces.TreeElementContainer;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.ObjectList;

import java.util.*;

/**
 * This class is a link between a task and a user, it  allow a user access  a task in paralel tree
 * To change this template use File | Settings | File Templates.
 */
public class TaskSharer extends NetObject implements TreeElementContainer {
    private static final String TASK = "taskId", USER = "userID";
    private static final String IS_DELETED = "is_deleted";
    private static final String SHARED_INDEX = "shared_index";

    public void define() {
        super.define();
        this.addField(TASK, new TWIDField());
        this.addField(USER, new TWIDField());
        this.addField(SHARED_INDEX, new TWIntField(0));
        this.addField(IS_DELETED, new TWBooleanField(false));
    }

    public void virtualDelete() {
        getUser().addRefreshMarker();
        ((TWBooleanField) this.getField(IS_DELETED)).setValue(true);
        this.save();
    }

    public void undelete() {
        ((TWBooleanField) this.getField(IS_DELETED)).setValue(false);
        this.save();
    }

    public boolean isDeleted() {
        return ((TWBooleanField) this.getField(IS_DELETED)).getBooleanValue();
    }


    public User getParentUser() {
        return (User) loadObject(get(USER));
    }

    /**
     * adds a task sharer, and then
     *
     * @param task      task to share
     * @param shareWith user to share with
     * @return ture if sharer was added,  false if not.
     */
    public static boolean add(TreeElement task, User shareWith) {
        LogicCondition c = Condition.var(TASK).Equal(task.getID()).AND(USER).Equal(shareWith.getID());
        if (!Transportables.get(TASK_SHARER).objectExists(c)) {
            TaskSharer sharer = new TaskSharer();
            sharer.set(TASK, task.getID());
            sharer.set(USER, shareWith.getID());
            sharer.save();
            return true;
        } else {
            TaskSharer sharer = (TaskSharer) Transportables.get(TASK_SHARER).loadOneObject(c);
            if (sharer.isDeleted()) {
                sharer.undelete();
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * returns  a tree element representative of share linked to certain user
     * @param user
     * @return
     */

    public static Iterable<TreeElement> getTreeShares(User user) {
        Vector<TreeElement> retVals = new Vector<TreeElement>();
        for (TaskSharer share : getShares(user)) {
            retVals.add(share.getTreeElement());
        }
        return retVals;
    }

    public static Iterable<TaskSharer> getShares(User user) {
        Vector<TaskSharer> retVals = new Vector<TaskSharer>();
        LogicCondition c = Condition.var(USER).Equal(user.getID()).AND(IS_DELETED).Equal(false);
        ObjectList result = Transportables.get(TASK_SHARER).loadObjects(c);
        while (result.hasNext()) {
            TaskSharer share = (TaskSharer) result.next();
            retVals.add(share);
        }
        return retVals;
    }

    public TreeElement getTreeElement() {
        Task task = getTask();
        if (task.haveParentTask() && task.parentTaskIsHere()) {
            return SharedTask.getST(this);
        } else {
            if (task.isAssignedToUser() && task.assignedUserIsHere()) {
                return SharedTask.getST(this);
            } else  {
                task.setIsShared(this);
            }
            return task;
        }
    }

    Task getTask() {
        return (Task) loadObject(get(TASK));
    }

    public User getUser() {
        return (User) loadObject(get(USER));
    }

    public String getUserID() {
        return get(USER);
    }

    public static TaskSharer getShare(User user, Task task) {
        LogicCondition c = Condition.var(TASK).Equal(task.getID()).AND(USER).Equal(user.getID());
        TaskSharer sharer = (TaskSharer) Transportables.get(TASK_SHARER).loadOneObject(c);
        return sharer;
    }

    public void addAllSubtasks(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        if (!this.isDeleted()) {
            getTask().addObjectsToSend(objectsToSend, afterDate, excludeList);
        }
    }
    public Iterable<NetObject> getAllSubtasks() {
        Vector<NetObject> tasks = new Vector<NetObject>();
        addAllSubtasks(tasks, 0, new HashSet<String>());
        return tasks;
    }

    public int getOrder() {
        return Transportables.TASK_SHARER.getID();
    }

    public int getIndex() {
        return ((TWIntField) this.getField(SHARED_INDEX)).getIntValue();
    }

    public void setIndex(int newIndex) {
        ((TWIntField) this.getField(SHARED_INDEX)).setIntValue(newIndex);
    }

    public static boolean hasShare(Task task) {
        LogicCondition c = Condition.var(TASK).Equal(task.getID()).AND(IS_DELETED).Equal(false);
        ObjectList result = Transportables.get(TASK_SHARER).loadObjects(c);
        return result.hasNext();
    }

    public static Collection<String> getSharedUserIDs(Task task) {
        HashSet<String> retVals = new HashSet<String>();
        LogicCondition c = Condition.var(TASK).Equal(task.getID()).AND(IS_DELETED).Equal(false);
        ObjectList result = Transportables.get(TASK_SHARER).loadObjects(c);
        while (result.hasNext()) {
            TaskSharer share = (TaskSharer) result.next();
            retVals.addAll(share.getUserIDsUntil());
        }
        return retVals;
    }

    /**
     * it returns all users between shared task parent user and user with whom task was shared
     * @return
     */
    public List<String> getUserIDsUntil() {
        List<String> usersIDs =  new Vector<String>();
        User user = getUser();
        String userID = getTask().getParentUser().getID();
        while(!user.isRoot())  {
            String currentUserID = user.getID();
            if (currentUserID.equals(userID)) break;
            usersIDs.add(currentUserID);
            user = (User)user.getParent();
        }
        return usersIDs;
    }
    public void afterReceive() {
        getTask().reset();
    }

}
