package com.jslope.briskproject.server;

import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.fields.IDField;
import com.jslope.toDoList.core.User;

/**
 * Date: 12.10.2005
 */
public class ServerConfig extends DBPersistentObject {
    private static final String ADMIN_ID = "AdminID";

    public void define() {
        this.addField(ADMIN_ID, new IDField());
    }

    private static ServerConfig ourInstance;
    static {
        ourInstance = new ServerConfig();
        ObjectList result = ourInstance.loadObjects(null);
        if (result.hasNext()) {
            ourInstance = (ServerConfig) result.next();
        } else {
            ourInstance.save();
        }

    }
    private static ServerConfig getInstance() {
        return ourInstance;
    }

    public static void setAdmin(User admin) {
        admin.save();
        getInstance().set(ADMIN_ID, admin.getID());
        getInstance().save();
    }

    public static User getAdmin() {
        return (User)loadObject(getInstance().get(ADMIN_ID));
    }

    public static boolean isAdmin(User loggedUser) {
        return loggedUser.getID().equals(getInstance().get(ADMIN_ID));
    }
}
