package com.jslope.toDoList.core;

import static com.jslope.toDoList.core.Transportables.MESSAGE;
import com.jslope.toDoList.core.persistence.*;
import com.jslope.persistence.fields.IDField;
import com.jslope.persistence.fields.TimestampField;
import com.jslope.persistence.fields.TextField;
import com.jslope.utils.Log;

import java.util.Date;

/**
 * Date: 28.12.2005
 */
public class Message extends TaskBelonged {
    public final static String CREATION_TIME = "creationTime", LAST_EDIT_TIME = "lastEditedTime", CONTENT = "content",
    USER = "user", USER_NAME = "userName";

    //debug fields:
    private static final String MODIFED_INFO = "modInfo";
    private static final String TOSSING_INFO = "tossInfo";

    public void define() {
        super.define();
        this.addField(CONTENT, new TWTextField(""));
        this.addField(CREATION_TIME, new TimestampField());
        this.addField(USER, new IDField(Options.getUserID()));
        this.addField(USER_NAME, new TWVarcharField(User.MAX_NAME_LENGHT));

        //debug info:
        this.addField(MODIFED_INFO, new TextField(""));
        this.addField(TOSSING_INFO, new TextField(""));
        updateTime();
    }

    /**
     * this method is for debug purposes only
     * @param user
     */
    public void setUser(User user) {
        set(USER, user.getID());
    }
    public String getContent() {
        return get(CONTENT);
    }

    public String getUserName() {
        return get(USER_NAME);
    }

    public void setContent(String text) {
        set(CONTENT, text);
    }

    protected int getOrder() {
        return MESSAGE.getID();
    }

    public String toString() {
        return "Message:" + getContent() + " is modified " + isModified();
    }

    /**
     * Set's the user name and then saves.
     */
    public void updateAndSave() {
        this.set(USER_NAME, Options.getLoggedUser().getName());
        this.save();
    }

    public Date getCreationDate() {
        return ((TimestampField) this.getField(CREATION_TIME)).getDate();
    }

    public long getCreationDateTimestamp() {
        return ((TimestampField) this.getField(CREATION_TIME)).getLongValue();
    }

    public Date getLastEditedDate() {
        return ((TimestampField) this.getField(MODIFIED_TIME)).getDate();
    }

    public void synchronizeTime(long deltaTime) {
        super.synchronizeTime(deltaTime);
        set(CREATION_TIME, "" + (getCreationDateTimestamp() + deltaTime));
    }

    public void afterReceive() {
        super.afterReceive();
        Task parent = getParent();
        if (parent.getRealIsActiveValue()) {
            parent.turnNewlyModifiedOn();
        }
    }

    protected void beforeSend() {
        super.beforeSave();
    }

    public void updateTime() {
        boolean oldIsModified = isModified();   //only for debug purposes
        super.updateTime();
        //now following is only for debug purposes:
        if (isModified() == true && oldIsModified == false) {
            String modifiedInfo = " modified on " + new Date();
            if (Options.isOnServer()) {
                modifiedInfo += " on server ";
            } else {
                modifiedInfo += " on client ";
            }
            modifiedInfo += " by " + Options.getLoggedUser().getName();
            set(MODIFED_INFO, modifiedInfo);
            save();
        }
    }

    public void tossDebug(String userID) {
        set(TOSSING_INFO, " tossed on "+new Date() + " received from user = "+loadObject(userID));
        save();
    }

    public void showDebugInfo() {
        Log.debug(" Message: "+getContent() );
        Log.debug(get(MODIFED_INFO));
        Log.debug(get(TOSSING_INFO));
    }
}
