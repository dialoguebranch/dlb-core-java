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

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Objects;

/**
 * An {@link EditableBody} represents a (partial) body of a DialogueBranch Script Node, including
 * convenience methods that can be used in an editor for such a body. This class informs registered
 * property change listeners of changes in its content using the {@link PropertyChangeSupport}
 * mechanism.
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class EditableBody extends Editable {

    /** The String representing the contents of this {@link EditableBody}. */
    private String script;

    /** Stores whether any changes have been made to this body */
    private boolean isModified;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of an empty {@link EditableBody}.
     */
    public EditableBody() {
        this.script = "";
        this.isModified = false;
    }

    /**
     * Creates an instance of an {@link EditableBody} with a given {@link String} representing the
     * contents of this {@link EditableBody}.
     *
     * @param script the String representing the contents of this {@link EditableBody}.
     */
    public EditableBody(String script) {
        this.script = Objects.requireNonNullElseGet(script, String::new);
        this.isModified = false;
    }

    /**
     * Creates an instance of an {@link EditableBody} with a given {@link List} of Strings
     * representing the contents of this {@link EditableBody}.
     *
     * @param lines the list of Strings representing the contents of this {@link EditableBody}.
     */
    public EditableBody(List<String> lines) {
        if(lines == null || lines.isEmpty()) {
            this.script = "";
        } else {
            StringBuilder bodyScriptBuilder = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) {
                if (i != 0) bodyScriptBuilder.append(System.lineSeparator());
                bodyScriptBuilder.append(lines.get(i));
            }
            this.script = bodyScriptBuilder.toString();
        }
        this.isModified = false;
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    /**
     * Returns the String representing the contents of this {@link EditableBody}.
     *
     * @return the String representing the contents of this {@link EditableBody}.
     */
    public String getScript() {
        return script;
    }

    /**
     * Sets the String representing the contents of this {@link EditableBody}. If the provided
     * String is {@code null}, the contents will be set to an empty String.
     *
     * @param script the String representing the contents of this {@link EditableBody}.
     */
    public void setScript(String script) {
        this.script = Objects.requireNonNullElseGet(script, String::new);
        this.setModified(true);
    }

    /**
     * Returns whether the contents of this body has been modified (true), or not (false).
     *
     * @return whether the contents of this body has been modified (true), or not (false).
     */
    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Sets whether the contents of this body has been modified (true), or not (false).
     *
     * @param isModified whether the contents of this body has been modified (true), or not (false).
     */
    public void setModified(boolean isModified) {
        boolean oldValue = this.isModified;
        this.isModified = isModified;
        this.getPropertyChangeSupport()
                .firePropertyChange(PROPERTY_IS_MODIFIED,oldValue,isModified);
    }

}
