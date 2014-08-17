package com.jslope.briskproject.networking;

import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.toDoList.core.persistence.TWBooleanField;
import com.jslope.toDoList.core.User;
import com.jslope.persistence.ObjectList;

/**
 * Date: 07.11.2005
 */
public class ProjectOptions extends NetObject {
    private static ProjectOptions ourInstance = null;

    static {
        ourInstance = new ProjectOptions();
        ObjectList result = ourInstance.loadObjects(null);
        if (result.hasNext()) {
            ourInstance = (ProjectOptions) result.next();
        }
    }


    public void define() {
        super.define();
        this.addField("TEMP_FIELD", new TWBooleanField(true));  // should be deleted when real fields will be added
    }

    public User getParentUser() {
        return null;
    }

    public static ProjectOptions getInstance() {
        return ourInstance;
    }
}
