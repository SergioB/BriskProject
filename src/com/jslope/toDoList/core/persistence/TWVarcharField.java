package com.jslope.toDoList.core.persistence;

import com.jslope.persistence.fields.VarcharField;

/**
 * Date: 19.10.2005
 */
public class TWVarcharField extends VarcharField implements TimeAwareField {

    /**
     * @param size length of the field in the database
     */
    public TWVarcharField(int size) {
        super(size);
    }

    public TWVarcharField(int size, String defaultValue) {
        super(size, defaultValue);
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
