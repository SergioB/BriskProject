package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.LoadException;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.utils.Pair;
import com.jslope.persistence.sql.BasicCondition;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.fields.TimestampField;
import com.jslope.persistence.fields.Field;
import com.jslope.persistence.fields.BooleanField;
import com.jslope.briskproject.networking.Streamable;
import com.jslope.toDoList.core.User;

import java.io.DataOutput;
import java.io.IOException;
import java.io.DataInput;
import java.util.Set;

/**
 * Date: 19.10.2005
 * This object does know when it was last time changed
 * also it can be saved to DataStream
 */
public abstract class NetObject extends DBPersistentObject implements Streamable {
    public static final String MODIFIED_TIME = "modifiedTime", IS_MODIFIED = "isModified";

    public void define() {
        this.addField(MODIFIED_TIME, new TimestampField());
        this.addField(IS_MODIFIED, new BooleanField(false));
    }

    public void addField(final String name, Field f) {
        super.addField(name, f);
        if (f instanceof TimeAwareField) {
            TimeAwareField field = (TimeAwareField) f;
            field.setParent(this);
        }
    }

    public void updateTime() {
        if (isntLoading) {
            ((TimestampField) getField(MODIFIED_TIME)).setCurrent();
            setIsModified(true);
        }
    }

    public void setIsModified(boolean newValue) {
        ((BooleanField) getField(IS_MODIFIED)).setValue(newValue);
    }

    /**
     * it's mostly for debug  purpose
     *
     * @return true if node is modified
     */
    public boolean isModified() {
        return ((BooleanField) getField(IS_MODIFIED)).getBooleanValue();
    }

    protected boolean isntLoading = true;

    public void loadWhere(BasicCondition condition) throws LoadException {
        isntLoading = false;
        super.loadWhere(condition);
        isntLoading = true;
    }

    public void loadWhere(Pair[] condition) throws LoadException {
        isntLoading = false;
        super.loadWhere(condition);
        isntLoading = true;
    }


    public long getModifiedTime() {
        return ((TimestampField) getField(MODIFIED_TIME)).getLongValue();
    }

    /**
     * Overload this method if something should be done before send
     */
    protected void beforeSend() {
        beforeSave();
    }

    public void send(DataOutput out) throws IOException {
        beforeSend();
        getID();    //generate id in case it wasn't generated
        for (String fieldName : classFieldNames) {
            Field field = getField(fieldName);
            if ((!field.isUnsendable()) && (!fieldName.equals(MODIFIED_TIME)) && (!fieldName.equals(IS_MODIFIED))) {
                out.writeUTF(fieldName);
                out.writeUTF(field.getValue());
            }
        }
        out.writeUTF(IS_MODIFIED);
        out.writeUTF(get(IS_MODIFIED));
        out.writeUTF(MODIFIED_TIME);
        out.writeUTF(get(MODIFIED_TIME));

    }

    public void receive(DataInput in) throws IOException {
        isntLoading = false;
        String fieldName, fieldValue;
        do {
            fieldName = in.readUTF();
            fieldValue = in.readUTF();
            set(fieldName, fieldValue);
        } while (!fieldName.equals(MODIFIED_TIME));
        setID(get(ID_FIELD_NAME));  //storing into cache
        isntLoading = true;
//        afterReceive();   this method will be called, manually, after receive
    }

    /**
     * overload this method if something special should be done after load
     * this is done manually after all objects where received so if some object
     * need to reference some objict which might be uploaded later in the same session
     * <p/>
     * WARNING! This method is called only on client, on server this method is not called!
     */
    public void afterReceive() {
        afterLoad();
    }

    public void copyFrom(DBPersistentObject obj) {
        isntLoading = false;
        super.copyFrom(obj);
        isntLoading = true;
    }

    public void addObjects(Set<NetObject> objectsToSend, Set receivedIDs) {
        ObjectList result;
        result = this.loadObjects(Condition.var(IS_MODIFIED).Equal(true));
        NetObject object;

        while (result.hasNext()) {
            object = (NetObject) result.next();
            String id = object.getID();
            if (!receivedIDs.contains(id) && object.sendWithAll()) {
                objectsToSend.add(object);
                object.setIsModified(false);
                object.save();
            }
        }
    }

    /**
     * @return true if object must be sent with all other object, false if it is some special case
     */
    public boolean sendWithAll() {
        return true;
    }

    public void synchronizeTime(long deltaTime) {
        set(MODIFIED_TIME, "" + (getModifiedTime() + deltaTime));
    }

    abstract public User getParentUser();

    public boolean isUntossable() {
        return getParentUser() == null;
    }
    protected void setUnsentable(String fieldName) {
        getField(fieldName).setUnsentable(true);
    }

    public void tossDebug(String userID) {

    }
}
