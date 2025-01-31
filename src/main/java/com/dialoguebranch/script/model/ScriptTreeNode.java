package com.dialoguebranch.script.model;

import com.dialoguebranch.model.Constants;
import com.dialoguebranch.model.ResourceType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScriptTreeNode {

    /** The parent node of this ScriptTreeNode (or null if this is the root). */
    private ScriptTreeNode parent;

    /** A pointer to the location (file or database) where this script node is stored. */
    private StorageSource storageSource;

    /** Whether this node represents a script, translation file, or folder. */
    private ResourceType resourceType;

    /** The name of this ScriptTreeNode. */
    private String name;

    /** A list of children of this ScriptTreeNode. */
    private List<ScriptTreeNode> children;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    public ScriptTreeNode(ScriptTreeNode parent, StorageSource storageSource,
                          ResourceType resourceType, String name) {
        this.parent = parent;
        this.storageSource = storageSource;
        this.resourceType = resourceType;
        this.name = name;
        this.children = new ArrayList<>();
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    public ScriptTreeNode getParent() {
        return this.parent;
    }

    public void setParent(ScriptTreeNode parent) {
        this.parent = parent;
    }

    public StorageSource getStorageSource() {
        return this.storageSource;
    }

    public void setStorageSource(StorageSource storageSource) {
        this.storageSource = storageSource;
    }

    public ResourceType getResourceType() {
        return this.resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScriptTreeNode> getChildren() {
        return this.children;
    }

    public void setChildren(List<ScriptTreeNode> children) {
        this.children = children;
        this.children.sort(new ScriptTreeNodeComparator());
    }

    // ------------------------------------------------------- //
    // -------------------- Other Methods -------------------- //
    // ------------------------------------------------------- //

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void addChild(ScriptTreeNode node) {
        this.children.add(node);
        this.children.sort(new ScriptTreeNodeComparator());
    }

    /**
     * Returns the total number of actual scripts that exist under this {@link ScriptTreeNode}
     * (recursively). E.g. if this ScriptTreeNode represents the 'root' language directory, this
     * function will return the total number of scripts or translation files that exist in that
     * language folder. Every leaf node that is not a folder (i.e. an empty folder) is therefore
     * counted.
     *
     * @return the total number of leaf nodes under this {@link ScriptTreeNode}.
     */
    public int getTotalNumberOfScripts() {
        if(this.isLeaf() && (
                this.resourceType == ResourceType.SCRIPT
                || this.resourceType == ResourceType.TRANSLATION)) return 1;
        else {
            int childLeaves = 0;
            for(ScriptTreeNode child : children) {
                childLeaves += child.getTotalNumberOfScripts();
            }
            return childLeaves;
        }
    }

    /**
     * Returns {@code true} if, and only if this {@link ScriptTreeNode} has a direct child with the
     * given {@code name} and the same type (folder / non-folder) as the given boolean {@code
     * isFolder} indicates. E.g. If we are looking for a folder with the name "test", we call this
     * method using "test", and "true". If we are looking for any script (translation or actual
     * script) called "startScript", we call this method using "startScript" and "false".
     *
     * @param name the name of the child node to look for
     * @param isFolder whether the resourceType should be FOLDER or not
     * @return true if such a child exists, false otherwise
     */
    public boolean hasChild(String name, boolean isFolder) {

        for(ScriptTreeNode child : this.children) {

            if(child.getName().equals(name)) {
                if(isFolder) {
                    if(child.getResourceType().equals(ResourceType.FOLDER))
                        return true;
                } else {
                    if(child.getResourceType().equals(ResourceType.SCRIPT) ||
                    child.getResourceType().equals(ResourceType.TRANSLATION)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        if(parent == null) {
            result.append("ROOT: ");
        }

        result.append(this.getStorageSource().getDescriptor()).append(" (").append(this.getName()).append(")");

        if(!children.isEmpty()) {
            result.append(Constants.DLB_PATH_SEPARATOR + " (").append(children.size()).append(" children).");
        }

        if(this.resourceType.equals(ResourceType.SCRIPT)) {
            result.append(Constants.DLB_SCRIPT_FILE_EXTENSION);
        } else if(this.resourceType.equals(ResourceType.TRANSLATION)) {
            result.append(Constants.DLB_TRANSLATION_FILE_EXTENSION);
        } else {
            for(ScriptTreeNode child : this.getChildren()) {
                result.append("\n\t\t").append(this.getName()).append(Constants.DLB_PATH_SEPARATOR).append(child.toString());
            }
        }

        return result.toString();
    }

    private static class ScriptTreeNodeComparator implements Comparator<ScriptTreeNode> {

        @Override
        public int compare(ScriptTreeNode node1, ScriptTreeNode node2) {
            if(node1.isLeaf()) {
                if(node2.isLeaf()) {
                    return node1.getName().compareTo(node2.getName());
                } else {
                    return 1;
                }
            } else {
                if(node2.isLeaf()) {
                    return -1;
                } else {
                    return node1.getName().compareTo(node2.getName());
                }
            }
        }
    }

}
