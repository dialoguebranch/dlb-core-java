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

import java.util.Objects;

/**
 * A {@link ScriptNode} represents a node in Dialogue Branch script that may or may not be correctly
 * parseable according to the Dialogue Branch Language Specification. This class may be used to e.g.
 * represent a node during editing, and it is extremely fault-tolerant.
 *
 * <p>A {@link ScriptNode} is a simple container class that contains a {@link ScriptNodeHeader} and
 * a {@link ScriptNodeBody}, both of which can be empty, but never {@code null}.</p>
 *
 * @author Harm op den Akker (Fruit Tree Labs)
 */
public class ScriptNode {

    private ScriptNodeHeader header;
    private ScriptNodeBody body;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of an empty {@link ScriptNode}.
     */
    public ScriptNode() {
        header = new ScriptNodeHeader();
        body = new ScriptNodeBody();
    }

    /**
     * Creates an instance of a {@link ScriptNode} with a given {@code header} and {@code body}.
     *
     * @param header the {@link ScriptNodeHeader} representing the header of this {@link ScriptNode}.
     * @param body the {@link ScriptNodeBody} representing the body of this {@link ScriptNode}.
     */
    public ScriptNode(ScriptNodeHeader header, ScriptNodeBody body) {
        this.header = Objects.requireNonNullElseGet(header, ScriptNodeHeader::new);
        this.body = Objects.requireNonNullElseGet(body, ScriptNodeBody::new);
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    public ScriptNodeHeader getHeader() {
        return header;
    }

    public void setHeader(ScriptNodeHeader header) {
        this.header = Objects.requireNonNullElseGet(header, ScriptNodeHeader::new);
    }

    public ScriptNodeBody getBody() {
        return body;
    }

    public void setBody(ScriptNodeBody body) {
        this.body = Objects.requireNonNullElseGet(body, ScriptNodeBody::new);
    }


    // ------------------------------------------------------- //
    // -------------------- Other Methods -------------------- //
    // ------------------------------------------------------- //

    public String toString() {
        StringBuilder result = new StringBuilder();
        for(String s : header.getLines()) {
            result.append(s).append(System.lineSeparator());
        }
        result.append(Constants.DLB_HEADER_SEPARATOR).append(System.lineSeparator());
        for(String s : body.getLines()) {
            result.append(s).append(System.lineSeparator());
        }
        result.append(Constants.DLB_NODE_SEPARATOR).append(System.lineSeparator());

        return String.valueOf(result);
    }

}
