package com.dialoguebranch.script.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScriptNodeHeader {

    /** The list of Strings representing the contents of this {@link ScriptNodeHeader}. */
    List<String> lines;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    /**
     * Creates an instance of an empty {@link ScriptNodeHeader}.
     */
    public ScriptNodeHeader() {
        lines = new ArrayList<>();
    }

    /**
     * Creates an instance of a {@link ScriptNodeHeader} with a given list of {@link String}s
     * representing the contents of this {@link ScriptNodeHeader}.
     *
     * @param lines the list of Strings representing the contents of this {@link ScriptNodeHeader}.
     */
    public ScriptNodeHeader(List<String> lines) {
        this.lines = Objects.requireNonNullElseGet(lines, ArrayList::new);
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    /**
     * Returns the list of Strings representing the contents of this {@link ScriptNodeHeader}.
     *
     * @return the list of Strings representing the contents of this {@link ScriptNodeHeader}.
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * Sets the list of Strings representing the contents of this {@link ScriptNodeHeader}. If the
     * provided list is {@code null}, the contents will be set to an empty list.
     *
     * @param lines the list of Strings representing the contents of this {@link ScriptNodeHeader}.
     */
    public void setLines(List<String> lines) {
        this.lines = Objects.requireNonNullElseGet(lines, ArrayList::new);
    }

    // ------------------------------------------------------- //
    // -------------------- Other Methods -------------------- //
    // ------------------------------------------------------- //

    /**
     * Adds the given {@link String} to the list of contents for this {@link ScriptNodeHeader} if it
     * is not {@code null}.
     *
     * @param line the line to add to this {@link ScriptNodeHeader}.
     */
    public void addLine(String line) {
        if(line != null) {
            this.lines.add(line);
        }
    }

}
