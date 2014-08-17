package com.jslope.toDoList.core;

import static com.jslope.toDoList.core.ChildrenTypes.TASKS;
import static com.jslope.toDoList.core.ChildrenTypes.TASKS_DONE;
import com.jslope.persistence.ObjectList;
import com.jslope.persistence.sql.Condition;
import com.jslope.persistence.sql.LogicCondition;
import com.jslope.persistence.fields.IntField;
import com.jslope.persistence.fields.BooleanField;
import com.jslope.persistence.fields.TimestampField;
import com.jslope.toDoList.core.persistence.NetObject;
import com.jslope.toDoList.core.persistence.TWIntField;
import com.jslope.toDoList.core.persistence.TWBooleanField;
import com.jslope.toDoList.core.persistence.TWIDField;
import com.jslope.toDoList.core.interfaces.TreeElementContainer;
import com.jslope.utils.Constants;
import com.jslope.UI.TaskTreeModel;

import javax.swing.tree.TreePath;
import javax.swing.*;
import java.util.*;

/**
 * Date: 20.07.2005
 */
public abstract class TreeElement extends NetObject implements TreeElementContainer {
    public static final String PARENT = "parent"
    , SORT_INDEX = "sortIndex"
    , IS_EXPANDED = "isExpanded", IS_DELETED = "is_deleted", CHILDREN_MODIFIED_TIME = "childrenModTime";
    protected TreeElementChildSet childSet;
    protected List<TreeElement> children;
    protected List<TreeElement> doneChildren;

    public void define() {
        super.define();
        this.addField(PARENT, new TWIDField());
        this.addField(SORT_INDEX, new TWIntField());
        this.addField(IS_DELETED, new TWBooleanField(false));
        this.addField(IS_EXPANDED, new BooleanField(true));
        this.addField(CHILDREN_MODIFIED_TIME, new TimestampField()); //it is not time aware, so that when this field is modfied on server it will not update modified time and will not send the node with no other modifications to client
        setUnsentable(IS_EXPANDED);
        childSet = getChildSet();
        initChildren();
    }

    protected void initChildren() {
        children = childSet.getVector(TASKS);
        doneChildren = childSet.getVector(TASKS_DONE);
    }

    protected TreeElementChildSet getChildSet() {
        return new TreeElementChildSet(this, EnumSet.of(TASKS, TASKS_DONE));
    }

    public boolean isExpanded() {
        return ((BooleanField) getField(IS_EXPANDED)).getBooleanValue();
    }

    public void setExpanded(boolean newValue) {
        ((BooleanField) getField(IS_EXPANDED)).setValue(newValue);
    }

    public TreeElement getParent() {
        return parent;
    }

    TreeElement parent = null;

    abstract public void initParent();

    /**
     * returns true if this objects doesn't have any parents locally (if it's on client then it might have parents on server)
     * if parent == null then most probably that this node is root
     */
    public boolean isRoot() {
        return parent == null;
    }


    /**
     * adding existing node
     *
     * @param element
     */
    public void add(TreeElement element) {
        childSet.add(element);
    }

    protected void setParent(TreeElement treeElement, int index) {
        setParent(treeElement);
        setIndex(index);
    }

    public int getIndex() {
        return ((IntField) this.getField(SORT_INDEX)).getIntValue();
    }

    public void setIndex(int newIndex) {
        ((IntField) this.getField(SORT_INDEX)).setIntValue(newIndex);
    }

    public void reindexTasks() {
        childSet.reindex(TASKS);
    }


    public void setParent(TreeElement newParent) {
        this.set(PARENT, newParent.getID());
        parent = newParent;
    }

    public void deleteObject() {
        removeFromParent();
        deleteChildren();
        super.deleteObject();
    }

