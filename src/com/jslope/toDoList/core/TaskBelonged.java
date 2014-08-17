package com.jslope.toDoList.core;

import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.persistence.fields.IDField;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.LoadException;
import com.jslope.utils.Log;
import com.jslope.briskproject.networking.ClientProtocol;
import com.jslope.briskproject.networking.NetworkConfig;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Collection;

/**
 * Should be extended by classes which objects belong to a class, for example TimeInterval and Message (had one task as parent)
 */

public abstract class TaskBelonged extends NetObject {
    public final static String PARENT_TASK = "parent_task";

    public void define() {
        super.define();
        this.addField(PARENT_TASK, new IDField());
    }

    public void setParent(String id) {
        set(PARENT_TASK, id);
    }

    public Task getParent() {
        String taskID = get(PARENT_TASK);
        try {
            return (Task) loadObject(taskID);
        } catch (LoadException e) {
            Log.error("Unable to load parent for " + this + " with id=" + getID(), e);
            if (!Options.isOnServer()) {
                Log.debug("trying to request object from server...");
                ClientProtocol.requestObjectFromServer(taskID);
                return (Task) loadObject(taskID);
            }
            throw new LoadException(e);
        }
    }

    public User getParentUser() {
        return getParent().getParentUser();
    }

    public void addObjectsToSend(Collection<NetObject> objectsToSend, LogicCondition taskCondition, Set excludeList) {
        ObjectList result;
        result = this.loadObjects(taskCondition);
        while (result.hasNext()) {
            TaskBelonged taskBelonged = (TaskBelonged) result.next();
            if (!excludeList.contains(taskBelonged.getID())) {
                objectsToSend.add(taskBelonged);
            }
        }
    }

    public static TaskBelonged[] types = null;

    /**
     * this method is needed because static initialization here is done eariler than
     * static init in Transportables
     *
     * @return
     */
    public static TaskBelonged[] getTypes() {
        if (types == null) {
            types = new TaskBelonged[2];
            types[0] = (TaskBelonged) Transportables.get(Transportables.MESSAGE);
            types[1] = (TaskBelonged) Transportables.get(Transportables.TIME_INTERVAL);
        }
        return types;
    }


    public static Iterable<TaskBelonged> getAllChildren(Task task) {
        Vector<TaskBelonged> retValue = new Vector<TaskBelonged>();
        for (TaskBelonged type : getTypes()) {
            List<TaskBelonged> list = type.getChildren(task);
            retValue.addAll(list);
        }
        return retValue;
    }

    public List<TaskBelonged> getChildren(Task task) {
        ObjectList result = this.loadObjects(Condition.var(Message.PARENT_TASK).Equal(task.getID()));
        Vector<TaskBelonged> retValue = new Vector<TaskBelonged>();
        while (result.hasNext()) {
            retValue.add((TaskBelonged) result.next());
        }
        return retValue;
    }

}
