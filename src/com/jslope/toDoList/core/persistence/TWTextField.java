package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.fields.VarcharField;
import com.jslope.persistence.fields.TextField;

/**
 * Date: 19.10.2005
 */
public class TWTextField extends TextField implements TimeAwareField {



    public TWTextField(String defaultValue) {
        super(defaultValue);
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
