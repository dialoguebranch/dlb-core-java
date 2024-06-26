/*
 *
 *                Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 *
 *     This material is part of the DialogueBranch Platform, and is covered by the MIT License
 *                                        as outlined below.
 *
 *                                            ----------
 *
 * Copyright (c) 2023-2024 Fruit Tree Labs (www.fruittreelabs.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.dialoguebranch.script.model;

import com.dialoguebranch.model.Language;
import com.dialoguebranch.model.ProjectMetaData;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link EditableProject} represents a Dialogue Branch project at the state of being edited.
 * It consists of a {@link ProjectMetaData} object representing the metadata information on the
 * project, and a set of {@link EditableScript}s, representing the dialogue branch scripts that
 * may yet still be edited.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class EditableProject extends Editable implements PropertyChangeListener {

    /** The project metadata information */
    private ProjectMetaData projectMetaData;

    /** A mapping of language to roots of trees of script contents */
    private Map<Language,ScriptTreeNode> availableScripts;

    /** The set of "active" EditableScripts representing the scripts in this project */
    private Map<String,EditableScript> activeScripts;

    /** Stores whether any changes have been made to this project */
    private boolean isModified;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of a new empty {@link EditableProject} from the given {@link
     * ProjectMetaData}.
     *
     * @param projectMetaData the project meta data object.
     */
    public EditableProject(ProjectMetaData projectMetaData) {
        this.projectMetaData = projectMetaData;
        this.availableScripts = new HashMap<>();
        this.activeScripts = new HashMap<>();
        this.isModified = false;
    }

    /**
     * Creates an instance of a new empty {@link EditableProject} from the given {@link
     * ProjectMetaData} and the map of {@code availableScripts}.
     *
     * @param projectMetaData the project meta data object.
     * @param availableScripts a mapping from {@link Language} to {@link ScriptTreeNode},
     *                         representing the root of the tree of script contents.
     */
    public EditableProject(ProjectMetaData projectMetaData,
                           Map<Language,ScriptTreeNode> availableScripts) {
        this.projectMetaData = projectMetaData;
        this.availableScripts = availableScripts;
        this.activeScripts = new HashMap<>();
        this.isModified = false;
    }

    // ------------------------------------------------------------ //
    // -------------------- Getters & Setters --------------------- //
    // ------------------------------------------------------------ //

    /**
     * Returns the project metadata object for this {@link EditableProject}.
     *
     * @return the project metadata object for this {@link EditableProject}.
     */
    public ProjectMetaData getProjectMetaData() {
        return this.projectMetaData;
    }

    /**
     * Sets the project metadata object for this {@link EditableProject}.
     *
     * @param projectMetaData the project metadata object for this {@link EditableProject}.
     */
    public void setProjectMetaData(ProjectMetaData projectMetaData) {
        ProjectMetaData oldValue = this.projectMetaData;
        this.projectMetaData = projectMetaData;
        this.getPropertyChangeSupport().firePropertyChange(
                PROPERTY_PROJECT_METADATA,oldValue,projectMetaData);
    }

    /**
     * Returns a mapping from {@link Language}s to {@link ScriptTreeNode}s, representing the roots
     * of the trees of available scripts in this project.
     *
     * @return the map of available scripts in this project.
     */
    public Map<Language,ScriptTreeNode> getAvailableScripts() {
        return this.availableScripts;
    }

    /**
     * Sets a mapping from {@link Language} to a list of script names, indicating the full set of
     * available scripts in this project, and informing any {@link PropertyChangeListener}s of the
     * change
     *
     * @param availableScripts the map of available scripts in this project.
     */
    public void setAvailableScripts(Map<Language,ScriptTreeNode> availableScripts) {
        Map<Language,ScriptTreeNode> oldValue = new HashMap<>(this.availableScripts);
        this.availableScripts = availableScripts;
        this.getPropertyChangeSupport().firePropertyChange(
                PROPERTY_PROJECT_AVAILABLE_SCRIPTS,oldValue,this.availableScripts);
    }

    // -------------------------------------------------------- //
    // -------------------- Other Methods --------------------- //
    // -------------------------------------------------------- //

    public void addActiveScript(EditableScript editableScript) {
        activeScripts.put(editableScript.getDialogueName(),editableScript);
        editableScript.addPropertyChangeListener(this);
    }

    public void removeActiveScript(EditableScript editableScript) {
        editableScript.removePropertyChangeListener(this);
        activeScripts.remove(editableScript.getDialogueName());
    }

    public boolean isModified() {
        return this.isModified;
    }

    public void setModified(boolean isModified) {
        boolean oldValue = this.isModified;
        this.isModified = isModified;

        // If this project is set to be _not_ modified, all its active script are also not modified
        if(!isModified) {
            for(EditableScript editableScript : activeScripts.values()) {
                editableScript.setModified(false);
            }
        }
        this.getPropertyChangeSupport()
                .firePropertyChange(PROPERTY_IS_MODIFIED,oldValue,isModified);
    }

    public List<EditableScript> getModifiedScripts() {
        List<EditableScript> result = new ArrayList<>();
        for(EditableScript script : activeScripts.values()) {
            if(script.isModified()) {
                result.add(script);
            }
        }
        return result;
    }

    public ScriptTreeNode getScriptTreeForLanguage(String languageCode) {
        for(Language language : availableScripts.keySet()) {
            if(language.getCode().equals(languageCode)) return availableScripts.get(language);
        }
        return null;
    }

    // ------------------------------------------------------------------- //
    // -------------------- Property Change Listeners -------------------- //
    // ------------------------------------------------------------------- //

    /**
     * This method gets called when a bound property is changed.
     *
     * @param event A PropertyChangeEvent object describing the event source and the property that
     *              has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(PROPERTY_IS_MODIFIED)) {
            boolean modified = (Boolean) event.getNewValue();
            // If any of its active scripts are modified, this project is modified
            if(modified) {
                this.setModified(true);
            }
        }
    }

}
