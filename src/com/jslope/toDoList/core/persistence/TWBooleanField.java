package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.fields.BooleanField;

/**
 * Date: 19.10.2005
 */
public class TWBooleanField extends BooleanField implements TimeAwareField {

    public TWBooleanField(boolean value) {
        this.setDefaultValue(booleanValue(value));
    }

    NetObject parent = null;
    public void setParent(NetObject parent) {
        this.parent = parent;
    }

    public void setValue(String newValue) {
        if (!newValue.equals(getValue())) {
            if (parent != null) {
                parent.updateTime();
            }
            super.setValue(newValue);
        }
    }


}
