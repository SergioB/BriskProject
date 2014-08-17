package com.jslope.briskproject.networking;

import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.persistence.sql.SqlUtil;
import com.jslope.persistence.sql.ResultIterator;
import com.jslope.persistence.fields.IDField;
import com.jslope.persistence.utils.UtilMisc;

/**
 * Date: 24.01.2006
 */
public class ObjectsToSend {
    final static String tableName = "ObjectsToSend", ObjectID = "objectID", UserID = "userID";
    static {
        if (!NetworkConfig.toSendTableExists()) {
            SqlUtil.createTable(tableName);
            SqlUtil.addField(tableName, ObjectID, IDField.getIdSqlType());
            SqlUtil.addField(tableName, UserID, IDField.getIdSqlType());
            NetworkConfig.setToSendTable(true);
        }
    }

    private static final String[] fieldsToReques = {ObjectID};
    private ResultIterator result;
    private String userID;
    private ObjectsToSend(String userID) {
        this.userID = userID;
        result = SqlUtil.findByAnd(tableName, fieldsToReques, UtilMisc.toPair(UserID, userID));
    }

    public static ObjectsToSend forUser(User loggedUser) {
        return new ObjectsToSend(loggedUser.getID());
    }

    public boolean hasObjects() {
        return result.hasNext();
    }

    //todo: to add protection between deletion of object and adding it
    public NetObject next() {
        String objectID = result.next().getString(ObjectID);
        removeFromTodo(objectID);
        return (NetObject)NetObject.loadObject(objectID);
    }

    private void removeFromTodo(String objectID) {
        SqlUtil.delete(tableName, UtilMisc.toPair(UserID, userID, ObjectID, objectID));
    }

    public static boolean contains(String userID, String objectID) {
        ResultIterator result = SqlUtil.findByAnd(tableName, fieldsToReques, UtilMisc.toPair(UserID, userID, ObjectID, objectID));
        return result.hasNext();
    }

    public static void add(String userID, String objectID) {
        SqlUtil.insert(tableName, UtilMisc.toPair(UserID, userID, ObjectID, objectID));
    }
}
