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

import com.dialoguebranch.script.parser.EditableHeaderParser;

import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * An {@link EditableHeader} represents a (partial) header of a DialogueBranch Script Node,
 * including convenience methods that can be used in an editor for such a header. This class informs
 * registered property change listeners of changes in its content using the {@link
 * PropertyChangeSupport} mechanism.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class EditableHeader extends Editable {

    /** An EditableHeader is part of this EditableNode */
    private final EditableNode editableNode;

    /** The script that makes up the content of this {@link EditableHeader}. */
    private String script;

    /** The results of successfully parsed tags in the header */
    private Map<String,String> tags;

    /** Stores whether any changes have been made to this header */
    private boolean isModified;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of an empty {@link EditableHeader} that belongs to the given {@link
     * EditableNode}.
     *
     * @param editableNode the {@link EditableNode} to which this header belongs.
     */
    public EditableHeader(EditableNode editableNode) {
        this.editableNode = editableNode;
        this.script = "";
        this.tags = new HashMap<>();
        this.isModified = false;
    }

    /**
     * Creates an instance of an {@link EditableHeader} that belongs to the given {@link
     * EditableNode} with a given {@code script} String representing the contents of this
     * {@link EditableHeader}.
     *
     * @param editableNode the {@link EditableNode} to which this header belongs.
     * @param script the script representing the contents of this {@link EditableHeader}.
     */
    public EditableHeader(EditableNode editableNode, String script) {
        this.editableNode = editableNode;
        this.script = Objects.requireNonNullElseGet(script, String::new);
        this.tags = new HashMap<>();
        this.isModified = false;

        EditableHeaderParser.parseHeader(this);
    }

    /**
     * Creates an instance of an {@link EditableHeader} that belongs to the given {@link
     * EditableNode} with a given {@link List} of Strings representing the contents of this {@link
     * EditableHeader}.
     *
     * @param editableNode the {@link EditableNode} to which this header belongs.
     * @param lines the list of Strings representing the contents of this {@link EditableHeader}.
     */
    public EditableHeader(EditableNode editableNode, List<String> lines) {
        this.editableNode = editableNode;
        if(lines == null || lines.isEmpty()) {
            this.script = "";
        } else {
            StringBuilder headerScriptBuilder = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                if (i != 0) headerScriptBuilder.append(System.lineSeparator());
                headerScriptBuilder.append(lines.get(i));
            }
            this.script = headerScriptBuilder.toString();
        }
        this.tags = new HashMap<>();
        this.isModified = false;

        EditableHeaderParser.parseHeader(this);
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    /**
     * Returns the {@link EditableNode} to which this {@link EditableHeader} belongs.
     *
     * @return the {@link EditableNode} to which this {@link EditableHeader} belongs.
     */
    public EditableNode getEditableNode() {
        return editableNode;
    }

    /**
     * Returns the String representing the contents of this {@link EditableHeader}.
     *
     * @return the String representing the contents of this {@link EditableHeader}.
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the String representing the contents of this {@link EditableHeader}. If the
     * provided String is {@code null}, the contents will be set to an empty String.
     *
     * @param script the String representing the contents of this {@link EditableHeader}.
     */
    public void setScript(String script) {
        this.script = Objects.requireNonNullElseGet(script, String::new);
        this.setModified(true);
        EditableHeaderParser.parseHeader(this);
    }

    /**
     * Returns the key-value mapping of the tags that were parsed from this header's script.
     * @return the key-value mapping of the tags that were parsed from this header's script.
     */
    public Map<String,String> getTags() {
        return this.tags;
    }

    /**
     * Sets the key-value mapping of the tags that were parsed from this header's script.
     * @param tags the key-value mapping of the tags that were parsed from this header's script.
     */
    public void setTags(Map<String,String> tags) {
        this.tags = tags;
        // TODO: If the tags are set, the script model should be updated
    }

    /**
     * Returns whether the contents of this header has been modified (true), or not (false).
     *
     * @return whether the contents of this header has been modified (true), or not (false).
     */
    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Sets whether the contents of this header has been modified (true), or not (false).
     *
     * @param isModified whether the contents of this header has been modified (true), or not
     *                   (false).
     */
    public void setModified(boolean isModified) {
        boolean oldValue = this.isModified;
        this.isModified = isModified;
        this.getPropertyChangeSupport()
                .firePropertyChange(PROPERTY_IS_MODIFIED,oldValue,isModified);
    }

}
