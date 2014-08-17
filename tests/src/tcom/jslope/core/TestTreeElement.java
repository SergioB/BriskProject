package tcom.jslope.core;

import junit.framework.TestCase;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Task;

import java.util.List;

/**
 * Date: 16.01.2006
 */
public class TestTreeElement extends TestCase  {
    public void testGetParentUser() {
        User user = new User();
        user.setParent(new User()); // user should not be root otherwise getParentUser doesn't work
        Task task = new Task();
        task.setParent(user);
        assertTrue(user == task.getParentUser());
    }
    public void testGetParentUserNonDirectParent() {
        User user = new User();
        user.setParent(new User()); // user should not be root otherwise getParentUser doesn't work
        Task task = new Task();
        task.setSubject("task");
        task.setParent(user);
        Task task1 = new Task();
        task1.setSubject("task 01");
        task1.setParent(task);
        assertTrue(user == task1.getParentUser());
    }

    public void testGetAllParentUsersUntil() {
        User user1 = new User(), user2 = new User(), user3 = new User();
        Task task1 = new Task(), task2 = new Task(), task3 = new Task();
//        user1.setParent(new User());//so user1 will not be root
        user2.setParent(user1);
        user3.setParent(user2);
        task1.setParent(user3);
        task2.setParent(task1);
        task3.setParent(user2);
        //test move up 1 node
        List<User> users = task2.getAllParentUsersUntil(task3, task2.getParentUser());
        assertTrue(users.contains(user3));
        assertTrue(!users.contains(user2));

        //test move up 2 nodes
        users = task2.getAllParentUsersUntil(user1, task2.getParentUser());
        assertTrue(users.contains(user3));
        assertTrue(users.contains(user2));

        //testing that if moved to children it will return an empty set
        users = task3.getAllParentUsersUntil(user3, task3.getParentUser());
        assertTrue(users.size() == 0);
    }

    public void testShoulReindex() {
        User user1 = new User();
        Task task1 = new Task(), task2 = new Task(), task3 = new Task();
        user1.add(task1);
        user1.add(task2);
        user1.add(task3);
        assertTrue(user1.shouldReindex(task1) == false);
        assertTrue(user1.shouldReindex(task2) == false);
        assertTrue(user1.shouldReindex(task3) == false);
        task1.setIndex(1);
        assertTrue(user1.shouldReindex(task1));
        assertTrue(user1.shouldReindex(task2));
        assertTrue(user1.shouldReindex(task3) == false);
        task3.setIndex(5);
        assertTrue(user1.shouldReindex(task3));
    }

}
