package tcom.jslope.briskproject.networking;

import com.jslope.briskproject.networking.Tosser;
import com.jslope.briskproject.networking.ObjectsToSend;
import com.jslope.toDoList.core.User;
import com.jslope.toDoList.core.Task;
import com.jslope.toDoList.core.Message;
import com.jslope.toDoList.core.TaskSharer;
import com.jslope.toDoList.core.persistence.NetObject;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * Date: 26.01.2006
 */
public class TestTossing extends TestCase  {
    public void testTossing() {
        User user1 = new User(), user2 = new User(), user3 = new User();
        Task task1 = new Task(), task2 = new Task(), task3 = new Task();
//        user1.setParent(new User());//so user1 will not be root
        user1.setName("user1");
        user2.setName("user2");
        user3.setName("user3");
        task1.setSubject("task1");
        task2.setSubject("task2");
        task3.setSubject("task3");
        user2.setParent(user1);
        user3.setParent(user2);
        task1.setParent(user3);
        task2.setParent(task1);
        task3.setParent(user2);
        user1.save(); user2.save(); user3.save();
        task1.save(); task2.save(); task3.save();
        Set<NetObject> objects = new HashSet<NetObject>();
        objects.add(task1);
        objects.add(task2);
        Set<NetObject> objects2 = new HashSet<NetObject>();
        objects2.add(task3);
        objects2.add(user3);
        Tosser.addObjects(objects, user3.getID());
        Tosser.addObjects(objects2, user3.getID());
        Tosser.waitUntilEmpty();
        System.out.println("tossing finished");
        assertTrue(ObjectsToSend.contains(user2.getID(), task1.getID()));
        assertTrue(ObjectsToSend.contains(user1.getID(), task1.getID()));
        assertTrue(ObjectsToSend.contains(user2.getID(), task2.getID()));
        assertTrue(ObjectsToSend.contains(user1.getID(), task2.getID()));
        ObjectsToSend objs = ObjectsToSend.forUser(user2);
        List<NetObject> tossedObjs = new Vector<NetObject>();
        while (objs.hasObjects()) {
            tossedObjs.add(objs.next());
        }
        System.out.println(" tossedObjs = " + tossedObjs);
        assertTrue(tossedObjs.contains(task1));
        assertTrue(tossedObjs.contains(task2));
        assertTrue(!ObjectsToSend.contains(user2.getID(), task1.getID()));
        assertTrue(!ObjectsToSend.contains(user2.getID(), task2.getID()));
        for (NetObject obj : tossedObjs) {
            assertTrue(!ObjectsToSend.contains(user2.getID(), obj.getID()));
        }
    }
    final static int numUsers = 50, numTasks = 20;
    public void testSharedTossing() {
        User userRoot = new User();
        userRoot.setName("Root User");
        userRoot.save();

        Task basicTask = new Task();
        basicTask.setSubject("basic task");
        basicTask.setParent(userRoot);
        basicTask.save();
        User prevUser = userRoot;

        Vector<User> users = new Vector<User>();
        for (int i=0;i<numUsers;i++) {
            User tmp = new User();
            tmp.setName("user"+i);
            TaskSharer.add(basicTask, tmp);
            tmp.setParent(prevUser);
            tmp.save();
            users.add(tmp);
            prevUser = tmp;
        }
        Vector<Task> tasks = new Vector<Task>();
        Task prevTask = basicTask;
        for (int i=0;i<numTasks;i++) {
            Task tmp = new Task();
            tmp.setSubject("task"+i);
            tmp.setParent(prevTask);
            tmp.save();
            tasks.add(tmp);
            prevTask = tmp;
        }
        Vector<Message> messages = new Vector<Message>();
        for (int i=0;i<numUsers;i++) {
            Message tmp = new Message();
            tmp.setContent(" message "+i);
            tmp.setParent(prevTask.getID());
            tmp.setUser(users.get(i));
            tmp.save();
            messages.add(tmp);
        }
        Set<NetObject> tasksToSend = new HashSet<NetObject>();
        tasksToSend.addAll(tasks);
        Tosser.addObjects(tasksToSend, userRoot.getID());

        Set<NetObject> messagesToSend = new HashSet<NetObject>();
        messagesToSend.addAll(messages);
        Tosser.addObjects(messagesToSend, userRoot.getID());
        Tosser.waitUntilEmpty();
        Tosser.addObjects(new HashSet<NetObject>(), prevUser.getID());  // adding one empty set so that we'll wait until tossing ends
        Tosser.waitUntilEmpty();
        System.out.println("tossing finished");

        for (User user : users) {
            for (Task task : tasks) {
                assertTrue(ObjectsToSend.contains(user.getID(), task.getID()));
            }
            for (Message msg : messages) {
                System.out.println(" checking user "+user.getName() + " message "+msg+ " with id= "+msg.getID());
                assertTrue(ObjectsToSend.contains(user.getID(), msg.getID()));
            }
        }
    }
}
