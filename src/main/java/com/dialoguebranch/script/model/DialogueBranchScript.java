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

import com.dialoguebranch.model.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link DialogueBranchScript} models the contents of a DialogueBranch script that may contain
 * errors, because it is still being written. This class may be used by tools that provide editing
 * functionality.
 *
 * <p>You may ask "What makes a DialogueBranch Script a DialogueBranch Script?" When talking about
 * <em>functioning</em> scripts, this answer is strictly defined through the language definition,
 * but when it comes to scripts that are being written, the answer is not so clear.</p>
 *
 * <p>The definition that we use to model this class is designed to be as fault-tolerant as possible
 * while covering the very basics of what entails a DialogueBranch script. This definition is that a
 * DialogueBranch Script has a name, a language code and a list of {@link ScriptNode}s.</p>
 *
 * <p>How the separation of this script into nodes is handled is irrelevant and depends on the
 * underlying storage mechanism (e.g. files, or a database).</p>
 *
 * <p>This class enforces that a name and language code are provided and will assume default values
 * if {@code null} or empty String values are provided.</p>
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class DialogueBranchScript {

    /** The name of this dialogue (that should be unique within a project) */
    private String dialogueName;

    /** The language code for this dialogue */
    private String languageCode;

    /** The List of ScriptNodes that make up the contents of this DialogueBranchScript */
    private List<ScriptNode> scriptNodes;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of an empty {@link DialogueBranchScript} with the given
     * {@code dialogueName} and {@code languageCode}. If the provided {@code dialogueName} is {@code
     * null} or the empty {@link String}, the dialogueName will default to {@link
     * Constants#DLB_DEFAULT_DIALOGUE_NAME}.
     *
     * <p>If the provided {@code languageCode} is {@code null} or the empty {@link String}, the
     * languageCode will default to {@link Constants#DLB_DEFAULT_LANGUAGE_CODE}.</p>
     *
     * @param dialogueName the name of this {@link DialogueBranchScript}.
     * @param languageCode the language code for this {@link DialogueBranchScript}.
     */
    public DialogueBranchScript(String dialogueName, String languageCode) {
        // Set the dialogue name, or use the default value
        if(dialogueName == null || dialogueName.isEmpty()) {
            this.dialogueName = Constants.DLB_DEFAULT_DIALOGUE_NAME;
        } else {
            this.dialogueName = dialogueName;
        }

        // Set the languageCode, or use the default value
        if(languageCode == null || languageCode.isEmpty()) {
            this.languageCode = Constants.DLB_DEFAULT_LANGUAGE_CODE;
        } else {
            this.languageCode = languageCode;
        }

        this.scriptNodes = new ArrayList<>();
    }

    /**
     * Creates an instance of a {@link DialogueBranchScript} with the given {@code dialogueName},
     * {@code languageCode} and list of {@link ScriptNode}s, representing the contents of this
     * {@link DialogueBranchScript}.
     *
     * <p>If the provided {@code dialogueName} is {@code null} or the empty {@link String}, the
     * dialogueName will default to {@link Constants#DLB_DEFAULT_DIALOGUE_NAME}.</p>
     *
     * <p>If the provided {@code languageCode} is {@code null} or the empty {@link String}, the
     * languageCode will default to {@link Constants#DLB_DEFAULT_LANGUAGE_CODE}.</p>
     *
     * @param dialogueName the name of this {@link DialogueBranchScript}.
     * @param languageCode the language code for this {@link DialogueBranchScript}.
     * @param scriptNodes the {@link ScriptNode}s that make up this {@link DialogueBranchScript}.
     */
    public DialogueBranchScript(String dialogueName, String languageCode,
                                List<ScriptNode> scriptNodes) {
        this(dialogueName, languageCode);

        this.scriptNodes = Objects.requireNonNullElseGet(scriptNodes, ArrayList::new);
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    /**
     * Returns the name of this {@link DialogueBranchScript} as a String.
     *
     * @return the name of this {@link DialogueBranchScript} as a String.
     */
    public String getDialogueName() {
        return dialogueName;
    }

    /**
     * Sets the name of this {@link DialogueBranchScript} as a String. If the provided {@code
     * dialogueName} is {@code null} or the empty {@link String}, the dialogueName will default to
     * {@link Constants#DLB_DEFAULT_DIALOGUE_NAME}.
     *
     * @param dialogueName the name of this {@link DialogueBranchScript} as a String.
     */
    public void setDialogueName(String dialogueName) {
        if(dialogueName == null || dialogueName.isEmpty()) {
            this.dialogueName = Constants.DLB_DEFAULT_DIALOGUE_NAME;
        } else {
            this.dialogueName = dialogueName;
        }
    }

    /**
     * Returns the language code for this {@link DialogueBranchScript}.
     *
     * @return the language code for this {@link DialogueBranchScript}.
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the language code for this {@link DialogueBranchScript} as a String. If the provided
     * {@code languageCode} is {@code null} or the empty {@link String}, the languageCode will
     * default to {@link Constants#DLB_DEFAULT_LANGUAGE_CODE}.
     *
     * @param languageCode the language code for this {@link DialogueBranchScript}.
     */
    public void setLanguageCode(String languageCode) {
        if(languageCode == null || languageCode.isEmpty()) {
            this.languageCode = Constants.DLB_DEFAULT_LANGUAGE_CODE;
        } else {
            this.languageCode = languageCode;
        }
    }

    /**
     * Returns the List of {@link ScriptNode}s that make up this {@link DialogueBranchScript}.
     *
     * @return the List of {@link ScriptNode}s that make up this {@link DialogueBranchScript}.
     */
    public List<ScriptNode> getScriptNodes() {
        return scriptNodes;
    }

    /**
     * Sets the List of {@link ScriptNode}s that make up this {@link DialogueBranchScript}. If a
     * {@code null}-value is provided, the list of script nodes is set to an empty list.
     *
     * @param scriptNodes the List of {@link ScriptNode}s that make up this {@link
     *                    DialogueBranchScript}.
     */
    public void setScriptNodes(List<ScriptNode> scriptNodes) {
        this.scriptNodes = Objects.requireNonNullElseGet(scriptNodes, ArrayList::new);
    }

    // ------------------------------------------------------- //
    // -------------------- Other Methods -------------------- //
    // ------------------------------------------------------- //

    /**
     * Adds the given {@link ScriptNode} to the list of {@link ScriptNode}s for this {@link
     * DialogueBranchScript} if a non-{@code null} value is provided.
     *
     * @param scriptNode the {@link ScriptNode} to add to this {@link DialogueBranchScript}.
     */
    public void addScriptNode(ScriptNode scriptNode) {
        if(scriptNode != null)
            this.scriptNodes.add(scriptNode);
    }

    /**
     * Returns a human-readable String representation of this {@link DialogueBranchScript}.
     * @return a human-readable String representation of this {@link DialogueBranchScript}.
     */
    public String toString() {
        return "DialogueBranchScript with name '" + dialogueName
                + "' in language '" + languageCode + "' and " + scriptNodes.size() + " nodes.";
    }

}
