package com.dialoguebranch.script.model;

import java.util.ArrayList;
import java.util.List;

public class EditableTranslationSet {

    private List<EditableTranslation> editableTranslations;

    // -------------------------------------------------------- //
    // -------------------- Constructor(s) -------------------- //
    // -------------------------------------------------------- //

    public EditableTranslationSet() {
        editableTranslations = new ArrayList<>();
    }

    public EditableTranslationSet(List<EditableTranslation> editableTranslations) {
        this.editableTranslations = editableTranslations;
    }

    // ----------------------------------------------------------- //
    // -------------------- Getters & Setters -------------------- //
    // ----------------------------------------------------------- //

    public List<EditableTranslation> getEditableTranslations() {
        return editableTranslations;
    }

    public void setEditableTranslations(List<EditableTranslation> editableTranslations) {
        this.editableTranslations = editableTranslations;
    }

    // -------------------------------------------------------- //
    // -------------------- Public Methods -------------------- //
    // -------------------------------------------------------- //

    public void addEditableTranslation(EditableTranslation editableTranslation) {
        editableTranslations.add(editableTranslation);
    }

}
