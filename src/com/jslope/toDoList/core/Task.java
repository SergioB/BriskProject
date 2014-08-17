package com.jslope.toDoList.core;

import static com.jslope.toDoList.core.Transportables.TASK;
import com.jslope.persistence.PersistenceException;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.DBPersistentObject;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.fields.*;
import com.jslope.persistence.fields.interfaces.DateContainer;
import com.jslope.utils.Utils;
import com.jslope.utils.Log;
import com.jslope.UI.*;
import com.jslope.toDoList.core.persistence.*;
import com.jslope.briskproject.networking.ClientProtocol;

import javax.swing.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Date: 07.03.2005
 */

public class Task extends TreeElement {

//    String subject, notes;
    public static final String SUBJECT = "subject", NOTES = "notes", DATE = "date"
    , IS_ACTIVE = "is_active", TIME_SPENT = "time_spent", FINISHED_DATE = "finished_date"
    , ASSIGNED_TO = "assignedTo", IS_INTERNAL_TASK = "isInternalTask", CHILDREN_TIME = "childrenTime"
    , OWNER = "ownerUser", ASSIGNED_INDEX = "assignedIndex", IS_DIRTY = "isDirty"
    , IS_PARENT_DIRTY = "isParentDirty", MUST_REOPEN_PARENT = "mustReopenParent"
    , IS_NEWLY_MODIFIED = "isNewlyModified";
    private boolean isPaused = true;
    TimeInterval currentInterval = null;
    static Task activeTask = null;
    /**
     * means that task is still not done
     */
    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public static final int MAX_SUBJECT_LENGHT = 255;

    public void define() {
        super.define();
        this.addField(SUBJECT, new TWVarcharField(MAX_SUBJECT_LENGHT, "empty"));
        this.addField(NOTES, new TWTextField(""));
        this.addField(DATE, new TWDateField());
        this.addField(IS_ACTIVE, new TWBooleanField(true));
        this.addField(FINISHED_DATE, new TWTimestampField(0));
        this.addField(ASSIGNED_TO, new TWIDField());
        this.addField(OWNER, new TWIDField(Options.getUserID()));
        this.addField(IS_INTERNAL_TASK, new TWBooleanField(false));
        this.addField(TIME_SPENT, new TWIntField(0));
        this.addField(CHILDREN_TIME, new TWIntField(0));  // time spent by subtasks
        this.addField(ASSIGNED_INDEX, new TWIntField(0));
        this.addField(IS_DIRTY, new TWBooleanField(false));
        this.addField(IS_PARENT_DIRTY, new TWBooleanField(false));
        this.addField(MUST_REOPEN_PARENT, new TWBooleanField(false));
        this.addField(IS_NEWLY_MODIFIED, new BooleanField(false));
        setUnsentable(IS_NEWLY_MODIFIED);
        isActive = true;
    }

    public Task() {
        super();
    }


    public Task(String subject, String note) {
        super();
        setSubject(subject);
        setNotes(note);
        try {
            this.save();
        } catch (PersistenceException e) {
            System.out.println("Unable to save task with subject =" + subject);
            e.printStackTrace();
        }
    }

    public int getIndex() {
        if (isShared) {
            return taskSharer.getIndex();
        } else {
            if (isAssignedToUser() && Options.isShowingUsers()) {
                return ((IntField) this.getField(ASSIGNED_INDEX)).getIntValue();
            } else {
                return ((IntField) this.getField(SORT_INDEX)).getIntValue();
            }
        }
    }

    public void setIndex(int newIndex) {
        if (isShared) {
            taskSharer.setIndex(newIndex);
        } else {
            if (isAssignedToUser() && Options.isShowingUsers()) {
                ((IntField) this.getField(ASSIGNED_INDEX)).setIntValue(newIndex);
            } else {
                ((IntField) this.getField(SORT_INDEX)).setIntValue(newIndex);
            }
        }
    }


    public Task(String subject, String note, int index) {
        this(subject, note);
        this.setIndex(index);
    }

    public String getSubject() {
        return this.get(SUBJECT);
    }

    public void setSubject(String value) {
        this.set(SUBJECT, value);
    }

    public String getNotes() {
        return get(NOTES);
    }

    public void setNotes(String note) {
        this.set(NOTES, note);
    }

    public int getRow() {
        return this.getIndex();
    }

