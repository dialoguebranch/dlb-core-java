package com.dialoguebranch.writer;

import com.dialoguebranch.script.model.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;

public class EditableTranslationWriter {

    /**
     * Writes the given {@link EditableTranslation} to its source file.
     *
     * @param editableTranslation the {@link EditableTranslation} to write.
     * @throws IOException in case of any write error.
     */
    public static void write(EditableTranslation editableTranslation) throws IOException {
        StorageSource storageSource = editableTranslation.getStorageSource();
        if(storageSource instanceof FileStorageSource fileStorageSource) {
            File translationFile = fileStorageSource.getSourceFile();
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(translationFile, editableTranslation.getTranslations());
        }

    }

}