    public void virtualDelete() {
        removeFromParent();
        virtualDeleteChildren();
        ((TWBooleanField) this.getField(IS_DELETED)).setValue(true);
        this.save();
    }
    public boolean isDeleted() {
        return ((TWBooleanField) this.getField(IS_DELETED)).getBooleanValue();
    }

    public void virtualDeleteChildren() {
        childSet.virtualDeleteChildren();
    }

    private void deleteChildren() {
        childSet.deleteChildren();
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    protected void remove(TreeElement treeElement) {
        childSet.remove(treeElement);
    }

    /**
     * loading subtask which are assigned to
     */
    public void loadChildren() {
        LogicCondition condition = Condition.var(TreeElement.PARENT).Equal(this.getID());
        if (Options.isShowingUsers()) {
            condition = condition.AND(Task.ASSIGNED_TO).Equal(TWIDField.null_id);
        }
        this.loadChildren(condition);
    }

    protected String getChildrenSorter() {
        return TreeElement.SORT_INDEX;
    }

    public void loadChildren(LogicCondition tasksCondition) {
        childSet.init();
        initChildren();

        if (!Options.isShowingTasks()) {  //if we should not show tasks.
            return;
        }

        Task task = new Task();
        ObjectList result;
        result = task.loadObjects(tasksCondition.AND(Task.IS_ACTIVE).Equal(true).AND(IS_DELETED).Equal(false),
                new String[]{Task.IS_INTERNAL_TASK, getChildrenSorter()});
        boolean toReindexTasks = false;
        while (result.hasNext()) {
            task = (Task) result.next();
            children.add(task);
            task.setLoadParent(this);
            task.loadChildren();
            toReindexTasks |= shouldReindex(task); // if (task.shouldReindex()) toReindex = true;
        }
        if (toReindexTasks) {
            reindexTasks();
        }
        if (!Options.getInstance().getHideTaskDone()) {
            long daysOld = Options.getInstance().getTaskDoneFilter();
            long timeStamp = new Date().getTime() - daysOld * Constants.MILLIS_IN_ONE_DAY;
            result = task.loadObjects(tasksCondition.AND(Task.IS_ACTIVE).Equal(false).
                    AND(Task.FINISHED_DATE).MoreThan("" + timeStamp).AND(IS_DELETED).Equal(false),
                    new String[]{Task.FINISHED_DATE});  //todo: to order loaded fields by indexes (when indexes will be added)
            while (result.hasNext()) {
                task = (Task) result.next();
                doneChildren.add(task);
                task.setLoadParent(this);
                task.loadChildren();
            }
        }
    }

    public void saveChildren() {
        childSet.saveChildren();
    }

    public Object getChild(int index) {
        return childSet.getChild(index);
    }

    public int getChildCount() {
        return childSet.getChildCount();
    }

    public boolean isLeaf() {
        return childSet.isLeaf();
    }

    public int indexOfChild(Object child) {
        return childSet.indexOf(child);
    }

    public abstract void updateEditor();

    /**
     * this method is used to gather data from editor when application is closed
     */
    public abstract void updateDataFromEditor();

    protected abstract ChildrenTypes getType();

    public TreePath getPath() {
        return getParentPath(this);
    }

    private TreePath getParentPath(TreeElement treeElement) {
        if (treeElement.isRoot()) {
            return new TreePath(treeElement);
        } else {
            return getParentPath(treeElement.getParent()).pathByAddingChild(treeElement);
        }
    }

    public boolean canMoveUp() {
        if (parent != null) {
            return this.getIndex() > 0;
        } else {
            return false;
        }
    }

    public void moveUp() {
        parent.moveUpChild(this);
    }

    private void moveUpChild(TreeElement child) {
        swapNodes(child.getIndex(), child.getIndex() - 1);
    }

    private void swapNodes(int index, int newIndex) {
        childSet.swapNodes(TASKS, index, newIndex);
    }

    public boolean canMoveDown() {
        if (parent != null) {
            return parent.canMoveDownChild(this);
        } else {
            return false;
        }
    }

    private boolean canMoveDownChild(TreeElement treeElement) {
        return childSet.canMoveDownChild(treeElement);
    }

    public void moveDown() {
        parent.moveDownChild(this);
    }

    private void moveDownChild(TreeElement treeElement) {
        swapNodes(treeElement.getIndex(), treeElement.getIndex() + 1);
    }

    public boolean canMoveLeft() {
        if (parent != null) {
            return parent.canChildMoveLeft();
        } else {
            return false;
        }
    }

    protected abstract boolean canChildMoveLeft();

    public void moveLeft() {
        parent.moveChildOut(this);
    }

    protected void insertAt(int index, TreeElement element) {
        childSet.insertAt(index, element);
    }

    private void moveChildOut(TreeElement treeElement) {
        this.remove(treeElement);
        parent.insertAt(this.getIndex() + 1, treeElement);
    }

    public boolean canMoveRight() {
        return this.getIndex() > 0;
    }

    public void moveIn() {
        parent.moveChildIn(this);
    }

    private void moveChildIn(TreeElement treeElement) {
        TreeElement newParent = childSet.getMoveChildInParent(treeElement);
        this.remove(treeElement);
        newParent.add(treeElement);
    }

    /**
     * returns true if this has as child node
     *
     * @param node to be checked if it is a child of this
     * @return true if this has node as grandchild 
     */
    public boolean hasAsGrandChild(TreeElement node) {
        if (this == node) {
            return true;
        }
        while (!node.isRoot()) {
            node = node.getParent();
            if (this == node) {
                return true;
            }
        }
        return false;
    }

    public List<TreeElement> getChildren() {
        return childSet.getChildren();
    }

    public void addSibling(Task newTask) {
        parent.insertAt(this.getIndex() + 1, newTask);
    }

    public boolean canAddSiblingTask() {
        return !isRoot();
    }

    public boolean canAddSubtask() {
        return true;
    }

    public boolean canMove() {
        return !isRoot();
    }

    public boolean canDelete() {
        return !isRoot();
    }

    public abstract Icon getIcon();

    public void addToTasksDone(Task task) {
        doneChildren.add(0, task);
    }

    public boolean isStillInTree() {
        if (isRoot()) {
            if (this == Options.getInstance().getRootNode()) {
                return true;
            } else {
                return false;
            }
        } else if (parent.hasChild(this)) {
            return parent.isStillInTree();
        }
        return false;
    }

    private boolean hasChild(TreeElement treeElement) {
        return childSet.hasChild(treeElement);
    }

    public boolean canAcceptNewChildren() {
        return true;
    }
    public boolean canBeShared() {
        return false;
    }

    public TreeElement getNextLeafTask() {
        if (children.size() > 0) {
            return children.get(0).getNextLeafTask();
        } else {
            return this;
        }
    }

    public void addObjectsToSend(Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        assert Options.isOnServer();
        LogicCondition taskCondition = Condition.var(TreeElement.PARENT).Equal(this.getID())
                .AND(Task.ASSIGNED_TO).Equal(TWIDField.null_id).AND(CHILDREN_MODIFIED_TIME).MoreThan(afterDate);

        addObjectsToSend(taskCondition, objectsToSend, afterDate, excludeList);

        if (getModifiedTime() > afterDate) {
            if (!excludeList.contains(getID())) {
                objectsToSend.add(this);
            }
        }
    }


    protected static void addObjectsToSend(LogicCondition taskCondition, Collection<NetObject> objectsToSend, long afterDate, Set<String> excludeList) {
        Task task = new Task();
        ObjectList result;
        result = task.loadObjects(taskCondition);
        while (result.hasNext()) {
            task = (Task) result.next();
            task.addObjectsToSend(objectsToSend, afterDate, excludeList);
        }
    }

    public boolean canAddUser() {
        return false;
    }

    public long getChildrenModifiedTime() {
        return ((TimestampField) getField(CHILDREN_MODIFIED_TIME)).getLongValue();
    }

    synchronized public boolean updateChildrenTime(long newTime) {
        assert Options.isOnServer();    //this operation should be executed only on server
        if (isntLoading) {
            if (newTime > getChildrenModifiedTime()) {
                ((TimestampField) getField(CHILDREN_MODIFIED_TIME)).setLongValue(newTime);
                this.save();
                return true;
            }
        }
        return false;
    }


    protected TreeElement getRealParent() {
        return getParent();
    }

    protected void updateTimeRecursively(long newTime) {
        if (updateChildrenTime(newTime)) {
            if (!this.isRoot()) {
                getRealParent().updateTimeRecursively(newTime);
            }
        }
    }


    abstract public void showDebugInfo();

    public void propagateUP() {
        updateChildrenModifiedTime();
        if (!this.isRoot()) {
            getParent().updateTimeRecursively(getModifiedTime());
        }
    }

    protected void updateChildrenModifiedTime() {
        if (getChildrenModifiedTime() < getModifiedTime()) {
            set(CHILDREN_MODIFIED_TIME, get(MODIFIED_TIME));
            save();
        }
    }

    /**
     * it is used for debug purposes
     */
    public String showChildren() {
        List<TreeElement> childs = getChildren();
        String retValue = "[";
        System.out.println("how many children: " + childs.size());
        System.out.println("children: " + childs);
        for (TreeElement node : childs) {
            node.showDebugInfo();
            retValue += node + " ";
        }
        retValue += "]";
        return retValue;
    }

    public void verifyMoveOutOfParent(TreeElement newParent) {
        if (Options.isShowingUsers()) {
            User currentParentUser = getParentUser();
            if (currentParentUser.hasAsGrandChild(newParent)) {
                this.propagateDown();
            } else {
                for (User user : getAllParentUsersUntil(newParent, currentParentUser)) {
                    user.addRefreshMarker();
                }
            }
        }
    }

    public void propagateDown() {
        this.updateTime();
        childSet.propagateDown();
    }

    public List<User> getAllParentUsersUntil(TreeElement newParent, User curentUser) {
        User newParentUser;
        if (newParent instanceof User) {
            newParentUser = (User) newParent;
        } else {
            newParentUser = newParent.getParentUser();
        }
        List<User> usersToBeMarked = new Vector<User>();
        while (!curentUser.isRoot()) {
            usersToBeMarked.add(curentUser);
            curentUser = (User) curentUser.getParent();
            if (newParentUser == curentUser) {
                return usersToBeMarked;
            }
        }
        //if we haven't found newParentUser to be a perent of current user, then it means it's it child and
        // he'll receive information about this object moved to hes child, so we send a empty list to be marked
        return new Vector<User>();
    }

    public boolean shouldReindex(TreeElement element) {
        return childSet.shouldReindex(element);
    }

    public TreeElement selectNextNode() {
        TreeElement nodeToSelect;
        if (this.canMoveDown()) {
            nodeToSelect = (TreeElement) parent.getChild(this.getIndex() + 1);
        } else if (this.canMoveUp()) {
            nodeToSelect = (TreeElement) parent.getChild(this.getIndex() - 1);
        } else {
            nodeToSelect = getParent();
        }
        return nodeToSelect;
    }

    public TreeElement getTreeElement() {
        return this;
    }

    /**
     * this method only removes from parent withoud doing anything additional
     */
    public void justRemoveFromParent() {
        if (parent != null) {
            parent.childSet.remove(this);
        }
    }

    protected void updateThisInTree() {
        TaskTreeModel.getInstance().fireTreeNodeChanged(this);
    }

    public boolean canDoTaskDone() {
        return false;
    }

    public boolean canReopen() {
        return false;
    }

    public boolean canBePaused() {
        return false;
    }
}
