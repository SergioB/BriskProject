package com.jslope.UI;

import com.jslope.toDoList.core.TreeElement;
import com.jslope.toDoList.core.Options;
import com.jslope.toDoList.core.User;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import java.util.Vector;
import java.util.List;

/**
 * Date: 11.08.2005
 */
public class TaskTreeModel implements TreeModel {
    private TreeElement rootNode;
    private List<TreeModelListener> listeners;

    private TaskTreeModel() {
        setUserRoot();
        loadTree();
        System.out.println("after load");
        listeners = new Vector<TreeModelListener>();
    }

    public void loadTree() {
        rootNode.loadChildren();
    }

    /**
     * this function does both - load tree data and sends signal to listening trees to reload 
     */
    public void reloadTree() {
        loadTree();
        reload();
    }

    private static TaskTreeModel ourInstance = new TaskTreeModel();

    public static TaskTreeModel getInstance() {
        return ourInstance;
    }

    public Object getRoot() {
        return rootNode;
    }

    public Object getChild(Object parent, int index) {
        TreeElement element = (TreeElement) parent;
        return element.getChild(index);
    }

    public int getChildCount(Object parent) {
        return ((TreeElement) parent).getChildCount();
    }

    public boolean isLeaf(Object node) {
        return ((TreeElement) node).isLeaf();
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // our tree is still not editable so it should do nothing, we'll show just a warning:
        System.out.println("valueForPathChanged newValue=" + newValue);
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeElement) parent).indexOfChild(child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void reload(TreeElement node) {
        fireTreeStructureChanged(this, getPathToRoot(node), null, null);
    }
    public void reload() {
        reload(rootNode);
    }

    private TreePath getPathToRoot(TreeElement node) {
        return node.getPath();
    }


    protected void fireTreeStructureChanged(Object source, TreePath path,
                                            int[] childIndices,
                                            Object[] children) {
        TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
        for (TreeModelListener l : listeners) {
            l.treeStructureChanged(e);
        }
    }
    public void fireTreeNodeChanged(TreeElement node) {
        TreeModelEvent e = new TreeModelEvent(this, node.getPath(), null, null);
        for (TreeModelListener l : listeners) {
            l.treeNodesChanged(e);
        }
    }

    public void saveRootNode() {
        rootNode.save();
        rootNode.saveChildren();
    }

    public void setTaskRoot() {
        if (rootNode instanceof User) {
            rootNode = ((User)rootNode).getInternalTask();
        }
    }

    public void setUserRoot() {
        rootNode = Options.getInstance().getRootNode();
    }

}
