package tcom.jslope.core;

import junit.framework.TestCase;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Task;
import com.jslope.persistence.sql.SqlUtil;

/**
 * Date: 18.10.2005
 */
public class TestIDs extends TestCase {
    public void testIDs() {
        try {
            User user = new User();
            Task task = new Task();
            System.out.println(" user:"+user.getID() + " order "+user.getOrder());
            assertTrue(user.getID().substring(0, 3).equals("100"));
            assertTrue(task.getID().substring(0, 3).equals("101"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception catched");
        }
        SqlUtil.closeDatabase();
    }
}
