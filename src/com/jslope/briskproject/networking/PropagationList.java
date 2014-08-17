package com.jslope.briskproject.networking;

import com.jslope.toDoList.core.Transportables;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.fields.TextField;

import java.util.Map;
import java.util.HashMap;

/**
 * This object is used to keep a list of object IDs which need further porpagation on server
 * This is needed for the case when subtask is assigned to a User and when task is modified
 * subtask tree also must be marked to be updated on other Users
 * Date: 09.11.2005
 */
public class PropagationList extends NetObject {
    private static PropagationList ourInstance;
    private static final String CHILDREN_TIME = "childrenTime",
    SEPARATOR = ";", SECOND_SEPARATOR = ":";
    private static boolean isOnClient = !Options.isOnServer();

    //todo: to make a thread which will save default propagation list each 20 seconds
    static {
        ourInstance = new PropagationList();
        ObjectList result = ourInstance.loadObjects();
        if (result.hasNext()) {
            ourInstance = (PropagationList) result.next();
        } else {
            ourInstance.save();
        }
    }

    public PropagationList() {
        super();
        init();
    }

    public void define() {
        super.define();
        this.addField(CHILDREN_TIME, new TextField(""));
    }

    public User getParentUser() {
        return null;
    }

    private void init() {
        set(CHILDREN_TIME, "");
        initChildrenTime();
    }

    public static PropagationList getInstance() {
        return ourInstance;
    }

    public int getOrder() {
        return Transportables.PROP_LIST.getID();
    }

    public static void addTime(String id, int seconds) {
        System.out.println("In PropagationList adding time :"+seconds);
        if (isOnClient) {
            getInstance().addChildrenTime(id, seconds);
        }
    }

    protected void afterLoad() {
        childrenTime = stringToMap(get(CHILDREN_TIME));
    }

    public static Map<String, Integer> stringToMap(String sourceString) {
        Map<String, Integer> retVal = new HashMap<String, Integer>();
        if (!sourceString.equals("")) {
            for (String pair : sourceString.split(SEPARATOR)) {
                String processed[] = pair.split(SECOND_SEPARATOR);
                retVal.put(processed[0], Integer.decode(processed[1]));
            }
        }
        return retVal;
    }

    public static String mapToString(Map<String, Integer> array) {
        String retVal = "";
        boolean addSeparator = false;
        for (String id : array.keySet()) {
            if (addSeparator) {
                retVal += SEPARATOR;
            } else {
                addSeparator = true;
            }
            retVal += id + SECOND_SEPARATOR + array.get(id);
        }
        return retVal;
    }

    protected void beforeSave() {
        set(CHILDREN_TIME, mapToString(childrenTime));
    }


    Map<String, Integer> childrenTime;

    private void initChildrenTime() {
        childrenTime = new HashMap<String, Integer>();
    }

    private void addChildrenTime(String id, int seconds) {
        int newValue = seconds;
        if (childrenTime.containsKey(id)) {
            newValue = childrenTime.get(id) + seconds;
        }
        childrenTime.put(id, newValue);
    }

    public void propagate() {
        propagateChildrenTime();
    }

    private void propagateChildrenTime() {
        System.out.println(" received childrenTime: " + childrenTime);
        for (String id : childrenTime.keySet()) {
            Task task = (Task) loadObject(id);
            task.addTimeToParent(childrenTime.get(id));
        }
    }

    public void clear() {
        init();
        save();
    }
}
