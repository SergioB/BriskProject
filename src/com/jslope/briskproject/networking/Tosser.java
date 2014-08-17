package com.jslope.briskproject.networking;

import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.toDoList.core.*;
import com.jslope.utils.Log;
import com.jslope.persistence.DBPersistentObject;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Date: 24.01.2006
 */
public class Tosser implements Runnable {
    static {
        init();
    }

    private static BlockingQueue<Addition> queue;

    /**
     * initialize this class and creates new thread with it
     */
    private static void init() {
        queue = new LinkedBlockingQueue<Addition>();
        new Thread(new Tosser()).start();
    }

    public static void addObjects(Set<NetObject> receivedObjects, String loggedUserID) {
        queue.add(new Addition(receivedObjects, loggedUserID));
    }

    public void run() {
        Log.debug("Tosser started");
        while (true) {
            try {
                queue.take().toss();
            } catch (InterruptedException e) {
                Log.error("Exception in tosser:", e);
            } catch (Exception e) {
                Log.error("Exception in tosser:", e);
            }
        }
    }

    public static void waitUntilEmpty() {
        while (queue.size() > 0) ;
    }

    static class Addition {
        Set<NetObject> objects;
        String userID;
        private Set<String> usersDone;
        private HashMap<String, QuickSharer> sharedTasks;

        Addition(Set<NetObject> receivedObjects, String loggedUserID) {
            objects = receivedObjects;
            userID = loggedUserID;
        }

        public void toss() {
            initTosser();
            for (NetObject obj : objects) {
                if (!obj.isUntossable()) {
                    tossOneObject(obj);
                }
            }
        }

        private void tossOneObject(NetObject obj) {
            Log.debug("tossing object " + obj);
            usersDone = new HashSet<String>();
            usersDone.add(userID);
            if (obj instanceof TaskSharer) {
                processTaskSharer((TaskSharer)obj);
            }
            String objectID = obj.getID();
            if (obj instanceof Task) {
                for (String userID : getUsersToAddForShare((Task) obj)) {
                    addObjectToSendForUserMultiple(userID, objectID);
                }
            } else if (obj instanceof TaskBelonged) {
                Task task = ((TaskBelonged) obj).getParent();
                for (String userID : getUsersToAddForShare(task)) {
                    addObjectToSendForUserMultiple(userID, objectID);
                }
            }
            User parent = obj.getParentUser();
            boolean moveForward = false;
            do {// adding object to parent userIDs
                for (User user : parent.getUsersAtTheSameLevel()) {
                    addObjectToSendForUser(user, objectID);
                }
                moveForward = !parent.isRoot();
                parent = (User) parent.getParent();
            } while (moveForward);
        }

        private void processTaskSharer(TaskSharer taskSharer) {
            Log.debug("tossing TaskSharer");
            if (!taskSharer.isDeleted()) {
//                            String sharerUserID = taskSharer.getUserID();
                List<String> userIDs = taskSharer.getUserIDsUntil();
                for (NetObject task : taskSharer.getAllSubtasks()) {
                    Log.debug("+adding shared  task: " + task);
                    for (String sharedUserID : userIDs) {
                        addObjectToSendForUserMultiple(sharedUserID, task.getID());
                    }
                }
            }
        }

        private void initTosser() {
            sharedTasks = new HashMap<String, QuickSharer>();
        }

        /**
         * This task returns all the userIDs for a certain task, if the task is not shared then
         * it returns an empty set of userIDs
         *
         * @param originalTask
         * @return users to be added to share
         */
        private Iterable<String> getUsersToAddForShare(Task originalTask) {
            Task task = originalTask;
            HashSet<Task> tasks = new HashSet<Task>();
            QuickSharer previousQuickSharer = null;
            do {
//                if (hasShares(task)) {
//                    userIDs.addAll(getUsersForShare(task));
//                }
                tasks.add(task);
                if (sharedTasks.containsKey(task.getID())) {
                    QuickSharer tempSharer = sharedTasks.get(task.getID());
                    addSharedTasks(tasks, tempSharer);
                    return sharedTasks.get(originalTask.getID()).getUserIDs();
                } else {
                    if (task.hasShares()) {
                        if (previousQuickSharer == null) {
                            previousQuickSharer = new QuickSharer(task);
                        } else {
                            QuickSharer tempSharer = previousQuickSharer;
                            previousQuickSharer = new QuickSharer(task);
                            tempSharer.setParent(previousQuickSharer);
                        }
                        addSharedTasks(tasks, previousQuickSharer);
                        tasks = new HashSet<Task>();
                    }
                }
                task = task.getParentTask();
            } while (!task.isRoot());
            addEmptySharedTasks(tasks);
            return sharedTasks.get(originalTask.getID()).getUserIDs();
        }

        /**
         * @param tasks
         * @param quickSharer userIDs associated with certain  quickSharer and it's parents
         */
        private void addSharedTasks(HashSet<Task> tasks, QuickSharer quickSharer) {
            for (Task task : tasks) {
                sharedTasks.put(task.getID(), quickSharer);
            }
        }

        /**
         * it adds tasks which don't have any shares up until root
         *
         * @param tasks
         */
        private void addEmptySharedTasks(HashSet<Task> tasks) {
            QuickSharer empty = new QuickSharer();
            addSharedTasks(tasks, empty);
        }

        class QuickSharer {
            Collection<String> userIDs;

            /**
             * create an  empty QuickSharer
             */
            protected QuickSharer() {
                userIDs = new HashSet<String>();
            }

            QuickSharer(Task task) {
                this(task.getSharedUserIDs());
            }

            private QuickSharer(Collection<String> userIDs) {
                this.userIDs = userIDs;
            }

            public Collection<String> getUserIDs() {
                if (parent == null) {
                    return userIDs;
                } else {
                    Collection<String> retUsers = new HashSet<String>();
                    retUsers.addAll(userIDs);
                    retUsers.addAll(parent.getUserIDs());
                    return retUsers;
                }
            }

            QuickSharer parent = null;

            public void setParent(QuickSharer previousQuickSharer) {
                parent = previousQuickSharer;
            }
        }

        private void addObjectToSendForUser(User user, String objectID) {
            String userID = user.getID();
            if (!usersDone.contains(userID)) {
                if (!ObjectsToSend.contains(userID, objectID)) {
                    Log.debug("adding to user " + user);
                    ObjectsToSend.add(userID, objectID);
                }
                usersDone.add(userID);
            }
        }

        private void addObjectToSendForUserMultiple(String toSenduserID, String objectID) {
            if (!toSenduserID.equals(userID)) {
                if (!ObjectsToSend.contains(toSenduserID, objectID)) {
                    if (Options.isDebugMode()) {
                    Log.debug("adding to user "+DBPersistentObject.loadObject(toSenduserID));
                        NetObject obj = (NetObject)NetObject.loadObject(objectID);
                        obj.tossDebug(userID);
                    }
                    ObjectsToSend.add(toSenduserID, objectID);
                }
            }
        }
    }
}
