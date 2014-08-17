package com.jslope.toDoList.core;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Vector;
import java.util.List;

/**
 * Date: 01.11.2005
 */
public class TreeElementChildSet {

    EnumSet <ChildrenTypes> typeSet;
    private EnumMap<ChildrenTypes, List<TreeElement>> map;
    TreeElement parent;
    TreeElementChildSet(TreeElement parent, EnumSet<ChildrenTypes> set) {
        this.parent = parent;
        this.typeSet = set;
        map = new EnumMap<ChildrenTypes, List<TreeElement>>(ChildrenTypes.class);
        init();
    }

    public void init() {
        for (ChildrenTypes type: typeSet) {
            map.put(type, new Vector<TreeElement>());
        }
    }

    public List<TreeElement> getVector(ChildrenTypes type) {
        return map.get(type);
    }

    public void reindex(ChildrenTypes type) {
        List<TreeElement> children = getVector(type);
        TreeElement element;
        int size = children.size();
        for (int i = 0; i < size; i++) {
            element = children.get(i);
            element.setIndex(i);
            element.save();
        }

    }

    public void deleteChildren() {
        for (ChildrenTypes type : typeSet) {
            List<TreeElement> children = getVector(type);
            while(children.size()>0) {
                children.get(0).deleteObject();
            }
        }
    }

    //todo: to verify  why does it need to  remove from all the types instead of just removing from children  type
    public void remove(TreeElement treeElement) {
        for (ChildrenTypes type : typeSet) {
            if (getVector(type).remove(treeElement)) {
                if (type != ChildrenTypes.TASKS_DONE) {
                    reindex(type);
                }
                return;
            }
        }
    }

    public void saveChildren() {
        for (ChildrenTypes type : typeSet) {
            for (TreeElement element : getVector(type)) {
                element.save();
                element.saveChildren();
            }
        }
    }

    public TreeElement getChild(int index) {
        int offset = 0;
        for (ChildrenTypes type : typeSet) {
            List<TreeElement> children = getVector(type);
            if (index - offset < children.size()) {
                return children.get(index - offset);
            } else {
                offset += children.size();
            }
        }
        throw new IndexOutOfBoundsException("Index "+index+" is too big");
    }

    public int getChildCount() {
        int childCount = 0;
        for (ChildrenTypes type : typeSet) {
            childCount += getVector(type).size();
        }
        return childCount;
    }

    /**
     * returns true if there is no one child.
     * @return true if is leaf
     */
    public boolean isLeaf() {
        for (ChildrenTypes type : typeSet) {
            if (getVector(type).size() != 0) {
                return false;
            }
        }
        return true;
    }

    public int indexOf(Object child) {
        int offset = 0, index;
        for (ChildrenTypes type : typeSet) {
            List<TreeElement> children = getVector(type);
            index = children.indexOf(child);
            if (index == -1) {
                offset += children.size();
            } else {
                return index + offset;
            }
        }
        return -1;
    }

    public void swapNodes(ChildrenTypes type, int index, int newIndex) {
        List<TreeElement> children = getVector(type);
        TreeElement tmp = children.get(newIndex);
        children.set(newIndex, children.get(index));
        children.set(index, tmp);
        reindex(type);
    }

    public void add(TreeElement element) {
        List<TreeElement> children = getVector(element);
        element.setParent(parent, children.size());
        element.save();
        children.add(element);
    }

    private List<TreeElement> getVector(TreeElement element) {
        return getVector(element.getType());
    }

    public boolean canMoveDownChild(TreeElement treeElement) {
        List<TreeElement> children = getVector(treeElement);
        return treeElement.getIndex() < children.size() - 1;
    }

    public void insertAt(int index, TreeElement treeElement) {
        List<TreeElement> children = getVector(treeElement);
        children.add(index, treeElement);
        treeElement.setParent(parent);
        reindex(treeElement.getType());

    }

    public TreeElement getMoveChildInParent(TreeElement treeElement) {
        List<TreeElement> children = getVector(treeElement);
        TreeElement newParent = children.get(treeElement.getIndex() - 1);
        return newParent;
    }

    public List<TreeElement> getChildren() {
        List<TreeElement> retValue = new Vector<TreeElement>();
        for (ChildrenTypes type : typeSet ) {
            retValue.addAll(getVector(type));
        }
        return retValue;
    }

    public boolean hasChild(TreeElement treeElement) {
        for (ChildrenTypes type : typeSet) {
            if (getVector(type).contains(treeElement)) {
                return true;
            }
        }
        return false;
    }

    public void virtualDeleteChildren() {
        for (ChildrenTypes type : typeSet) {
            List<TreeElement> children = getVector(type);
            while(children.size()>0) {
                children.get(0).virtualDelete();
            }
        }
    }

    /**
     * this method is for cases where 2 elements were added at the same time to the same parent
     * @param element
     * @return true if should reindex
     */
    public boolean shouldReindex(TreeElement element) {
        try {
        List<TreeElement> children = getVector(element);
        int index = children.indexOf(element);
        if (index == -1) {
            //can't be possible, it is called only after element was added to vector
            throw new RuntimeException("Element "+element+" with id="+element.getID() +" doesn't exist in "+children);
        } else {
            int ownIndex = element.getIndex();
            if (index != ownIndex) {
                return true;
            } else {
                if (index > 0) {
                    if (children.get(index-1).getIndex() != ownIndex-1) {
                        return true;
                    }
                }
            }
        }
        return false;
        } catch (NullPointerException e) {
            System.out.println("this object = "+ this.parent);
            System.out.println("element = " + element);
            throw new NullPointerException(e.toString());
        }
    }

    public void propagateDown() {
        for (ChildrenTypes type : typeSet ) {
            for (TreeElement child : getVector(type)) {
                child.propagateDown();
            }
        }
    }

    public List<TreeElement> getTasks() {
        List<TreeElement> retValue = new Vector<TreeElement>();
        for (ChildrenTypes type : EnumSet.of(ChildrenTypes.TASKS, ChildrenTypes.TASKS_DONE) ) {
            retValue.addAll(getVector(type));
        }
        return retValue;
    }
}
