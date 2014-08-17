package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.fields.VarcharField;
import com.jslope.persistence.fields.TextField;
import com.jslope.persistence.fields.BooleanField;
import com.jslope.persistence.fields.IntField;

/**
 * Date: 19.10.2005
 */
public class TWIntField extends IntField implements TimeAwareField{

    public TWIntField() {
        super();
    }

    public TWIntField(int n) {
        super(n);
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
