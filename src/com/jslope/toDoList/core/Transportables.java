package com.jslope.toDoList.core;

import com.jslope.briskproject.networking.PropagationList;
import com.jslope.toDoList.core.persistence.NetObject;

import java.util.Map;
import java.util.EnumMap;
import java.util.Collection;

/**
 * Date: 18.10.2005
 * Used to keep types IDs;
 */
public enum Transportables {
    USER, TASK, TIME_INTERVAL, PROP_LIST, MESSAGE, REFRESH_MARKER, TASK_SHARER;

    boolean toInitObjects = true;

    public int getID() {
        return ordinal() * 3 + 100;
    }


    /**
     * a list of objects which must be synchronized between server and clinet
     */
    public static Collection<NetObject> objectsToSynchronize() {
        return map.values();
    }
    // will not be a gap bettween inits so that after User init there might be created
    // NetworkConfig for examle, which will use next type ID destinated for Task
    public static Map<Transportables, NetObject> map = new EnumMap<Transportables, NetObject>(Transportables.class);
    static {
        map.put(USER, new User());
        map.put(TASK, new Task());
        map.put(TIME_INTERVAL, new TimeInterval());
//        map.put(PROP_LIST, new PropagationList()); it looks like it is not necessary to be in list
        new PropagationList();  // but we need it to have same ID on server and on client so we call it here;
        map.put(MESSAGE, new Message());
        map.put(REFRESH_MARKER, new RefreshMarker());
        map.put(TASK_SHARER, new TaskSharer());
    }

    public static NetObject get(Transportables key) {
        return map.get(key);
    }

    public NetObject object() {
        return get(this);
    }
}