    public void updateTimeElapsed(JLabel time) {
        int interval = getTimeSpent(),
                childrenTime = getChildrenTime();
        if (!isPaused()) {
            interval += currentInterval.getSeconds();
        }
        int totalInterval = interval + childrenTime;
        time.setText(Utils.formatTime(totalInterval));
        String toolTip = "";
        if (childrenTime > 0) {
            toolTip = "sum of personal time: " + Utils.formatTime(interval) + " + subtasks time: " + Utils.formatTime(childrenTime);
        }
        time.setToolTipText(toolTip);
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void resume() {
        if (activeTask != null) {
            if (activeTask != this) {
                activeTask.pause();
                activeTask = this;
            }
        } else {
            activeTask = this;
        }
        ActiveTaskButton.setEnabled(true);
        currentInterval = new TimeInterval();
        currentInterval.setParent(this);
        isPaused = false;
    }

    public void pause() {
        if (!isPaused()) {
            if (currentInterval != null) {
                try {
                    currentInterval.finishAndSave();
                    int interval = getTimeSpent();
                    interval += currentInterval.getSeconds();
                    setTimeSpent(interval);
                    addTimeToParent(currentInterval.getSeconds());
                    this.save();
                } catch (PersistenceException e) {
                    System.out.println("unable to save time interval");
                    e.printStackTrace();
                }
            }
        }
        isPaused = true;
    }

    public boolean isInternalTask() {
        return ((TWBooleanField) (getField(IS_INTERNAL_TASK))).getBooleanValue();
    }

    protected void addTimeOfChildren(Task element) {
        int seconds = element.getTotalTime();
        addTimeOfChildren(seconds);
    }

    private void addTimeOfChildren(int seconds) {
        justAddChildrenTime(seconds);
        addTimeToParent(seconds);
        updateChildrenModifiedTime();
    }


    public int getChildrenTime() {
        if (isDirty()) {
            return computeChildrenTime();
        } else {
            return ((IntField) this.getField(CHILDREN_TIME)).getIntValue();
        }
    }


    private int computeChildrenTime() {
        int childrenTime = 0;
        for (Task task : loadChildrenTasks()) {
            childrenTime += task.getTotalTime();
        }
        setChildrenTime(childrenTime);
        setIsDirty(false);
        save();
        return childrenTime;
    }

    protected List<Task> loadChildrenTasks() {
        ObjectList result = this.loadObjects(Condition.var(PARENT).Equal(this.getID()));
        Vector<Task> retValue = new Vector<Task>();
        while (result.hasNext()) {
            retValue.add((Task) result.next());
        }
        return retValue;
    }

    private void setIsDirty(boolean newValue) {
        ((TWBooleanField) getField(IS_DIRTY)).setValue(newValue);
    }

    private boolean isDirty() {
        return ((TWBooleanField) getField(IS_DIRTY)).getBooleanValue();
    }

    private void setChildrenTime(int newTime) {
        ((IntField) this.getField(CHILDREN_TIME)).setIntValue(newTime);
    }

    protected void justAddChildrenTime(int seconds) {
        System.out.println("adding " + seconds + " to " + this);
        int childrenTime = getChildrenTime();
        childrenTime += seconds;
        setChildrenTime(childrenTime);
        this.save();
    }


    protected void justSubstractTime(int seconds) {
        justAddChildrenTime(-seconds);
    }

    protected void insertAt(int index, TreeElement element) {
        super.insertAt(index, element);
        addRoutine((Task) element);
    }


    public void addTimeToParent(int seconds) {
        if (seconds != 0) {
            if (haveParentTask()) {
                if (parentTaskIsHere()) {
                    Task directParent = getParentTask();
                    directParent.addTimeOfChildren(seconds);
                } else {
                    setParentDirty(true);
                }
            }
        }
    }

    private void setParentDirty(boolean newValue) {
        ((TWBooleanField) getField(IS_PARENT_DIRTY)).setValue(newValue);
        save();
    }


    boolean haveParentTask() {
        return !get(PARENT).equals(TWIDField.null_id);
    }

    /**
     * removes one child
     *
     * @param treeElement
     */
    protected void remove(TreeElement treeElement) {
        super.remove(treeElement);
        substractTimeOfChild(((Task) treeElement).getTotalTime());
    }

    /**
     * adding existing node
     *
     * @param element
     */
    public void add(TreeElement element) {
        super.add(element);
        addRoutine((Task) element);
    }

    private void addRoutine(Task element) {
        addTimeOfChildren(element);
    }


    /**
     * is used when Task is deleted
     *
     * @param seconds
     */
    public void substractTimeOfChild(int seconds) {
        addTimeOfChildren(-seconds);
    }


    protected int getTotalTime() {
        return this.getChildrenTime() + this.getTimeSpent();
    }

    public User getAssignedUser() {
        return (User) loadObject(get(ASSIGNED_TO));
    }

    public String getAssignedUserID() {
        return get(ASSIGNED_TO);
    }

    public void deleteObject() {
        TimeInterval interval = new TimeInterval();
        interval.deleteObjects(Condition.var(TimeInterval.PARENT_TASK).Equal(this.getID()));
        this.substragTimeFromParent(this.getTimeSpent());
        super.deleteObject();
    }

    private void substragTimeFromParent(int timeSpent) {
        if (timeSpent != 0) {
            addTimeToParent(-timeSpent);
        }
    }

    public void virtualDelete() {
        TimeInterval interval = new TimeInterval();
        ObjectList result;
        result = interval.loadObjects(Condition.var(TimeInterval.PARENT_TASK).Equal(this.getID()));
        while (result.hasNext()) {
            interval = (TimeInterval) result.next();
            interval.virtualDelete();
        }
        //saving ASSIGNED_TO
        String assigned = get(ASSIGNED_TO);
        if (Options.isShowingUsers() && Options.isNetworkClient()) {
            Options.setShowUsers(false);//setting flag that users are hidden so we can
            this.loadChildren();        //reload all the child tasks, not just those which are not assigned
            super.virtualDelete();      // we delete here so that all child tasks will not load again their children
            Options.setShowUsers(true);
        } else {
            super.virtualDelete();
        }
        set(ASSIGNED_TO, assigned);
        if (getParentTask() != null) {
            substragTimeFromParent(this.getTimeSpent());
        }
//        super.virtualDelete();
    }

    /**
     * Warning it doesn't show if task has started just now
     *
     * @return (it returns true only if task has started and then pause pressed at least once)
     */
    public boolean hasStarted() {
        return this.getTimeSpent() > 0;
    }

    public int getTimeSpent() {
        return ((IntField) this.getField(TIME_SPENT)).getIntValue();
    }

    private void setTimeSpent(int newValue) {
        ((IntField) this.getField(TIME_SPENT)).setIntValue(newValue);
    }

    /**
     * Set's task done and selects next element to be active
     */
    public void setDoneAndShowNext() {
        this.selectNextElement();
    }

    private void selectNextElement() {
        TreeElement nodeToSelect;
        nodeToSelect = selectNextNode();
        this.setDone(); // nodeToSelect must be choosen before setDone is called
        justRemoveFromParent();
        nodeToSelect = nodeToSelect.getNextLeafTask(); //it should be after task is done otherwise it will select itself
        if (!Options.getInstance().getHideTaskDone()) {
            getParent().addToTasksDone(this);
        }
        TreePanel.reload(getParent());
        if (nodeToSelect instanceof Task) {
            if (Options.getInstance().getAutostartTask()) {
                ((Task) nodeToSelect).resume();
            } else {
                activeTask = null;
            }
        }
        TreePanel.selectNode(nodeToSelect);
    }

    public void setActive(boolean newValue) {
        this.set(IS_ACTIVE, "" + newValue);
        isActive = newValue;
    }

    public void setDone() {
        for (TreeElement task : children) {
            ((Task) task).setDone();
        }
        setActive(false);
        ((DateContainer) this.getField(FINISHED_DATE)).setDate(new Date());  // set current date as finished date
        if (currentInterval == null) {
            currentInterval = new TimeInterval(); //this timeInterval is created in order that task to be shown in the list (it should be shown with almost 0 time in this case)
            currentInterval.setParent(this);
            currentInterval.finishAndSave();
        }
        if (!isPaused()) {
            pause(); // it also does a save, so there is no need to save in setDone()
        } else {
            try {
                this.save();
                currentInterval.justSave();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateEditor() {
        if (isActive) {
            EditorsPanel.select(Editors.TASK_PANEL);
            TaskEditor.getInstance().update(this);
        } else {
            EditorsPanel.select(Editors.TASK_DONE_PANEL);
            TaskDoneEditor.getInstance().update(this);
        }
    }

    public void updateDataFromEditor() {
        if (isActive) {
            TaskEditor.getInstance().updateData(this);
        }
    }


    protected boolean canChildMoveLeft() {
        if (isRoot()) {
            return false;
        } else {
            return getParent().canAcceptNewChildren(); //it always can move out to the parent of this
        }
    }

    public String toString() {
        return this.getSubject();
    }

    protected void afterLoad() {
        initIsActive();
        if (Options.isOnServer()) {
            initParent();
        } else {
            if (isActive && isNewlyModified()) {
                updateNewToParent();
            }
        }
        isShared = false;
    }

    private void updateNewToParent() {
        if (Options.isShowingUsers()) {
            if (!isAssignedToUser()) {
                addModifiedChildToParent();
            }
        } else {
            addModifiedChildToParent();
        }
    }

    private void addModifiedChildToParent() {
        Task parentTask = getParentTask();
        if (parentTask != null) {
            parentTask.addModifiedChild(this.getID());
            parentTask.updateNewToParent();
        }
    }

    private Set<String> modifiedChildrenInstance = null;

    private Set<String> modifiedChildren() {
        if (modifiedChildrenInstance == null) {
            modifiedChildrenInstance = new HashSet<String>();
        }
        return modifiedChildrenInstance;
    }

    private void addModifiedChild(String id) {
        modifiedChildren().add(id);
    }

    public void afterReceive() {
        initIsActive();
        if (!Options.isOnServer()) {
            if (parentIsDirty()) {

                propagateDirtyUp();
                setParentDirty(false);
            }
            if (mustPropagateReopen()) {
                this.reopen();
                setMustReopenParent(false);
            }
            turnNewlyModifiedOn();
        }

        isShared = false;
    }

    /**
     * this turns newly modified flag on, but with checking all the conditions before
     * and with turning havChildren parents flag after
     */
    public void turnNewlyModifiedOn() {
        if (isActive && ClientProtocol.isntFullRequest() && !isDeleted()) {
            setNewlyModified(true);
            updateNewToParent();
        }
    }

    private void propagateDirtyUp() {
        if (haveParentTask()) {
            if (parentTaskIsHere()) {
                Task directParent = getParentTask();
                directParent.setIsDirty(true);
                directParent.save();
                directParent.propagateDirtyUp();
            } else {
                setParentDirty(true);
            }
        }
    }

    private boolean parentIsDirty() {
        return ((TWBooleanField) getField(IS_PARENT_DIRTY)).getBooleanValue();
    }

    public void initParent() {
        String parentID;
        parent = null;
        if (isAssignedToUser()) {
            parentID = get(ASSIGNED_TO);
        } else {
            parentID = get(PARENT);
        }

        if (!parentID.equals(TWIDField.null_id)) {
            if (DBPersistentObject.existsObject(parentID)) {
                parent = (TreeElement) loadObject(parentID);
            }
        }
    }

    public void copyFrom(DBPersistentObject obj) {
        super.copyFrom(obj);
        initIsActive();
    }


    private void initIsActive() {
        isActive = getRealIsActiveValue();
    }

    public boolean getRealIsActiveValue() {
        return ((BooleanField) getField(IS_ACTIVE)).getBooleanValue();
    }

    public boolean canDelete() {
        if (ownerIsLogged()) {
            return super.canDelete();
        } else {
            return false;
        }
    }

    public boolean canMoveDown() {
        if (isActive && !isInternalTask()) {
            return super.canMoveDown();
        } else {
            return false;
        }
    }

    public boolean canMoveUp() {
        if (isActive && !isInternalTask()) {
            return super.canMoveUp();
        } else {
            return false;
        }
    }

    public boolean canMoveLeft() {
        if (canMove()) {
            return super.canMoveLeft();
        } else {
            return false;
        }
    }

    public boolean canMoveRight() {
        if (canMove()) {
            return super.canMoveRight();
        } else {
            return false;
        }
    }

    public boolean canAddSubtask() {
        return isActive && !isInternalTask(); //if task is active then can add subtask
    }

    public boolean canAddSiblingTask() {
        if (isActive) {
            if (isRoot()) {
                return false;
            } else {
                return getParent().canAcceptNewChildren();
            }
        } else {
            return false;
        }
    }

    private boolean ownerIsLogged() {
        return get(OWNER).equals(Options.getUserID());
    }

    public boolean canMove() {
        if (super.canMove()) {
            if (isActive && !isInternalTask()) {
                if (isShared) {
                    return false;
                } else if (isAssignedToUser()) {
                    if (ownerIsLogged()) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //todo: may be canDelete should have a speacial behavior when task is done
    //public boolean canDelete() {

    public Icon getIcon() {
        if (isActive) {
            if (isShared) {
                if (this.isNewlyModified()) {
                    return Buttons.sharedTaskNewIcon;
                } else {
                    if (this.hasNewChildren()) {
                        return Buttons.sharedTaskNewChildrenIcon;
                    } else {
                        return Buttons.sharedTaskIcon;
                    }
                }
            } else {
                if (this.isLeaf()) {
                    if (this.isNewlyModified()) {
                        return Buttons.taskNewIcon;
                    } else {
                        return Buttons.taskIcon;
                    }
                } else {
                    if (this.isNewlyModified()) {
                        return Buttons.manyTasksNewIcon;
                    } else if (this.hasNewChildren()) {
                        return Buttons.hasNewChildrenIcon;
                    } else {
                        return Buttons.manyTasksIcon;
                    }
                }
            }
        } else {
            if (isShared) {
                return Buttons.sharedTaskDoneIcon;
            } else {
                if (this.isLeaf()) {
                    return Buttons.taskDoneIcon;
                } else {
                    return Buttons.manyTasksDoneIcon;
                }
            }
        }
    }

    private boolean hasNewChildren() {
        return !modifiedChildren().isEmpty();
    }

    public Date getFinishedDate() {
        return ((DateContainer) this.getField(FINISHED_DATE)).getDate();
    }

    protected static final String DATE_FORMAT = "dd/MM/yyyy";
    final static SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT);

    public String getFinishedDateAsText() {
        return dateFormater.format(getFinishedDate());
    }

    public boolean canAcceptNewChildren() {
        return isActive && !isInternalTask();    //if is active then can accept new children
    }

    public void showDebugInfo() {
        Log.debug("This is task object " + toString() + " id=" + getID() + "\n parent = " + get(PARENT) +
                "\nassigned to: " + get(ASSIGNED_TO) + "\n modified time: " + getModifiedTime());
        if (isAssignedToUser()) {
            String userName = loadObject(get(ASSIGNED_TO)).get(User.NAME);
            Log.debug("assigned to user = " + userName);
        } else {
            Log.debug("task is not assigned to user ASSIGNED_TO = " + get(ASSIGNED_TO));
        }
        Log.debug("Task owner: " + get(OWNER));
        Log.debug("task index = " + getIndex());
        Log.debug("is modified = " + this.get(IS_MODIFIED));
        Log.debug("is deleted = " + this.isDeleted());
        Log.debug("is newly modified = " + this.isNewlyModified());
        Log.debug("hasNewChildren = " + this.hasNewChildren());
        if (this.hasNewChildren()) {
            for (String objectID : modifiedChildren()) {
                Task task = (Task) loadObject(objectID);
                Log.debug("mod child: " + task);
            }
        }
        for (Message msg : getMessages()) {
            msg.showDebugInfo();
        }
    }

    /**
     * This is a debug function
     */
    public void showTaskIntervals() {
        List<TaskBelonged> intervals = ((TimeInterval)Transportables.get(Transportables.TIME_INTERVAL)).getChildren(this);
        for (TaskBelonged taskBelonged : intervals) {
            TimeInterval interval = (TimeInterval) taskBelonged;
            System.out.println(" seconds: " + interval.getSeconds() + "     id: " + interval.getID());
            System.out.println(interval.getTimes());
        }
    }

    /**
     * This is a debug function
     */
    public int getTaskIntervalsSeconds() {
        int seconds = 0;
        List<TaskBelonged> intervals = ((TimeInterval)Transportables.get(Transportables.TIME_INTERVAL)).getChildren(this);
        for (TaskBelonged taskBelonged : intervals) {
            TimeInterval interval = (TimeInterval) taskBelonged;
            seconds += interval.getSeconds();
        }
        return seconds;
    }


    public static void pauseActiveTask() {
        if (activeTask != null) {
            activeTask.pause();
        }
    }

    public static Task getActiveTask() {
        return activeTask;
    }

    public String getFormatedTimeSpent() {
        int interval = getTimeSpent(); // + childrenTime = getChildrenTime()
        if (!isPaused()) {
            interval += currentInterval.getSeconds();
        }

        return Utils.formatTime(interval);
    }

    public boolean hasActiveChildren() {
        return children.size() > 0;
    }

    protected int getOrder() {
        return TASK.getID();
    }

    protected ChildrenTypes getType() {
        if (isActive) {
            if (isShared) {
                return ChildrenTypes.SHARED_TASKS;
            } else {
                return ChildrenTypes.TASKS;
            }
        } else {
            if (isShared) {
                return ChildrenTypes.SHARED_TASKS_DONE;
            } else {
                return ChildrenTypes.TASKS_DONE;
            }
        }
    }

    public void assignTo(User user) {
        set(ASSIGNED_TO, user.getID());
    }

    public boolean isAssignedToUser() {
        if (isShared) {
            return false;
        } else {
            return !get(Task.ASSIGNED_TO).equals(TWIDField.null_id);
        }
    }

    public void setParent(TreeElement newParent) {
        if (newParent instanceof User) {
            if (this.isShared) {
                //todo: to do what is needed for seting parent of shared task
            } else {
                if (this.isNew()) { //setting task parent internal task of user to which we'll assign the task
                    super.setParent(((User) newParent).getInternalTask());
                } else if (this.isAssignedToUser()) {    // then getParent will return a user
                    System.out.println(" parent = " + this.getParent());
                    User parent = (User) this.getParent();
                    if (parent != null) {
                        if (parent.getInternalTask().getID().equals(this.get(PARENT))) { // if users internal task was its parent
                            super.setParent(((User) newParent).getInternalTask());
                        }
                    }
                }
                // warning: parent should not change if task is not new and is was not assigned to another user
                assignTo((User) newParent);
            }
            parent = newParent;

        } else {
            super.setParent(newParent);
        }
    }

    public void unassing() {
        set(ASSIGNED_TO, TWIDField.null_id);
    }

    public void setLoadParent(TreeElement newParent) {
        parent = newParent;
    }

    public Task getParentTask() {    //it is sure to return parent task.
        if (parentTaskIsHere()) {
            return (Task) loadObject(get(PARENT));
        } else {
            return null;
        }
    }

    protected TreeElement getRealParent() {
        if (Options.isShowingUsers()) {
            return getParent();
        } else {
            if (isAssignedToUser()) {
                return getAssignedUser();
            } else {
                return getParent();
            }
        }
    }


    synchronized public boolean updateChildrenTime(long newTime) {
        if (super.updateChildrenTime(newTime)) {
            if (!isRoot()) {
                if (getParent() instanceof User) {
                    if (parentTaskIsHere()) {
                        Task task = getParentTask();    //possible exlude from updated tasks internal task, we don't need to update internal task because user is updated automatically
                        if (task != null) {
                            task.updateTimeRecursively(newTime);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @return true if parent task exists in this tree (it can exist only on server)
     */
    boolean parentTaskIsHere() {
        return objectExists(get(PARENT));
    }

    public void addObjectsToSend(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        LogicCondition taskCondition = Condition.var(TimeInterval.PARENT_TASK).Equal(this.getID())
                .AND(TimeInterval.MODIFIED_TIME).MoreThan(afterDate);
        ((TimeInterval) Transportables.get(Transportables.TIME_INTERVAL)).addObjectsToSend(objectsToSend, taskCondition, excludeList);
        ((Message) Transportables.get(Transportables.MESSAGE)).addObjectsToSend(objectsToSend, taskCondition, excludeList);
        super.addObjectsToSend(objectsToSend, afterDate, excludeList);
    }

    public String getOwnerString() {
        String ownerID = get(OWNER);
        if (User.userExists(ownerID)) {
            return "task owner: " + loadObject(ownerID);
        } else {
            return "task owned by [grand]parent";
        }
    }

    public static boolean isActiveTaskPaused() {
        if (activeTask != null) {
            return activeTask.isPaused();
        }
        return true;
    }

    public Iterable<Message> getMessages() {
        Message mes = (Message) Transportables.get(Transportables.MESSAGE);
        ObjectList result = mes.loadObjects(Condition.var(Message.PARENT_TASK).Equal(getID()), new String[]{Message.CREATION_TIME});
        Vector<Message> retValue = new Vector<Message>();
        while (result.hasNext()) {
            retValue.add((Message) result.next());
        }
        return retValue;
    }

    public boolean sendWithAll() {
        return !isInternalTask();
    }

    public void removeFromParent() {
        super.removeFromParent();
        if (parent != null) {
            if (parent instanceof User) {
                addTimeToParent(-getTotalTime());   //it adds time to another task which is direct parent of assigned task
            }
        }
    }

    public void addTimeToParent() {
        addTimeToParent(getTotalTime());
    }

    public User getParentUser() {
        TreeElement parentUser = this.getRealParent();
        try {
            if (parentUser instanceof User) {
                return (User) parentUser;
            }

            while (!parentUser.isRoot()) {  //todo: to investigate bug why on server side here parentUser might be null
                parentUser = parentUser.getRealParent();
                if (parentUser instanceof User) {
                    return (User) parentUser;
                }
            }
        } catch (NullPointerException e) {
            Log.debug("Null Pointer Exception");
            Log.error("Null Pointer Exception, it looks liek parentUser is null", e);
        }
        this.showDebugInfo();
        throw new RuntimeException("an object without a user parent: " + this + " object id:" + this.getID() + " class=" + getClass() + " latestParentUser = " + parentUser);
    }

    public void propagateDown() {
        super.propagateDown();
        for (NetObject child : TaskBelonged.getAllChildren(this)) {
            child.updateTime();
            child.save();
        }
    }

    private boolean isShared = false;

    public void setIsShared(boolean newValue) {
        isShared = newValue;
    }

    TaskSharer taskSharer = null;

    public void setIsShared(TaskSharer sharer) {
        taskSharer = sharer;
        setIsShared(true);
    }

    public boolean hasShares() {
        return TaskSharer.hasShare(this);
    }

    public Collection<String> getSharedUserIDs() {
        return TaskSharer.getSharedUserIDs(this);
    }

    public boolean canBeShared() {
        if (Options.isShowingUsers()) {
            return !isShared && isActive;
        }
        return false;
    }

    private void setMustReopenParent(boolean newValue) {
        ((TWBooleanField) getField(MUST_REOPEN_PARENT)).setValue(newValue);
        save();
    }

    private boolean mustPropagateReopen() {
        return ((TWBooleanField) getField(MUST_REOPEN_PARENT)).getBooleanValue();
    }


    public void reopen() {
        this.setActive(true);
        if (haveParentTask()) {
            if (parentTaskIsHere()) {
                Task parentTask = getParentTask();
                if (!parentTask.isInternalTask() && !parentTask.isActive()) {
                    parentTask.reopen();
                }
            } else {
                setMustReopenParent(true);
            }
        }
    }

    public boolean assignedUserIsHere() {
        return User.userExists(get(ASSIGNED_TO));
    }

    /**
     * it doesn an reset of all children,  it  is done for  TaskSharer in order to remove for
     * tasks which are already in cache, to avoid conflicts with already existing shared tasks
     */
    public void reset() {
        isShared = false;
        for (Task task : loadChildrenTasks()) {
            task.reset();
        }
    }

    public void setAsRead() {
        setNewlyModified(false);
        if (!this.hasNewChildren()) {
            removeNewFromParent();
        }
    }

    private void removeNewFromParent() {
        if (Options.isShowingUsers()) {
            if (!isAssignedToUser()) {
                removeModifiedChildFromParent();
            }
        } else {
            removeModifiedChildFromParent();
        }
    }

    private void removeModifiedChildFromParent() {
        Task parentTask = getParentTask();
        if (parentTask != null) {
            parentTask.removeModifiedChild(this.getID());
            parentTask.removeNewFromParent();
        }
    }

    private void removeModifiedChild(String id) {
        modifiedChildren().remove(id);
        if (modifiedChildren().isEmpty()) {
            updateThisInTree();
        }
    }


    public void setNewlyModified(boolean newValue) {
        ((BooleanField) getField(IS_NEWLY_MODIFIED)).setValue(newValue);
        save();
    }

    public boolean isNewlyModified() {
        return ((BooleanField) getField(IS_NEWLY_MODIFIED)).getBooleanValue();
    }

    public boolean canDoTaskDone() {
        return isActive && !isInternalTask();
    }

    public boolean canReopen() {
        return !isActive;
    }

    public boolean canBePaused() {
        return canDoTaskDone(); //these are the same condition as of eiether task can be marked as finished
    }
}
