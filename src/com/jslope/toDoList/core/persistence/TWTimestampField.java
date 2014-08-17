package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.fields.*;

/**
 * Date: 19.10.2005
 */
public class TWTimestampField extends TimestampField implements TimeAwareField{

    public TWTimestampField(long n) {
        super(n);
    }
    public TWTimestampField() {
        super();
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
