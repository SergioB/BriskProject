package com.jslope.toDoList;

import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.Task;
import com.jslope.persistence.sql.SqlUtil;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.ObjectList;

/**
 * Date: 02.12.2005
 */
public class Upgrade {
    public static void main(String[] args) {
        User root = Options.getInstance().getUser();
        String internalTaskID = root.getInternalTask().getID();
        System.out.println(" internal task id = "+internalTaskID);
        root.set(User.MODIFIED_TIME, "0");
        root.save();
        Task task = new Task();
        ObjectList result = task.loadObjects(Condition.var(Task.PARENT).Equal(root.getID()));
        while(result.hasNext()) {
            task = (Task)result.next();
            System.out.println("fixing task"+task);
            task.set(Task.PARENT, internalTaskID);
            task.set(Task.ASSIGNED_TO, root.getID());
            task.save();
        }
        SqlUtil.closeDatabase();
    }
}
