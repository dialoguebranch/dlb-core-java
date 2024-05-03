package com.dialoguebranch.script.model;

import com.dialoguebranch.model.Constants;
import com.dialoguebranch.model.FileType;

import java.util.ArrayList;
import java.util.List;

public class ScriptTreeNode {

    private ScriptTreeNode parent;
    private StorageSource storageSource;
    private FileType fileType;
    private String name;
    private List<ScriptTreeNode> children;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    public ScriptTreeNode(ScriptTreeNode parent, StorageSource storageSource, FileType fileType, String name) {
        this.parent = parent;
        this.storageSource = storageSource;
        this.fileType = fileType;
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

    public FileType getFileType() {
        return this.fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
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
    }

    // ------------------------------------------------------- //
    // -------------------- Other Methods -------------------- //
    // ------------------------------------------------------- //

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void addChild(ScriptTreeNode node) {
        this.children.add(node);
    }

    public String toString() {
        String result = "";
        if(parent == null) {
            result += "ROOT: ";
        }

        result += this.getStorageSource().getDescriptor() + " ("+ this.getName() +")";

        if(!children.isEmpty()) {
            result += Constants.DLB_PATH_SEPARATOR + " ("+children.size()+" children).";
        }

        if(this.fileType.equals(FileType.SCRIPT)) {
            result += Constants.DLB_SCRIPT_FILE_EXTENSION;
        } else if(this.fileType.equals(FileType.TRANSLATION)) {
            result += Constants.DLB_TRANSLATION_FILE_EXTENSION;
        } else {
            for(ScriptTreeNode child : this.getChildren()) {
                result += "\n\t\t"+this.getName()+Constants.DLB_PATH_SEPARATOR+child.toString();
            }
        }

        return result;
    }

}
