package com.jslope.toDoList.core;

import static com.jslope.toDoList.core.Transportables.REFRESH_MARKER;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.toDoList.core.persistence.TWIDField;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.sql.Condition;

/**
 * Date: 16.01.2006
 * This is a mareker for User object which if is set it means that user must do a full refresh
 * this usually is done when some of users subobjects are move out so the user can not see them.
 */
public class RefreshMarker extends NetObject {
    final static public String USER = "user";
    public void define() {
        super.define();
        this.addField(USER, new TWIDField());
    }

    protected int getOrder() {
        return REFRESH_MARKER.getID();
    }

    public static RefreshMarker getMarkerFor(User user) {
        RefreshMarker rm = (RefreshMarker)REFRESH_MARKER.object();
        ObjectList result = rm.loadObjects(Condition.var(USER).Equal(user.getID()));
        if (result.hasNext()) {
            rm = (RefreshMarker)result.next();
        } else {
            rm = new RefreshMarker();
            rm.set(USER, user.getID());
        }
        return rm;
    }

    public static boolean hasMarker(User user) {
        RefreshMarker rm = (RefreshMarker)REFRESH_MARKER.object();
        ObjectList result = rm.loadObjects(Condition.var(USER).Equal(user.getID()));
        if ( result.hasNext()) {
            rm = (RefreshMarker)result.next();
            rm.deleteObject();
            return true;
        } else {
            return false;
        }
    }

    public User getParentUser() {
        return null;
    }
}
